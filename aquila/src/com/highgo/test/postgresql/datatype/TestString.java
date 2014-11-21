package com.highgo.test.postgresql.datatype;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * getCharacterStreamû��ʵ��
 * 
 * @author u
 */
public class TestString {

	public static void main(String[] args) {
//		writeTest();
		 readTest();
	}

	public static void writeTest() {
		try {
			ComboPooledDataSource cpds = new ComboPooledDataSource("postgres");
			Connection conn = cpds.getConnection();
			PreparedStatement ps = conn.prepareStatement("insert into teststring(field1,field2,field3) values(?,?,?)");
			for (int i = 0; i < 100; i++) {
				// ps.setString(1, ""+i); //char = string okay
				// ps.setString(2, ""+i); //varchar =string okay
				// ps.setString(3, ""+i); //text = string okay

				// ps.setCharacterStream(1, new StringReader("" + i)); //
				// setCharacterStream unimplements
				// ps.setCharacterStream(2, new StringReader("" + i)); //
				// setCharacterStream unimplements
				// ps.setCharacterStream(3, new StringReader("" + i)); //
				// setCharacterStream unimplements

				/**
				 * ���ַ�ʽ����
				 */
				// ps.setObject(2, i);
				// ps.setObject(1, i);
				// ps.setObject(3, i);
				
				ps.setObject(1, i, Types.CHAR);
				ps.setObject(2, i, Types.VARCHAR);
				ps.setObject(3, i, Types.LONGVARCHAR);
				
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
			PreparedStatement ps = conn.prepareStatement("select * from  teststring");
			ResultSet rs = ps.executeQuery();

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for (int m = 1; m <= columnCount; m++) {
				String columnName = rsmd.getColumnName(m);
				String columntype = rsmd.getColumnTypeName(m);
				int columntypen = rsmd.getColumnType(m);
				System.out.println(columnName + ":" + columntype + ":" + columntypen);
			}

			int i = 0;
			while (rs.next()) {

				/**
				 * ���ַ�ʽ����
				 */
				// String field1 = rs.getString(1);
				// String field2 = rs.getString(2);
				// String field3 = rs.getString(3);

				/**
				 * ���ַ�ʽ����
				 */
				Object field1 = rs.getObject(1);
				Object field2 = rs.getObject(1);
				Object field3 = rs.getObject(1);
				
				/**
				 * ���ַ�ʽò�ƶ�����
				 */
//				String field1 = rs.getObject(1, String.class);
//				String field2 = rs.getObject(2, String.class);
//				String field3 = rs.getObject(3, String.class);

				System.out.println(field1.getClass().getName());
				System.out.println(field2.getClass().getName());
				System.out.println(field3.getClass().getName());

				System.out.println("record" + i++ + ":field1=" + field1 + " field2=" + field2 + " field3=" + field3);
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
