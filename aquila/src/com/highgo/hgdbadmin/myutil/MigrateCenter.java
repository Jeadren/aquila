package com.highgo.hgdbadmin.myutil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.highgo.hgdbadmin.model.Column;
import com.highgo.hgdbadmin.model.ConstraintCK;
import com.highgo.hgdbadmin.model.ConstraintFK;
import com.highgo.hgdbadmin.model.ConstraintPK;
import com.highgo.hgdbadmin.model.ConstraintUK;
import com.highgo.hgdbadmin.model.Function;
import com.highgo.hgdbadmin.model.Index;
import com.highgo.hgdbadmin.model.Procedure;
import com.highgo.hgdbadmin.model.Schema;
import com.highgo.hgdbadmin.model.Sequence;
import com.highgo.hgdbadmin.model.Table;
import com.highgo.hgdbadmin.model.Trigger;
import com.highgo.hgdbadmin.model.View;
import com.highgo.hgdbadmin.vthread.MainThread;

@Deprecated
public class MigrateCenter {

	private static Logger logger = Logger.getLogger(MigrateCenter.class);

	/**
	 * ���ݿ��Ǩ��˳��ȽϹ̶�������schema����ṹ����ͼ�����ݡ�������Procedure��Trigger��Function��Sequence��
	 * Constraint��˳��Ǩ�ƣ����ⲻ�� Լ����Լ�������֣�ֻҪ�����󴴽�����û�����⣬������ܻ���������������ΨһԼ��
	 * 
	 * @param cpds
	 * @param cpds2
	 * @param schemas
	 * @param tables
	 * @param sequences
	 * @param views
	 * @param indexes
	 * @param procs
	 * @param funcs
	 * @param triggers
	 * @param cks
	 * @param pks
	 * @param uks
	 * @param fks
	 * @return
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public static boolean carryDatabase(List<Schema> schemas, List<Table> tables, List<View> views,
			List<Index> indexes, List<Procedure> procs, List<Trigger> triggers, List<Function> funcs,
			List<Sequence> sequences, List<ConstraintCK> cks, List<ConstraintPK> pks, List<ConstraintUK> uks,
			List<ConstraintFK> fks) throws InterruptedException {

		// migrate schema
		for (Schema schema : schemas) {
			createSchema(schema);
		}
		// migrate table schema
		for (Table table : tables) {
			createTable(table);
		}

		// migrate view
		for (View view : views) {
			createView(view);
		}

		// migrate data
		for (Table table : tables) {
			int recInMemory = MemoryTool.getRecordNumInMemory(table.schema, table.name);
			int threadnum = MemoryTool.getCoreNum();
			int batchSize = MemoryTool.getBatch(recInMemory) / 2;
			logger.info(table.schema.toUpperCase() + "." + table.name.toUpperCase() + " in thread num:"
					+ " batch size:" + batchSize);
			MainThread.migrateTable(table.schema, table.name, threadnum, recInMemory, batchSize);
		}
		// migrate index
		for (Index index : indexes) {
			createIndex(index);
		}

		// pass Procedure/Trigger/Function

		// migrate sequence
		for (Sequence sequence : sequences) {
			createSequence(sequence.schema, sequence.name);
		}

		// migrate Check Constraint
		for (ConstraintCK ck : cks) {
			MigrateConstraint.createConstraintCK(ck.cName);
		}
		// migrate Primary Key
		for (ConstraintPK pk : pks) {
			MigrateConstraint.createConstraintPK(pk.cName);
		}
		// migrate Unique key
		for (ConstraintUK uk : uks) {
			MigrateConstraint.createConstraintUK(uk.cName);
		}
		// migrate Foreign Key
		for (ConstraintFK fk : fks) {
			MigrateConstraint.createConstraintFK(fk.cName);
		}
		return true;
	}

	public static void carryTablesDefinition(List<Table> tables) {
		for (Table table : tables) {
			createTable(table);
		}
	}

	public static boolean createSchema(String schemastr) {
		logger.info("createSchema");
		Schema schema = new Schema(schemastr, null);
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			stmt = conn.createStatement();
			String sql = schema.toSql();
			logger.info(sql);
			stmt.executeUpdate(sql);
			logger.info("create schema " + schema.name + "successfully");
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(stmt);
			C3P0Util.getInstance().close(conn);
		}
		return true;
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
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			stmt = conn.createStatement();
			String sql = schema.toSql();
			logger.info(sql);
			stmt.executeUpdate(sql);
			logger.info("create schema " + schema + " successfully");
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(stmt);
			C3P0Util.getInstance().close(conn);
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
			rs.close();
			ps.close();
			conn.close();
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

	/**
	 * ��ͨ��ʹ������ı�������
	 * 
	 * @param source
	 * @param dest
	 * @param schema
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	public static boolean createTable(String schema, String table) {
		logger.info("createTable");
		Table t = new Table(schema, table, "BASE TABLE");
		Connection conn = null;
		Statement st = null;
		try {
			t.columns = fetchColumnsForATable(schema, table);
			t.keys = fetchPrimaryKeysForATable(schema, table);
			String sql = t.toSql();
			logger.info(sql);
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn.createStatement();
			st.execute(sql);
			logger.info("create table " + schema + "." + table + " successfully");
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return true;
	}

	public static boolean createTable(Table table) {
		String sql = table.toSql();
		logger.info(sql);
		Connection conn = null;
		Statement st = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn.createStatement();
			st.execute(sql);
			logger.info("create table " + table.schema + "." + table.name + " successfully");
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return true;
	}

	public static boolean deleteAllData(String schema, String table) {
		String sql = "delete from " + schema + "." + table;
		logger.info(sql);
		Connection conn = null;
		Statement st = null;

		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn.createStatement();
			st.execute(sql);
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
		} finally {
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}

		return true;
	}

	/**
	 * fetch table information from sqlserver except view information
	 * ��ȡ��Connection��������ݿ��µı� �����������Ҫ�����ǿ�������������û�����table
	 * 
	 * @param source
	 * @return
	 * @throws SQLException
	 */
	public static Table fetchATableFromSqlServer(String schema, String table) {
		logger.info("fetchATableFromSqlServer");
		Table tab = null;
		String sql = "SELECT TABLE_SCHEMA, TABLE_NAME,TABLE_TYPE FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE' AND TABLE_SCHEMA=? AND TABLE_NAME=?";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps = conn.prepareStatement(sql);
			ps.setString(1, schema);
			ps.setString(2, table);
			logger.info(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				tab = new Table(rs.getString("TABLE_SCHEMA"), rs.getString("TABLE_NAME"), rs.getString("TABLE_TYPE"));
			}
			tab.columns = fetchColumnsForATable(schema, table);
			tab.keys = fetchPrimaryKeysForATable(schema, table);
			logger.info("TABLE:" + tab);
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return null;
		} finally {
			C3P0Util.getInstance().close(rs);
			C3P0Util.getInstance().close(ps);
			C3P0Util.getInstance().close(conn);
		}
		return tab;
	}

	/**
	 * fetch table information from sqlserver except view information
	 * ��ȡ��Connection��������ݿ��µı�
	 * 
	 * @param source
	 * @return
	 * @throws SQLException
	 */
	public static List<Table> fetchTableFromSqlServer() {
		logger.info("fetchTableFromSqlServer");
		List<Table> list = new LinkedList<>();
		String sql = "SELECT TABLE_SCHEMA, TABLE_NAME,TABLE_TYPE FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE'";
		logger.info(sql);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				Table tmp = new Table(rs.getString("TABLE_SCHEMA"), rs.getString("TABLE_NAME"),
						rs.getString("TABLE_TYPE"));
				tmp.columns = fetchColumnsForATable(tmp.schema, tmp.name);
				list.add(tmp);
			}
			logger.info("TABLES:" + list);
			rs.close();
			ps.close();
			conn.close();
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

	/**
	 * fetch some table's columns information,for create table at dest database;
	 * 
	 * @param source
	 * @param schema
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	private static List<Column> fetchColumnsForATable(String schema, String table) {
		logger.info("fetchColumnsForATable");
		List<Column> list = new LinkedList<>();
		String sql = "SELECT COLUMN_NAME,COLUMN_DEFAULT,DATA_TYPE,IS_NULLABLE,CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION,NUMERIC_SCALE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? and TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
		logger.info(sql);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps = conn.prepareStatement(sql);
			ps.setString(1, schema);
			ps.setString(2, table);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new Column(rs.getString("COLUMN_NAME"), rs.getString("COLUMN_DEFAULT"), rs
						.getBoolean("IS_NULLABLE"), rs.getString("DATA_TYPE"), rs.getInt("CHARACTER_MAXIMUM_LENGTH"),
						rs.getInt("NUMERIC_PRECISION"), rs.getInt("NUMERIC_SCALE")));
			}
			logger.info("COLUMNS FOR [" + schema.toUpperCase() + "." + table.toUpperCase() + "]:" + list);
			rs.close();
			ps.close();
			conn.close();
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

	private static List<String> fetchPrimaryKeysForATable(String schema, String table) {
		logger.info("fetchPrimaryKeysForATable");
		List<String> list = new LinkedList<>();
		String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA=? and TABLE_NAME = ?";
		logger.info(sql);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps = conn.prepareStatement(sql);
			ps.setString(1, schema);
			ps.setString(2, table);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(rs.getString("COLUMN_NAME"));
			}
			logger.info("PRIMARY KEY FOR [" + schema.toUpperCase() + "." + table.toUpperCase() + "]:" + list);
			rs.close();
			ps.close();
			conn.close();
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

	/**
	 * Pg
	 * 
	 * @param source
	 * @param dest
	 * @param schema
	 * @param seqname
	 * @return
	 * @throws SQLException
	 */
	public static boolean createSequence(String schema, String seqname) {
		logger.info("createSequence");
		Connection conn = null;
		Statement st = null;
		try {
			Sequence seq = fetchSequencesFromSqlServer(schema, seqname);
			if (seq == null) {
				return false;
			}
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn.createStatement();
			String sql = seq.toSql();
			logger.info(sql);
			st.execute(sql);
			logger.info("create sequence " + schema + "." + seqname + " successfully");
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return true;
	}

	/**
	 * list all sequence in various schema
	 * 
	 * @param source
	 * @return
	 * @throws SQLException
	 */
	public static List<Sequence> fetchSequencesFromSqlServer() {
		logger.info("fetchSequencesFromSqlServer");
		List<Sequence> list = new LinkedList<>();
		String sql = "SELECT SYS.schemas.name as SEQUENCE_SCHEMA,SYS.SEQUENCES.NAME as SEQUENCE_NAME,CAST(CURRENT_VALUE AS VARCHAR) START,CAST(MINIMUM_VALUE AS VARCHAR) MINIMUM,CAST(MAXIMUM_VALUE AS VARCHAR) MAXIMUM,CAST(INCREMENT AS VARCHAR) INCREMENTT,is_cycling as CYCLE_OPTION FROM SYS.SEQUENCES,SYS.schemas WHERE SYS.SEQUENCES.SCHEMA_ID = SYS.schemas.SCHEMA_ID";
		logger.info(sql);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new Sequence(rs.getString("SEQUENCE_SCHEMA"), rs.getString("SEQUENCE_NAME"), Long.parseLong(rs
						.getString("START")), Long.parseLong(rs.getString("MINIMUM")), Long.parseLong(rs
						.getString("MAXIMUM")), Integer.parseInt(rs.getString("INCREMENTT")), rs.getInt("CYCLE_OPTION")));
			}
			logger.info("SEQUENCES:" + list);
			rs.close();
			ps.close();
			conn.close();
		} catch (NumberFormatException | SQLException e) {
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

	/**
	 * CAST(CURRENT_VALUE AS VARCHAR)
	 * START������˼�ǣ�����Ԫ���ݿ��еĵ�ǰֵ������Ŀ�����ݿ��е���ʼֵ����ʵӦ���ǵ�ǰֵ��1������ٸ�
	 * 
	 * @param source
	 * @param schema
	 * @return
	 * @throws SQLException
	 */
	public static List<Sequence> fetchSequencesFromSqlServer(String schema) {
		logger.info("fetchSequencesFromSqlServer");
		List<Sequence> list = new LinkedList<>();
		String sql = "SELECT SYS.schemas.name as SEQUENCE_SCHEMA,SYS.SEQUENCES.NAME as SEQUENCE_NAME,CAST(CURRENT_VALUE AS VARCHAR) START,CAST(MINIMUM_VALUE AS VARCHAR) MINIMUM,CAST(MAXIMUM_VALUE AS VARCHAR) MAXIMUM,CAST(INCREMENT AS VARCHAR) INCREMENTT,is_cycling as CYCLE_OPTION FROM SYS.SEQUENCES,SYS.schemas WHERE SYS.SEQUENCES.SCHEMA_ID = SYS.schemas.SCHEMA_ID AND SYS.schemas.name =?";
		logger.info(sql);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps = conn.prepareStatement(sql);
			ps.setString(1, schema);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new Sequence(rs.getString("SEQUENCE_SCHEMA"), rs.getString("SEQUENCE_NAME"), Long.parseLong(rs
						.getString("START")), Long.parseLong(rs.getString("MINIMUM")), Long.parseLong(rs
						.getString("MAXIMUM")), Integer.parseInt(rs.getString("INCREMENTT")), rs.getInt("CYCLE_OPTION")));
			}
			logger.info("SEQUENCES IN SCHEMA[" + schema.toUpperCase() + "]:" + list);
			rs.close();
			ps.close();
			conn.close();
		} catch (NumberFormatException | SQLException e) {
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

	/**
	 * CAST(CURRENT_VALUE AS VARCHAR) START������˼�ǣ�����Ԫ���ݿ��еĵ�ǰֵ������Ŀ�����ݿ��е���ʼֵ
	 * 
	 * @param source
	 * @param schema
	 * @return
	 * @throws SQLException
	 */
	public static Sequence fetchSequencesFromSqlServer(String schema, String name) {
		logger.info("fetchSequencesFromSqlServer");
		Sequence sequence = null;
		String sql = "SELECT SYS.schemas.name as SEQUENCE_SCHEMA,SYS.SEQUENCES.NAME as SEQUENCE_NAME,CAST(CURRENT_VALUE AS VARCHAR) START,CAST(MINIMUM_VALUE AS VARCHAR) MINIMUM,CAST(MAXIMUM_VALUE AS VARCHAR) MAXIMUM,CAST(INCREMENT AS VARCHAR) INCREMENTT,is_cycling as CYCLE_OPTION FROM SYS.SEQUENCES,SYS.schemas WHERE SYS.SEQUENCES.SCHEMA_ID = SYS.schemas.SCHEMA_ID AND SYS.schemas.name = ? AND SYS.SEQUENCES.NAME = ?";
		logger.info(sql);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps = conn.prepareStatement(sql);
			ps.setString(1, schema);
			ps.setString(2, name);
			rs = ps.executeQuery();
			while (rs.next()) {
				sequence = new Sequence(rs.getString("SEQUENCE_SCHEMA"), rs.getString("SEQUENCE_NAME"),
						Long.parseLong(rs.getString("START")), Long.parseLong(rs.getString("MINIMUM")),
						Long.parseLong(rs.getString("MAXIMUM")), Integer.parseInt(rs.getString("INCREMENTT")),
						rs.getInt("CYCLE_OPTION"));
			}
			logger.info("SEQUENCE:" + sequence);
			rs.close();
			ps.close();
			conn.close();
		} catch (NumberFormatException | SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return null;
		} finally {
			C3P0Util.getInstance().close(rs);
			C3P0Util.getInstance().close(ps);
			C3P0Util.getInstance().close(conn);
		}
		return sequence;

	}

	/**
	 * Ŀǰ�Ǽ��贴�����table������Schema��ֻ�ܴ���tableName����Schema�Ͷ�����Schema�����������û���ظ������
	 * 
	 * @param source
	 * @param dest
	 * @param schema
	 * @param view
	 * @return
	 * @throws SQLException
	 */
	public static boolean createView(String schema, String view) {
		logger.info("createView");
		Connection conn = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		Connection conn2 = null;
		Statement st = null;
		try {
			View viewv = fetchViewFromSqlServer(schema, view);
			String definition = viewv.toSql();
			if (definition.indexOf(" " + schema + "." + viewv.name + " ") == -1) {
				definition = definition.replaceAll(" " + view + " ", " " + schema + "." + view + " ");
			}
			String sql2 = "SELECT TABLE_SCHEMA,TABLE_NAME FROM INFORMATION_SCHEMA.VIEW_TABLE_USAGE WHERE VIEW_SCHEMA = ? and VIEW_NAME = ?";
			logger.info(sql2);
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps2 = conn.prepareStatement(sql2);
			ps2.setString(1, schema);
			ps2.setString(2, view);
			rs2 = ps2.executeQuery();
			while (rs2.next()) {
				String tableSchema = rs2.getString("TABLE_SCHEMA");
				String tableName = rs2.getString("TABLE_NAME");
				// �����棬��,ǰ���������where������������һ��������where�������
				// create view dbo.TestView4
				// as
				// select * from dbo.TSCHEMA7 where ...
				if (definition.indexOf(" " + tableSchema + "." + tableName + " ") == -1) {
					definition = definition
							.replaceAll(" " + tableName + " ", " " + tableSchema + "." + tableName + " ");
					System.out.println(definition);
					definition = definition
							.replaceAll(" " + tableName + ",", " " + tableSchema + "." + tableName + ",");
					System.out.println(definition);
					definition = definition
							.replaceAll("," + tableName + " ", "," + tableSchema + "." + tableName + " ");
					System.out.println(definition);
					definition = definition.replaceAll(" " + tableName, " " + tableSchema + "." + tableName);
					System.out.println(definition);
					definition = definition.replaceAll("," + tableName, "," + tableSchema + "." + tableName);
				}
			}
			rs2.close();
			ps2.close();
			conn.close();
			conn2 = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn2.createStatement();
			logger.info(definition);
			st.execute(definition);
			logger.info("create view " + viewv + " successfully");
			st.close();
			conn2.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(rs2);
			C3P0Util.getInstance().close(ps2);
			C3P0Util.getInstance().close(conn);
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn2);
		}
		return true;
	}

	public static boolean createView(View viewv) {
		String definition = viewv.toSql();
		// ��view�Ķ����е�ViewName�滻��SchemaName.ViewName
		if (definition.contains(" " + viewv.schema + "." + viewv.name + " ")) {
			definition = definition.replaceAll(" " + viewv.name + " ", " " + viewv.schema + "." + viewv.name + " ");
		}
		// ȥ��[��]��SqlServer�о������������������
		if (definition.contains("[") || definition.contains("]")) {
			definition = definition.replace("[", "");
			definition = definition.replace("]", "");
		}
		Connection conn = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		Connection conn2 = null;
		Statement st = null;

		String sql2 = "SELECT TABLE_SCHEMA,TABLE_NAME FROM INFORMATION_SCHEMA.VIEW_TABLE_USAGE WHERE VIEW_SCHEMA = ? and VIEW_NAME = ?";
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps2 = conn.prepareStatement(sql2);
			ps2.setString(1, viewv.schema);
			ps2.setString(2, viewv.name);
			rs2 = ps2.executeQuery();
			while (rs2.next()) {
				String tableSchema = rs2.getString("TABLE_SCHEMA");
				String tableName = rs2.getString("TABLE_NAME");
				// �����棬��,ǰ���������where������������һ��������where�������
				// create view dbo.TestView4
				// as
				// select * from dbo.TSCHEMA7 where ...
				if (definition.indexOf(" " + tableSchema + "." + tableName + " ") == -1) {
					definition = definition
							.replaceAll(" " + tableName + " ", " " + tableSchema + "." + tableName + " ");
					System.out.println(definition);
					definition = definition
							.replaceAll(" " + tableName + ",", " " + tableSchema + "." + tableName + ",");
					System.out.println(definition);
					definition = definition
							.replaceAll("," + tableName + " ", "," + tableSchema + "." + tableName + " ");
					System.out.println(definition);
					definition = definition.replaceAll(" " + tableName, " " + tableSchema + "." + tableName);
					System.out.println(definition);
					definition = definition.replaceAll("," + tableName, "," + tableSchema + "." + tableName);
				}
			}
			rs2.close();
			ps2.close();
			conn.close();
			conn2 = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn2.createStatement();
			logger.info(definition);
			st.execute(definition);
			logger.info("create view " + viewv + " successfully");
			st.close();
			conn2.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(rs2);
			C3P0Util.getInstance().close(ps2);
			C3P0Util.getInstance().close(conn);
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn2);
		}
		return true;
	}

	public static List<View> fetchViewsFromSqlServer() {
		logger.info("fetchViewsFromSqlServer");
		List<View> list = new LinkedList<>();
		List<Schema> schemas = fetchSchemasFromSqlServer();
		Iterator<Schema> iterator = schemas.iterator();
		while (iterator.hasNext()) {
			list.addAll(fetchViewsFromSqlServer(iterator.next().name));
		}
		logger.info("VIEWS:" + list);
		return list;
	}

	public static List<View> fetchViewsFromSqlServer(String schema) {
		logger.info("fetchViewsFromSqlServer");
		List<View> list = new LinkedList<>();
		String sql = "SELECT TABLE_NAME,VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = ?";
		logger.info(sql);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps = conn.prepareStatement(sql);
			ps.setString(1, schema);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new View(schema, rs.getString("TABLE_NAME"), rs.getString("VIEW_DEFINITION")));
			}
			logger.info("VIEWS IN SCHEMA[" + schema.toUpperCase() + "]:" + list);
			rs.close();
			ps.close();
			conn.close();
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

	/**
	 * TABLE_NAME����view��name
	 * 
	 * @param source
	 * @param schema
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public static View fetchViewFromSqlServer(String schema, String name) {
		logger.info("fetchViewFromSqlServer");
		View view = null;
		String sql = "SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
		logger.info(sql);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps = conn.prepareStatement(sql);
			ps.setString(1, schema);
			ps.setString(2, name);
			rs = ps.executeQuery();
			while (rs.next()) {
				view = new View(schema, name, rs.getString("VIEW_DEFINITION"));
			}
			logger.info("VIEW:" + view);
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return null;
		} finally {
			C3P0Util.getInstance().close(rs);
			C3P0Util.getInstance().close(ps);
			C3P0Util.getInstance().close(conn);
		}
		return view;
	}

	/**
	 * index ��Щ�ر���SqlServer�л���ֲ�ͬ����������index�����������Ҫ��index�����ֽ���һ�λ��ң�����֮������֣�
	 * �ı���������������index������������ġ� �������ڴ���������˵��indexName�ͳ���һ����ȷ���Ĳ��������ͳһ���±ߵ�API
	 * 
	 * @param schema
	 * @param tableName
	 * @param indexName
	 * @return
	 */
	public static boolean createIndex(String schema, String tableName, String indexName) {
		logger.info("createIndex");
		Connection conn = null;
		Statement st = null;
		try {
			Index idx = fetchIndexesFromSqlServer(schema, tableName, indexName);
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn.createStatement();
			String sql = idx.toSql();
			logger.info(sql);
			st.execute(sql);
			logger.info("create index " + schema.toUpperCase() + "." + indexName.toLowerCase() + " successfully");
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return true;
	}

	public static boolean createIndex(Index index) {
		logger.info("createIndex");
		Connection conn = null;
		Statement st = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn.createStatement();
			String sql = index.toSql();
			logger.info(sql);
			st.execute(sql);
			logger.info("create index " + index.schema.toUpperCase() + "." + index.name.toLowerCase() + " successfully");
			st.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println(e);
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return true;
	}

	/**
	 * ��ȡһ��������е�������Ϣ
	 * 
	 * @param source
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	@Deprecated
	public static List<Index> fetchIndexesFromSqlServer(Connection source, String table) throws SQLException {
		List<Index> list = new LinkedList<>();
		// ��ѯһ�������������
		String sql = "select sys.objects.object_id as TABLE_ID, sys.indexes.name as INDEX_NAME,sys.schemas.name as SCHEMA_NAMEE,sys.indexes.is_unique as UNIQUEE,sys.indexes.index_id as INDEX_ID from sys.objects,sys.indexes,sys.schemas where sys.objects.name=? and sys.objects.object_id = sys.indexes.object_id and sys.objects.schema_id = sys.schemas.schema_id";
		PreparedStatement ps = source.prepareStatement(sql);
		ps.setString(1, table);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			String indexName = rs.getString("INDEX_NAME");
			if (!(indexName == null || "".equals(indexName))) {
				list.add(new Index(indexName, rs.getString("SCHEMA_NAMEE"), rs.getString("TABLE_ID"), table, rs
						.getBoolean("UNIQUEE"), rs.getInt("INDEX_ID")));
			}
		}

		// ����tableId��indexId��ȡһ��index��������Ϣ
		String sql2 = "select column_id from sys.index_columns where sys.index_columns.object_id = ? and sys.index_columns.index_id = ?";
		for (Index idx : list) {
			ps = source.prepareStatement(sql2);
			ps.setString(1, idx.table);
			ps.setInt(2, idx.indexId);
			rs = ps.executeQuery();
			while (rs.next()) {
				Long columnid = rs.getLong("column_id");
				String sql3 = " select name from sys.index_columns,sys.all_columns where sys.index_columns.object_id = ? and sys.all_columns.object_id = ? and  sys.index_columns.object_id = sys.all_columns.object_id and sys.index_columns.index_id = ? and sys.index_columns.column_id=? and sys.all_columns.column_id=?";
				PreparedStatement ps3 = source.prepareStatement(sql3);
				ps3.setString(1, idx.table);
				ps3.setString(2, idx.table);
				ps3.setLong(3, idx.indexId);
				ps3.setLong(4, columnid);
				ps3.setLong(5, columnid);
				ResultSet rs3 = ps3.executeQuery();
				rs3.next();
				idx.columns.add(rs3.getString("name"));
				rs3.close();
			}
		}
		rs.close();
		return list;
	}

	public static List<Index> fetchIndexesFromSqlServer() {
		logger.info("fetchIndexesFromSqlServer");
		List<Index> indexes = new LinkedList<>();
		List<Table> tables = fetchTableFromSqlServer();
		for (Table table : tables) {
			indexes.addAll(fetchIndexesFromSqlServer4Table(table.schema, table.name));
		}
		logger.info("INDEXES:" + indexes);
		return chaosIndex(indexes);
	}

	private static List<Index> chaosIndex(List<Index> indexes) {
		Set<Index> indexSet = new HashSet<>();
		List<Index> returnList = new LinkedList<Index>();
		for (Index index : indexes) {
			if (indexSet.contains(index)) {
				index.name = index.tableName + "_" + index.name;
			}
			indexSet.add(index);
		}
		returnList.clear();
		returnList.addAll(indexSet);
		return returnList;
	}

	/**
	 * ��ѯһ���������Index
	 * 
	 * @param source
	 * @param tableSchema
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static List<Index> fetchIndexesFromSqlServer4Table(String tableSchema, String tableName) {
		logger.info("fetchIndexesFromSqlServer4Table");
		List<Index> list = new LinkedList<>();
		String sql = "select c.is_unique,c.name,a.TABLE_SCHEMA,e.COLUMN_NAME,c.type from INFORMATION_SCHEMA.TABLES a,sys.all_objects b,sys.indexes c,sys.index_columns d,INFORMATION_SCHEMA.COLUMNS e,sys.schemas F where a.TABLE_NAME=b.name and c.object_id=b.object_id and c.type<>0 and c.object_id=d.object_id and c.index_id=d.index_id and e.TABLE_NAME=a.TABLE_NAME and e.ORDINAL_POSITION=d.column_id AND B.schema_id=F.schema_id AND C.is_primary_key=0 AND C.is_unique_constraint=0 AND a.TABLE_SCHEMA=? AND b.name = ?";
		logger.info(sql);
		// ���ű�����е�Index Name��Index Schema
		Map<String, String> indexes;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps = conn.prepareStatement(sql);
			ps.setString(1, tableSchema);
			ps.setString(2, tableName);
			rs = ps.executeQuery();
			indexes = new HashMap<>();
			while (rs.next()) {
				indexes.put(rs.getString("name"), rs.getString("TABLE_SCHEMA"));
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return null;
		} finally {
			C3P0Util.getInstance().close(rs);
			C3P0Util.getInstance().close(ps);
			C3P0Util.getInstance().close(conn);
		}
		for (String indexName : indexes.keySet()) {
			list.add(fetchIndexesFromSqlServer(indexes.get(indexName), tableName, indexName));
		}
		logger.info("INDEXES IN [" + tableSchema.toUpperCase() + "." + tableName.toUpperCase() + "]:" + list);
		return list;
	}

	/**
	 * ��ѯָ��Name��Index
	 * 
	 * @param source
	 * @param tableSchema
	 * @param indexNamePara
	 * @return
	 * @throws SQLException
	 */
	public static Index fetchIndexesFromSqlServer(String tableSchema, String tableNamePara, String indexNamePara) {
		logger.info("fetchIndexesFromSqlServer");
		Index index = null;
		String sql = "select c.is_unique,c.name,a.TABLE_SCHEMA,a.TABLE_NAME,e.COLUMN_NAME,c.type,d.is_descending_key from INFORMATION_SCHEMA.TABLES a,sys.all_objects b,sys.indexes c,sys.index_columns d,INFORMATION_SCHEMA.COLUMNS e,sys.schemas F where a.TABLE_NAME=b.name and c.object_id=b.object_id and c.type<>0 and c.object_id=d.object_id and c.index_id=d.index_id and e.TABLE_NAME=a.TABLE_NAME and e.ORDINAL_POSITION=d.column_id AND B.schema_id=F.schema_id AND C.is_primary_key=0 AND C.is_unique_constraint=0 AND a.TABLE_SCHEMA=? AND a.TABLE_NAME=? AND c.NAME=?";
		logger.info(sql);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			ps = conn.prepareStatement(sql);
			ps.setString(1, tableSchema);
			ps.setString(2, tableNamePara);
			ps.setString(3, indexNamePara);
			rs = ps.executeQuery();
			boolean isUnique = false;
			String indexName = null;
			String tableSchemat = null;
			String tableName = null;
			List<String> columns = new ArrayList<>();
			List<Boolean> isDescendingKeys = new ArrayList<>();
			while (rs.next()) {
				isUnique = rs.getBoolean("is_unique");
				indexName = rs.getString("name");
				tableSchemat = rs.getString("TABLE_SCHEMA");
				tableName = rs.getString("TABLE_NAME");
				columns.add(rs.getString("COLUMN_NAME"));
				isDescendingKeys.add(rs.getBoolean("is_descending_key"));
			}
			index = new Index(indexName, tableSchemat, tableName, isUnique, columns, isDescendingKeys);
			logger.info("INDEX [" + tableSchema.toUpperCase() + "." + indexNamePara.toUpperCase() + "]:" + index);
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return null;
		} finally {
			C3P0Util.getInstance().close(rs);
			C3P0Util.getInstance().close(ps);
			C3P0Util.getInstance().close(conn);
		}
		return index;
	}

	public static boolean createProcedure(String name) {
		List<Procedure> list = fetchProceduresFromSqlServer();
		Connection conn = null;
		Statement st = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn.createStatement();
			for (Procedure pd : list) {
				if (!pd.name.equals("name")) {
					continue;
				}
				st.execute(pd.toSql());
			}
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return true;
	}

	public static boolean createProcedure() {
		List<Procedure> list = fetchProceduresFromSqlServer();
		Connection conn = null;
		Statement st = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn.createStatement();
			for (Procedure pd : list) {
				st.execute(pd.toSql());
			}
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return true;
	}

	public static List<Procedure> fetchProceduresFromSqlServer() {
		List<Procedure> list = new LinkedList<>();
		String sql = "select name , definition from sys.sql_modules,sys.procedures where sys.sql_modules.object_id = sys.procedures.object_id";
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				list.add(new Procedure(rs.getString("name"), rs.getString("definition")));
			}
			rs.close();
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return null;
		} finally {
			C3P0Util.getInstance().close(rs);
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return list;
	}

	public static boolean createFunction(String name) {
		List<Function> list = fetchFunctionsFromSqlServer();
		Connection conn = null;
		Statement st = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn.createStatement();
			for (Function pd : list) {
				if (!pd.name.toUpperCase().equals(name.toUpperCase())) {
					continue;
				}
				st.execute(pd.toSql());
			}
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return true;
	}

	public static boolean createFunction() {
		List<Function> list = fetchFunctionsFromSqlServer();
		Connection conn = null;
		Statement st = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn.createStatement();
			for (Function pd : list) {
				st.execute(pd.toSql());
			}
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return true;
	}

	public static List<Function> fetchFunctionsFromSqlServer() {
		List<Function> list = new LinkedList<>();
		String sql = "select name ,definition from sys.objects,sys.sql_modules where sys.objects.object_id =  sys.sql_modules.object_id and (type = 'AF' or type = 'FN' or type = 'FS' or type = 'FT' or type = 'IF')";
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				list.add(new Function(rs.getString("name"), rs.getString("definition")));
			}
			rs.close();
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return null;
		} finally {
			C3P0Util.getInstance().close(rs);
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return list;
	}

	public static boolean createTrigger(String name) {
		List<Trigger> list = fetchTriggerFromSqlServer();
		Connection conn = null;
		Statement st = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn.createStatement();
			for (Trigger pd : list) {
				if (!pd.name.equals("name")) {
					continue;
				}
				st.execute(pd.toSql());
			}
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return true;
	}

	public static boolean createTrigger() {
		List<Trigger> list = fetchTriggerFromSqlServer();
		Connection conn = null;
		Statement st = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.POSTGRES);
			st = conn.createStatement();
			for (Trigger pd : list) {
				st.execute(pd.toSql());
			}
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return false;
		} finally {
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return true;
	}

	public static List<Trigger> fetchTriggerFromSqlServer() {
		List<Trigger> list = new LinkedList<>();
		String sql = "select name ,definition from sys.sql_modules,sys.triggers where sys.sql_modules.object_id = sys.triggers.object_id";
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			conn = C3P0Util.getInstance().getConnection(C3P0Util.SOURCE);
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				list.add(new Trigger(rs.getString("name"), rs.getString("definition")));
			}
			rs.close();
			st.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			ShellEnvironment.println(e.getMessage());
			return null;
		} finally {
			C3P0Util.getInstance().close(rs);
			C3P0Util.getInstance().close(st);
			C3P0Util.getInstance().close(conn);
		}
		return list;
	}

	public static void main(String[] args) throws SQLException, InterruptedException {
		/**
		 * // MigrateCenter.createSchema("dbo"); List
		 * <Table>
		 * tables = MigrateCenter.fetchTableFromSqlServer(); //
		 * 
		 * // T_Node_New start // ��SiteDataDisplay start // ��attachment start //
		 * ��SiteDataField start // ��BLOB start Set<String> strs = new
		 * HashSet<>(); strs.add("T_Node_New"); strs.add("SiteDataDisplay");
		 * strs.add("attachment"); strs.add("SiteDataField"); strs.add("BLOB");
		 * 
		 * 
		 * // ��T_DataInfo start strs.add("T_DataInfo"); // null //
		 * ��TemplateDistributionRelation start
		 * strs.add("TemplateDistributionRelation"); // null // ��LabelDataSource
		 * start strs.add("LabelDataSource"); // null // ��LogError start
		 * strs.add("LogError"); // null // ��LabelStyle start
		 * strs.add("LabelStyle"); // null // ��PageTemplate start
		 * strs.add("PageTemplate"); // null // ��UserInfo start
		 * strs.add("UserInfo"); // null // ��TemplateRelationColumn start
		 * strs.add("UserInfo"); // null // ��dataSourceDetails start
		 * strs.add("dataSourceDetails"); // null // ��WaitPublishInfo start
		 * strs.add("WaitPublishInfo"); // null // ��DocumentExtendInfo start
		 * strs.add("DocumentExtendInfo"); // null // ��SiteInfo start
		 * strs.add("SiteInfo"); // null // ��WaitPublishInfo_bak start
		 * strs.add("WaitPublishInfo_bak"); // null // ��SiteMonitorExtUrlInfo
		 * start strs.add("SiteMonitorExtUrlInfo"); // null //
		 * ��SiteMonitExtUrlResult start strs.add("SiteMonitExtUrlResult"); //
		 * null // ��loginLog start strs.add("loginLog"); // null //
		 * ��DocumentBaseInfo_Text start strs.add("DocumentBaseInfo_Text"); //
		 * null strs.add("DistributionInfo"); // ��DistributionInfo start
		 * 
		 * 
		 * strs.add("TemplateRelationColumn");
		 * 
		 * 
		 * // ��RelatedDocument start // null strs.add("RelatedDocument"); //
		 * ��advQuery_userQuery start strs.add("advQuery_userQuery"); // null //
		 * ��T_Node_Error start strs.add("T_Node_Error"); // null //
		 * ��advQuery_fields start strs.add("advQuery_fields"); // null //
		 * ��advQuery_fieldDicts start strs.add("advQuery_fieldDicts"); // null
		 * // ��SiteDistributionRelation start
		 * strs.add("SiteDistributionRelation"); // null // ��LogOperate start
		 * strs.add("LogOperate"); // null // ��ResourceFiles start
		 * strs.add("ResourceFiles"); // null // ��visitInfo start
		 * strs.add("visitInfo"); // null // ��T_ErrorData start
		 * strs.add("T_ErrorData"); // null // ��Test_NodeId start // null
		 * strs.add("Test_NodeId");
		 * 
		 * 
		 * // // for(Table table: tables){ //
		 * MigrateCenter.createTable(table.schema, table.name); // }
		 * 
		 * 
		 * 
		 * for (Table table : tables) { System.out.println(table.toSql());
		 * if(strs.contains(table.name)){ continue; } int recInMemory =
		 * MemoryTool.getRecordNumInMemory(table.schema, table.name);
		 * MainThread.migrateTable(table.schema, table.name,
		 * MemoryTool.getCoreNum(), recInMemory,
		 * MemoryTool.getBatch(recInMemory)); }
		 **/

		// Set<String> viewset = new HashSet<>();
		// viewset.add("View_DocumentInfo");
		//
		// List<View> views = MigrateCenter.fetchViewsFromSqlServer();
		// for (View view : views) {
		// if(viewset.contains(view.name)){
		// continue;
		// }
		// System.out.println(view.toSql());
		// createView(view.schema, view.name);
		// }

		// MigrateCenter.createTable("dbo", "BLOB");

		// List<Table> tables = MigrateCenter.fetchTableFromSqlServer();
		// for (Table table : tables) {
		// if(table.name.equals("BLOB")){
		// int recInMemory = MemoryTool.getRecordNumInMemory(table.schema,
		// table.name);
		// MainThread.migrateTable(table.schema, table.name,
		// MemoryTool.getCoreNum(), recInMemory,
		// MemoryTool.getBatch(recInMemory));
		// }
		//
		// }

		// List<Index> indexes = MigrateCenter.fetchIndexesFromSqlServer();
		// System.out.println(indexes.size());
		// for(Index idx : indexes ){
		// System.out.println(idx.toSql());
		// }

		// List<ConstraintCK> cks =
		// MigrateConstraint.fetchConstraintCKFromSqlServer();
		//
		// for(ConstraintCK ck : cks ){
		// System.out.println(ck.toSql());
		// }

		List<Sequence> seqs = MigrateCenter.fetchSequencesFromSqlServer();
		System.out.println(seqs.size());

		Set<String> pkset = new HashSet<>();

		// ALTER TABLE dbo.T_Node_New ADD CONSTRAINT PK_T_Node_New1 PRIMARY KEY
		// (ID)
		// ALTER TABLE dbo.SiteDataDisplay ADD CONSTRAINT PK_SiteDataDisplay
		// PRIMARY KEY (GUID)
		// ALTER TABLE dbo.attachment ADD CONSTRAINT PK_ATTACHMENT PRIMARY KEY
		// (BLOBGUID)
		// ALTER TABLE dbo.BLOB ADD CONSTRAINT PK_BLOB PRIMARY KEY (BLOBGUID)
		// ALTER TABLE dbo.T_DataInfo ADD CONSTRAINT PK_T_DataInfo1 PRIMARY KEY
		// (Guid)
		// ALTER TABLE dbo.TemplateDistributionRelation ADD CONSTRAINT
		// PK_TEMPLATEDISTRIBUTIONRELATIO PRIMARY KEY (GUID)
		// ALTER TABLE dbo.LabelDataSource ADD CONSTRAINT PK_LABELDATASOURCE
		// PRIMARY KEY (guid)
		// ALTER TABLE dbo.LogError ADD CONSTRAINT PK_LOGERROR PRIMARY KEY (ID)
		// ALTER TABLE dbo.LabelStyle ADD CONSTRAINT PK_LABELSTYLE PRIMARY KEY
		// (GUID)
		// ALTER TABLE dbo.PageTemplate ADD CONSTRAINT PK_PAGETEMPLATE PRIMARY
		// KEY (GUID)
		// ALTER TABLE dbo.UserInfo ADD CONSTRAINT PK_UserInfo PRIMARY KEY
		// (Guid)
		// ---- ALTER TABLE dbo.TemplateRelationColumn ADD CONSTRAINT
		// PK_TEMPLATERELATIONCOLUMN PRIMARY KEY
		// (columnGuid,objectType,templateGuid)
		// ALTER TABLE dbo.WaitPublishInfo ADD CONSTRAINT PK_WAITPUBLISHINFO
		// PRIMARY KEY (GUID)
		// ALTER TABLE dbo.DocumentExtendInfo ADD CONSTRAINT
		// PK_DOCUMENTEXTENDINFO PRIMARY KEY (GUID)
		// ALTER TABLE dbo.WaitPublishInfo_bak ADD CONSTRAINT
		// PK_WAITPUBLISHINFO_bak PRIMARY KEY (GUID)
		// ALTER TABLE dbo.SiteInfo ADD CONSTRAINT PK_SITEINFO PRIMARY KEY
		// (GUID)
		// ALTER TABLE dbo.SiteMonitorExtUrlInfo ADD CONSTRAINT
		// PK_SITEMONITOREXTURLINFO PRIMARY KEY (GUID)
		// ALTER TABLE dbo.SiteMonitExtUrlResult ADD CONSTRAINT
		// PK_SITEMONITEXTURLRESULT PRIMARY KEY (GUID)
		// ALTER TABLE dbo.loginLog ADD CONSTRAINT PK_loginLog PRIMARY KEY
		// (GUID)
		// ALTER TABLE dbo.DocumentBaseInfo_Text ADD CONSTRAINT
		// PK_DOCUMENTBASEINFO_Text PRIMARY KEY (GUID)
		// ALTER TABLE dbo.DistributionInfo ADD CONSTRAINT PK_DISTRIBUTIONINFO
		// PRIMARY KEY (GUID)
		// ----- ALTER TABLE dbo.RelatedDocument ADD CONSTRAINT
		// PK_RelatedDocument PRIMARY KEY (GUID)

		pkset.add("PK_WAITPUBLISHINFO");
		pkset.add("PK_DOCUMENTEXTENDINFO");
		pkset.add("PK_WAITPUBLISHINFO_bak");
		pkset.add("PK_SITEINFO");
		pkset.add("PK_SITEMONITOREXTURLINFO");
		pkset.add("PK_SITEMONITEXTURLRESULT");
		pkset.add("PK_loginLog");
		pkset.add("PK_DOCUMENTBASEINFO_Text");
		pkset.add("PK_DISTRIBUTIONINFO");
		pkset.add("PK_RelatedDocument");

		pkset.add("PK_T_Node_New1");
		pkset.add("PK_SiteDataDisplay");
		pkset.add("PK_ATTACHMENT");
		pkset.add("PK_BLOB");
		pkset.add("PK_T_DataInfo1");
		pkset.add("PK_TEMPLATEDISTRIBUTIONRELATIO");
		pkset.add("PK_LABELDATASOURCE");
		pkset.add("PK_LOGERROR");
		pkset.add("PK_LABELSTYLE");
		pkset.add("PK_PAGETEMPLATE");
		pkset.add("PK_UserInfo");
		pkset.add("PK_TEMPLATERELATIONCOLUMN");

		List<ConstraintPK> pks = MigrateConstraint.fetchConstraintPKFromSqlServer();

		// for(ConstraintPK pk : pks ){
		// if(pkset.contains(pk.cName)){
		// continue;
		// }
		// System.out.println(pk.toSql());
		// MigrateConstraint.createConstraintPK(pk.cName);
		// }

		// List<ConstraintUK> uks =
		// MigrateConstraint.fetchConstraintUKFromSqlServer();
		//
		// for(ConstraintUK uk : uks ){
		// System.out.println(uk.toSql());
		// }

		// List<ConstraintFK> fks =
		// MigrateConstraint.fetchConstraintFKFromSqlServer();
		//
		// for(ConstraintFK fk : fks ){
		// System.out.println(fk.toSql());
		// }

		// 108036

	}
}
