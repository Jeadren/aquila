package com.highgo.test.postgresql.datatype;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * ps.setBinaryStream(parameterIndex, x);  �������δ��ʵ��
 * ps.setBytes(parameterIndex, x);
 * �������ǿ��Ե� 
 * @author u
 *
 */
public class TestBinaryString {
	
	public static List<Object> list = new LinkedList<>();
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		readTest();
		writeTest();
//		String[] strs = new String[10];
//		System.out.println(strs.getClass().getName());
//		byte[] bytes = new byte[10];
//		System.out.println(bytes.getClass().getName());
		
		
		
		
		
	}
	
	public static void writeTest() throws UnsupportedEncodingException {
		try {
			ComboPooledDataSource cpds = new ComboPooledDataSource("postgres");
			Connection conn = cpds.getConnection();
			PreparedStatement ps = conn
					.prepareStatement("insert into testbinary(field1) values(?)");
			for (int i = 0; i < 100; i++) {
//				ps.setBinaryStream(parameterIndex, x);   okay
//				ps.setBytes(parameterIndex, x);        okay 
//				ps.setCharacterStream(parameterIndex, reader);    no  ��������дnvarchar�ģ������ַ���������
//				ps.setNCharacterStream(parameterIndex, value);	  no  ��������дnvarchar�ģ������ַ���������
				
//				ps.setBytes(1, (i+"").getBytes() );  //bytea = byte[]  okay
//				ps.execute();
			}
			
			for(Object o : list){
				ps.setObject(1, o ,Types.BINARY);
				ps.execute();
			}
			
			ps.close();
			conn.close();
			cpds.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void readTest() {
		try {
			ComboPooledDataSource cpds = new ComboPooledDataSource("postgres");
			Connection conn = cpds.getConnection();
			PreparedStatement ps = conn.prepareStatement("select * from  testbinary");
			ResultSet rs = ps.executeQuery();
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for(int m = 1 ;m <= columnCount; m++){
				String columnName = rsmd.getColumnName(m);
				String columntype = rsmd.getColumnTypeName(m);
				int columntypen = rsmd.getColumnType(m);
				System.out.println(columnName + ":" +columntype+":"+columntypen);
			}
			
			
			int i = 0;
			while (rs.next()) {
				/**
				 * ���ַ�ʽ����
				 */
//				String field1 = new String(rs.getBytes(1));
				Object field1 = rs.getObject(1);
				list.add(field1);
				System.out.println("record" + i++ + ":field1=" + field1);
			}
			rs.close();
			ps.close();
			conn.close();
			cpds.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
