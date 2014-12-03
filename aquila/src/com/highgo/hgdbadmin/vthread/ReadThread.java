package com.highgo.hgdbadmin.vthread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.highgo.hgdbadmin.myutil.C3P0Util;
import com.highgo.hgdbadmin.myutil.ShellEnvironment;
import com.highgo.hgdbadmin.reportable.ReportRecord;
import com.highgo.hgdbadmin.reportable.TableDetail;

public class ReadThread implements Runnable {

	private static Logger logger = Logger.getLogger(ReadThread.class);
	private CountDownLatch startGate;
	private CountDownLatch endGate;
	private Buffer<Record> buffer;

	private String schema;
	private String table;
	private TableDetail tableDetail;
	

	public ReadThread(CountDownLatch startGate, CountDownLatch endGate, Buffer<Record> buffer, String schema,
			String table,TableDetail tableDetail) {
		this.startGate = startGate;
		this.endGate = endGate;
		this.buffer = buffer;
		this.schema = schema;
		this.table = table;
		this.tableDetail = tableDetail;
	}

	@Override
	public void run() {
		try {
			startGate.await();
			doAction();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			endGate.countDown();
		}

	}

	public void doAction() throws InterruptedException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Date start = new Date();
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

			String sql = "select * from " + schema + "." + table;
			logger.info(sql);

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			//��һ�η���MainThread��˳��ִ��
			/**
			 * ResultSetMetaData rsmd = rs.getMetaData(); int columnNum =
			 * rsmd.getColumnCount();
			 * 
			 * Lock.fieldsName.clear(); Lock.fieldsType.clear();
			 * 
			 * for (int i = 1; i <= columnNum; i++) {
			 * Lock.fieldsName.add(rsmd.getColumnName(i));
			 * Lock.fieldsType.add(rsmd.getColumnTypeName(i)); }
			 * Lock.CURRENT_TABLE = (schema + "." + table).toUpperCase();
			 * Lock.fieldNum4SomeTable.set(columnNum);
			 * 
			 * logger.info("Lock.fieldNum4SomeTable:" +
			 * Lock.fieldNum4SomeTable); logger.info("Lock.fieldsName:" +
			 * Lock.fieldsName); logger.info("Lock.fieldsType:" +
			 * Lock.fieldsType);
			 **/
			
			Lock.cdl.countDown();// ����д�߳�

			while (rs.next()) {
				// ReportRecord����
				ReportRecord.NUMBER_READER.incrementAndGet();
				buffer.put(new Record(rs));
			}
			tableDetail.totalRead = ReportRecord.NUMBER_READER.get();
			ReportRecord.NUMBER_READER.set(0L);//�����˽�������
					
			/**
			 * WriteThread������������Lock.isComeOn ==
			 * false(ReadThread2����)&&buffer.size==0(��������û��������)��
			 * ������finally�����������д��룬����Ͳ�Ҫ�ˣ�Ϊ�˱��ּ��䣬���������һ�У���ʵ�Ѿ�û�����ˡ�
			 */
			Lock.isComeOn.set(false);

			rs.close();
			ps.close();
			conn.close();

			tableDetail.baseInfo.causes.clear();
			
			Date end = new Date();
			long result = end.getTime() - start.getTime();
			logger.info("�߳�" + Thread.currentThread().getName() + "������ ��ʱ��" + result + "ms");
		} catch (SQLException e) {
			String message = "Table " + schema + "." + table + " has something wrong in migrating! " + e.getMessage();
			tableDetail.baseInfo.causes.add(message);
			ShellEnvironment.println(message);
			logger.error(message);
		} finally {
			Lock.isComeOn.set(false);// ����仰������ͺ���
			C3P0Util.getInstance().close(rs);
			C3P0Util.getInstance().close(ps);
			C3P0Util.getInstance().close(conn);
		}
	}
}
