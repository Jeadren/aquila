package com.highgo.hgdbadmin.transfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.groovy.tools.shell.Groovysh;

import com.highgo.hgdbadmin.model.Schema;
import com.highgo.hgdbadmin.myutil.C3P0Util;
import com.highgo.hgdbadmin.myutil.ShellEnvironment;
import com.highgo.hgdbadmin.reportable.Constant;
import com.highgo.hgdbadmin.reportable.ObjectInfo;
import com.highgo.hgdbadmin.reportable.ObjectTable;
import com.highgo.hgdbadmin.reportable.ReportInstance;
import com.highgo.hgdbadmin.reportable.SchemaDetail;

public class SchemaTransfer {
	private static Logger logger = Logger.getLogger(SchemaTransfer.class);

	/**
	 * 
	 * @param schemastr
	 * @return
	 
	public static boolean createSchema(String schemastr) {
		logger.info("createSchema");

		ObjectTable schemaTable = ReportInstance.getObjectTable(Constant.SCHEMA);
				
		ObjectInfo objectInfo = new ObjectInfo();
		objectInfo.objectName = schemastr;
		objectInfo.isSuccessed = true;
		
		SchemaDetail sd = new SchemaDetail();

		Schema schema = new Schema(schemastr, null);
		sd.source = schema;
		
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			stmt = conn.createStatement();
			String sql = schema.toSql();
			
			logger.info(sql);
			objectInfo.sqlGenerated = sql;
			schemaTable.sum++;
			
			stmt.executeUpdate(sql);
			
			schemaTable.numSuccessed++;
			logger.info("create schema " + schema.name + "successfully");
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());

			schemaTable.numFailed++;
			objectInfo.causes.add(e.getMessage());
			objectInfo.isSuccessed = false;
			
			return false;
		} finally {
			C3P0Util.getInstance().close(stmt);
			C3P0Util.getInstance().close(conn);
			
			objectInfo.sourceDifinition = "";
			sd.baseInfo = objectInfo;
			schemaTable.rows.add(objectInfo);
			ReportInstance.report.schemas.add(sd);
		}
		return true;
	}
*/
	
	
	public static boolean createSchema(String schema){
		Schema sch = new Schema(schema, null);
		return createSchema(sch);
	}
	
	/**
	 * �������schema�����ֱ����create schema schema.name�Ϳ����ˣ����ض��û��´�����schema�Զ���Ȩ����ǰ�û�
	 * PostgreSQL�� �����û���postgres���� select * from information_schema.schemata
	 * ��ѯ�����Բ�ѯ����ǰ���ݿ�ϵͳ�����������ݿ�����е�schema ��ͨ�û���ѯֻ�ܲ�ѯ����ǰ�û���schema�� SqlServer��
	 * ����ʲô�û� select * from information_schema.schemata ���ܲ鿴��ǰϵͳ�����е�schema
	 * 
	 * Schema�����Խ��٣�����ʱֻ��ҪSchema��Name�Ϳ��ԡ�
	 * 
	 * @param dest
	 * @param schema
	 * @return
	 * @throws SQLException
	 */
	public static boolean createSchema(Schema schema) {
		logger.info("createSchema");

		ObjectTable schemaTable = ReportInstance.getObjectTable(Constant.SCHEMA);
		
		ObjectInfo objectInfo = new ObjectInfo();
		objectInfo.objectName = schema.name;
		objectInfo.isSuccessed =true;
		
		SchemaDetail sd = new SchemaDetail();
		sd.source = schema;
		
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			stmt = conn.createStatement();
			String sql = schema.toSql();

			objectInfo.sqlGenerated = sql;
			logger.info(sql);
			schemaTable.sum++;
			
			stmt.executeUpdate(sql);
			schemaTable.numSuccessed++;
			logger.info("create schema " + schema + " successfully");
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());

			schemaTable.numFailed++;
			objectInfo.causes.add(e.getMessage());
			objectInfo.isSuccessed = false;

			return false;
		} finally {
			C3P0Util.getInstance().close(stmt);
			C3P0Util.getInstance().close(conn);
			
			objectInfo.sourceDifinition = "";
			
			sd.baseInfo = objectInfo;
			ReportInstance.getObjectTable(Constant.SCHEMA).rows.add(objectInfo);
		}
		return true;
	}

	/**
	 * SqlServer�� ����ʲô�û� select * from information_schema.schemata
	 * ���ܲ鿴��ǰϵͳ�����е�schema ���Բ��ܸ�Ǩ�ƹ�������ʲô���ĵ�¼�û��� select * from
	 * information_schema.schemata ����ϵͳ�����е�schema��������� ��Ȼ��SCHEMA_OWNER���Կ��ƣ�����
	 * select * from information_schema.schemata where SCHEMA_OWNER=��
	 * ���ǲ��ܱ�֤һ�����ݿ���ʹ���˶��Schema
	 * 
	 * @param source
	 * @return
	 * @throws SQLException
	 */
	public static List<Schema> fetchSchemasFromSqlServer() {
		logger.info("fetchSchemasFromSqlServer");
		Set<Schema> set = new HashSet<Schema>();
		List<Schema> list = new LinkedList<>();
		String sql = "SELECT TABLE_SCHEMA FROM INFORMATION_SCHEMA.TABLES";
		logger.info(sql);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				set.add(new Schema(rs.getString("TABLE_SCHEMA"), null));
			}
			list.addAll(set);
			logger.info("SCHEMAS:" + list);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return null;
		} finally {
			C3P0Util.getInstance().close(rs);
			C3P0Util.getInstance().close(ps);
			C3P0Util.getInstance().close(conn);
		}
		return list;
	}
	
	public static void main(String[] args) {
		
		System.setProperty("groovysh.prompt", "aquila");
		Groovysh shell = new Groovysh();
		ShellEnvironment.setIo(shell.getIo());
		
		SchemaTransfer.createSchema("dbo");
		ObjectTable  ot = ReportInstance.getObjectTable(Constant.SCHEMA);
//		for(ObjectInfo oi : ot.rows){
//			System.out.println(oi);
//		}
		
		System.out.println(ReportInstance.report);
	}
}
