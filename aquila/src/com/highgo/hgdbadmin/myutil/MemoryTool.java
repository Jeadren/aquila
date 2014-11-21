package com.highgo.hgdbadmin.myutil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.highgo.hgdbadmin.vthread.Record;
import com.javamex.classmexer.MemoryUtil;
import com.javamex.classmexer.MemoryUtil.VisibilityFilter;

public class MemoryTool {
	
	
	private static Logger logger = Logger.getLogger(MemoryTool.class);
	public static int BUFFERSIZE = 1024;
	public static int BATCHSIZE = 60;
	
	private static int getRecordNum(int mb, List<Record> records) {
		/**
		 * sum��ֵҪ����Ϊ1��������ó�0�Ļ��������ڼ������Ĵ�С��ʱ�� ���ܶ��ǱȽ�С���ַ���������Instrumentation�ڼ����ַ����Ĵ�Сʱ�Ǵ�Լ��ֵ����ЩС���ַ�����������ܶ���0��
		 * �����������sum��ֵ����0�����sumҪ�ڳ�����λ�ã�Ϊ0�Ļ����ͻ���ֳ���0�������
		 */
		int sum = 1;
		for (Record record : records) {
			sum += MemoryUtil.deepMemoryUsageOf(record, VisibilityFilter.ALL);
		}
		if(records.size()==0){
			return mb * 1024 * 1024 / (sum / 1);	
		}else{
			return mb * 1024 * 1024 / (sum / records.size());
		}
	}

	public static int getRecordNumInMemory(String schema, String table) {
		List<Record> records = null;
		try {
			Connection conn = C3P0Util.getInstance().getConnection("source");
			String sql = "select top(10) * from " + schema + "." + table;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnNum = rsmd.getColumnCount();
			records = new  LinkedList<>();
			while (rs.next()) {
				records.add(new Record(rs,columnNum));
			}
			rs.close();
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			return -1;
		}
		return getRecordNum(BUFFERSIZE, records);
	}
	
	public static int getBatch(int recNum){
		int per = BUFFERSIZE*1024*1024/recNum;
		int batch = 60*1024*1024/per;
		if(batch>1000)
			batch = 1000;
		return batch;
	}
	
	public static int getCoreNum() {
		return Runtime.getRuntime().availableProcessors();
	}

	public static void main(String[] args) throws SQLException {
		// List<Record> recs = new LinkedList<>();
		// for(int i=0;i<10;i++){
		// recs.add(new Record());
		// }
		// System.out.println(getRecordNum(1024,recs));
		//
//		System.out.println(getCoreNum());
		Date start = new Date();
		int bufferSize = getRecordNumInMemory("dbo","measurement");
		System.out.println(bufferSize);
		Date end = new Date();
		System.out.println((end.getTime()-start.getTime())/1000+"s");
		System.out.println(getBatch(bufferSize));
	}
}