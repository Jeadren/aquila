package com.highgo.hgdbadmin.log;

import java.util.concurrent.atomic.AtomicLong;

public class Constant {
	// �����ݿ�ʱ��id�ĳ�ʼֵ
	public static AtomicLong READNEXTID = new AtomicLong(1);
	// д���ݿ�ʱ��Id�ĳ�ʼֵ�������ĳ�ʼֵһ��
	public static AtomicLong WRITENEXTID = new AtomicLong(1);
	// ά��Derby���ݿ��еļ�¼������
	public static AtomicLong ROWSNUM = new AtomicLong(0);
	// ��ǰ���ŵ��̸߳���
	@Deprecated
	public static AtomicLong THREADNUM = new AtomicLong(0);

}
