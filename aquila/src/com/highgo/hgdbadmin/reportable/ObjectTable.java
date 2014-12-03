package com.highgo.hgdbadmin.reportable;

import java.util.LinkedList;
import java.util.List;

public class ObjectTable {
	// ���title��Ҫ���ֶ�
	public String type;
	public int sum; // �����������ҪǨ�Ƶ��������������ܹ���������
	public int numSuccessed;
	public int numFailed;

	// ����������
	public List<ObjectInfo> rows = new LinkedList<>();;

	public ObjectTable(String type) {
		this.type = type;
	}

	public String getTitle() {
		// TODO
		// ����ط���Ҫ��֤һ��sum=numSuccessed+numFailed��
		return this.type.toUpperCase() + "[" + sum + "/" + numSuccessed + "]";
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("type:" + type + "\n");
		sb.append("sum:" + sum + "\n");
		sb.append("numSuccessed:" + numSuccessed + "\n");
		sb.append("numFailed:" + numFailed + "\n");
		for (ObjectInfo oi : rows) {
			sb.append(oi);
		}
		return sb.toString();
	}

	public String toHTML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<div>");
		
		sb.append("<a NAME='"+this.type+"'></a>");
		
		sb.append("<table border=1>");

		sb.append("<caption><h2>" + this.getTitle() + "</h2></caption>");

		sb.append("<thead><tr>");
		sb.append("<th>objectName</th>");
//		sb.append("<th>sourceDifinition</th>");
//		sb.append("<th>sqlGenerated</th>");
		sb.append("<th>isSuccessed</th>");
		sb.append("<th>causes</th>");
		sb.append("</thead></tr>");

		sb.append("<tbody>");
		for (ObjectInfo oi : rows) {
			sb.append(oi.toHTML());
		}
		sb.append("</tbody>");

		sb.append("</table>");
		sb.append("</div>");
		
		sb.append("<br/>");
		sb.append("<br/>");
		sb.append("<br/>");
		sb.append("<br/>");
		sb.append("<br/>");
		sb.append("<br/>");
		sb.append("<br/>");
		return sb.toString();
	}
}