package com.highgo.hgdbadmin.vthread;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.highgo.hgdbadmin.log.DerbyUtil;
import com.highgo.hgdbadmin.myutil.C3P0Util;
import com.highgo.hgdbadmin.myutil.DBUtil;
import com.highgo.hgdbadmin.reportable.TableDetail;

public class WriteThread implements Runnable {

	private static Logger logger = Logger.getLogger(WriteThread.class);
	private CountDownLatch startGate;
	private CountDownLatch endGate;
	private Buffer<Record> buffer;

	private String toSchema;
	private String toTable;

	private int batchSize;
	private AtomicLong al;
	private TableDetail tableDetail;
	
	private List<Record> redo;

	private Connection conn;
	
	
	

	public WriteThread(CountDownLatch startGate, CountDownLatch endGate, Buffer<Record> buffer, String toSchema,
			String toTable, int batchSize,AtomicLong al,TableDetail tableDetail) {
		try {
			this.conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
		} catch (SQLException e) {
			try {
				this.conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			} catch (SQLException e1) {
				logger.error(e1.getMessage());
			}
		}
		this.startGate = startGate;
		this.endGate = endGate;
		this.buffer = buffer;
		this.toSchema = toSchema;
		this.toTable = toTable;
		this.batchSize = batchSize;
		this.al = al;
		this.tableDetail = tableDetail;
		this.redo = new LinkedList<>();
	}

	@Override
	public void run() {
		try {
			startGate.await();
			doAction();
		} catch (InterruptedException e) {
		} finally {
			endGate.countDown();
		}

	}

	private void doAction() throws InterruptedException {
		List<String> errors = new LinkedList<>();
		Date start = new Date();
		Lock.cdl.await();
		PreparedStatement ps = null;
		try {
			conn.setAutoCommit(false);
			String sql = makeSql(toSchema, toTable);
			ps = conn.prepareStatement(sql);
		} catch (SQLException e1) {
			errors.add(e1.getMessage());
			logger.error(e1.getMessage());
		}
		redo.clear();
		int m = 1;
		Record rec = null;
		/**
		 * Lock.isComeOn.get()==true ˵�� ���̻߳��ţ���ʱ�����buffer��ʱ��Ƭ��û�����ݣ�it��s
		 * okay��buffer�������ݵĻ�ҲOkay�� buffer.size!=0
		 * ˵��buffer�к������ݣ���ʱ����߳̿��ܽ���������ҲҪ��buffer�е�ʣ�����ݴ����ꡣ
		 * 
		 * ����ط���һ��������ǣ�����������е�Lock.isComeOn.get()ʱ�ж���ȷ��Ҳ���Ƕ��̻߳��ڣ�
		 * ���жϵ�buffer.size() != 0ʱҲ��true��������ʱ��buffer��Ψһ��һ�����ݽ��ű��������߳������ˣ�
		 * ���ʱ�򣬱��߳̾ͻ�������rec = buffer.take();��
		 * ����ط�����ʹ��BlockingQueue#poll(timeout,
		 * unit)����������ͬ����������⣬���統Buffer�е����ݲ�����ʱ��ĳЩ���Ȼ���Ƚ��ٵ�WriteThread�߳̿��ܾͻᳬʱ
		 */
		while (Lock.isComeOn.get() || buffer.size() != 0) {
			// rec = buffer.take();
			rec = buffer.poll(500L, TimeUnit.MILLISECONDS);
			if (rec != null) {
				try {
					rec.write(ps);
					redo.add(rec);
					m++;
					ps.addBatch();
					// ��ͻȻ�˳�catch (SQLException e) ������ʱ�򣬿��ܻ�ʹconn���close
					if (conn == null || conn.isClosed()) {
						conn = DBUtil.getNewConnection(C3P0Util.POSTGRES);
						if (conn != null) {
							conn.setAutoCommit(false);
							String sql = makeSql(toSchema, toTable);
							ps = conn.prepareStatement(sql);
						}
					}
					// ����һ��Batch�ύ����������buffer��û�������˾��ύ��
					if (m % batchSize == 0 || buffer.size() == 0) {
						ps.executeBatch();
						conn.commit();
						//��һ�д����ر����棬���conn.commit�ɹ����ͼ������redo.size(),���conn.commitʧ�ܣ��������벻�ᱻִ�У�ֱ������catch���ȥ�ˣ�Ȼ�����ReInsert�̵߳�������
						al.addAndGet(redo.size());
						redo.clear();// ������ߵ���һ�д��룬˵���ύ�ɹ��ˣ�Ȼ���redo list���
					}
				} catch (SQLException e) {
					// û�в���ɹ��ļ�¼���뵽derby��
					// ������ط���Ҫ������ӳ���û�õ����ӣ���Ȼһ��ʧ�ܵĽ���һ�����ӣ��ܿ�ͳ���pgserver��max-connection��
					logger.info(e.getMessage());
					errors.add(e.getMessage());
					try {
						DerbyUtil.insertBatch2(redo);
						redo.clear();
						conn = DBUtil.getNewConnection(C3P0Util.POSTGRES);
						if (conn != null) {
							conn.setAutoCommit(false);
							String sql = makeSql(toSchema, toTable);
							ps = conn.prepareStatement(sql);
						}
					} catch (SQLException | IOException e1) {
						logger.error(e.getMessage());
						errors.add(e.getMessage());
					}
				}
			}
		}
		try {
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
		Date end = new Date();
		long result = end.getTime() - start.getTime();
		logger.info("�߳�" + Thread.currentThread().getName() + "������ ��ʱ��" + result + "ms");
		errors.clear();
		tableDetail.baseInfo.causes.addAll(errors);
	}

	public String makeSql(String schema, String table) {
		String sql = "insert into " + schema + "." + table + "(";
		for (int i = 0; i < Lock.fieldNum4SomeTable.get() - 1; i++) {
			sql += Lock.fieldsName.get(i) + ",";
		}
		sql += Lock.fieldsName.get(Lock.fieldsName.size() - 1);

		sql += ")" + " values(";
		for (int i = 0; i < Lock.fieldNum4SomeTable.get() - 1; i++) {
			if (Lock.fieldsType.get(i).equals("xml")) {
				sql += "XML(?),";
			} else {
				sql += "?,";
			}
		}
		if (Lock.fieldsType.get(Lock.fieldsType.size() - 1).equals("xml")) {
			sql += "XML(?))";
		} else {
			sql += "?)";
		}
		return sql;
	}

}
