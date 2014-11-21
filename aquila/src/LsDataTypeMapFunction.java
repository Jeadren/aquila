import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;

import com.highgo.hgdbadmin.model.Sequence;
import com.highgo.hgdbadmin.model.View;
import com.highgo.hgdbadmin.myutil.DataTypeMap;
import com.highgo.hgdbadmin.myutil.MigrateCenter;
import com.highgo.hgdbadmin.myutil.MigrateConstraint;
import com.highgo.hgdbadmin.myutil.ShellEnvironment;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 1.���ݴ������Ϣ������һ��xml����� 2.����һ��Connection��������Ϣ�Ƿ���Ч 3.�������д��c3p0�������ļ�
 * 
 * @author u
 *
 */
public class LsDataTypeMapFunction extends AquilaFunction {

	@SuppressWarnings("static-access")
	public LsDataTypeMapFunction() {
		this.addOption(OptionBuilder.withDescription("list all datatype map.").withLongOpt("").hasArg().create());
	}

	public Object executeFunction(CommandLine line) {
		try {
			lsDataTypeMap();
		} catch (IOException | SQLException | InterruptedException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private void lsDataTypeMap() throws IOException, SQLException, InterruptedException {
		ShellEnvironment.println("list all data type map:");
		String dtMap = DataTypeMap.getAllDataTypeMap();
		ShellEnvironment.println(dtMap);
		ShellEnvironment.println("list all data type map successfully!");
	}
}
