package com.highgo.hgdbadmin.log;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import com.highgo.hgdbadmin.myutil.DBUtil;
import com.highgo.hgdbadmin.reportable.TableDetail;
import com.highgo.hgdbadmin.vthread.Record;

/**
 * ����̵߳���Ҫ���� 1.�����һ��Object 2.�����Object���뵽Pg�� 3.����ɹ���ɾ�����Object������ʧ�ܣ�д���ļ���
 * 
 * @author u
 *
 */
public class ReInsert implements Runnable {
	private Pair record = null;
	private long sleepTime = 100;
	private CountDownLatch startGate;
	private CountDownLatch endGate;
	private TableDetail tableDetail;

	public ReInsert(CountDownLatch endGate, CountDownLatch startGate, TableDetail tableDetail) {
		this.startGate = startGate;
		this.endGate = endGate;
		this.tableDetail = tableDetail;
	}

	@Override
	public void run() {
		try {
			this.startGate.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			doAction();
		} catch (ClassNotFoundException | SQLException | IOException e) {
			e.printStackTrace();
		} finally {
			this.endGate.countDown();
		}

	}

	public void doAction() throws ClassNotFoundException, SQLException, IOException {
		AtomicLong al = new AtomicLong(0);
		/**
		 * ����л��ŵ��߳�||Derby�������� lives() > 1,�л��ŵ��̣߳���ʱ����Derby��û�����ݣ�����Ҫ�̲߳��ܽ�����Ҫһֱ���
		 * DerbyUtil.getSize2() != 0��˵��Derby�������ݣ����ʱ�����ǲ������߳������У���Ҫ��������
		 */

		while (lives() > 1 || DerbyUtil.getSize2() != 0) {// �����̻߳��ţ����Ÿ�,���1�Ǳ��߳��Լ�
			if (Constant.ROWSNUM.get() != 0) {
				try {
					record = DerbyUtil.get2();
				} catch (ClassNotFoundException | SQLException | IOException e) {
				}
				Record r = record.get();
				try {
					DBUtil.insert(r);
					// �����������λ�ü��侫�⣬���insert�ɹ��������д����ִ�У�������ɹ������д���Ͳ�ִ���ˡ�

				} catch (SQLException e) {
					al.decrementAndGet();
					// TODO
					// ������ʧ�ܵĶ���д�뵽�����ļ��У�Ҫ���������󳬹�һ�����������ͽ����û�����Ǩ�ƣ�
					// ͬʱ���û�һ���Ľ��飬�������ݿ����Ӽ�飬��ģʽ���޸ĵȵ�
					if (e instanceof SQLException) {
						try {
							Log.writeObject(r);
						} catch (IOException e1) {
						}
					}
				}
			} else {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
				}
			}
		}
		this.tableDetail.totalDerby = al.get();
	}

	private long lives() {
		return this.endGate.getCount();
	}
}
