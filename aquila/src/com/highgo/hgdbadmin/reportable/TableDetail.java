package com.highgo.hgdbadmin.reportable;

import com.highgo.hgdbadmin.model.Table;

public class TableDetail {
	public ObjectInfo baseInfo;

	public long totalRead; // ���̶߳����ļ�¼��
	public long totalWrite; // ����д�̳߳ɹ��ύ�ĸ�����Ҳ���������ܹ�ȷ����д�뵽PG�еļ�¼��
	public long totalDerby; // ������derby��д��Ҳʧ�ܵļ�¼����Ҳ��������ʧ�ܵļ�¼��

	public Table source;
	public Table dest;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(baseInfo);

		sb.append("totalRead:" + totalRead + "\n");
		sb.append("totalWrite:" + totalWrite + "\n");
		sb.append("totalDerby:" + totalDerby + "\n");

		sb.append("source:" + source);
		sb.append("dest:" + dest);

		return sb.toString();
	}
}
