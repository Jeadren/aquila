package com.highgo.hgdbadmin.model;

/**
 * Pg��Sequenceò��ֻ֧��������SqlServer֧�����е��������ͣ���֧���Զ������ͣ�����Ǩ�ƹ����У�ֻ���������������
 * 
 * @author u
 *
 */
public class Sequence {
	public String schema;
	public String name;

	public long startValue;
	public long minimumValue;
	public long maximunValue;
	public int increment;
	public int cycle_option;

	public Sequence(String schema, String name, long startValue, long minimumValue, long maximunValue, int increment,
			int cycle_option) {
		super();
		this.schema = schema;
		this.name = name;
		this.startValue = startValue;
		this.minimumValue = minimumValue;
		this.maximunValue = maximunValue;
		this.increment = increment;
		this.cycle_option = cycle_option;
	}

	public String toSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE SEQUENCE " + this.schema + "." + this.name);
		sb.append(" INCREMENT BY " + this.increment);
		sb.append(" MINVALUE " + this.minimumValue);
		sb.append(" MAXVALUE " + this.maximunValue);
		sb.append(" START WITH " + this.startValue);
		if (this.cycle_option == 1) {
			sb.append(" CYCLE");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return this.schema + "." + this.name;
	}

}
