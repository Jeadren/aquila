import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;

import com.highgo.hgdbadmin.model.Table;
import com.highgo.hgdbadmin.myutil.ShellEnvironment;

/**
 * 1.���ݴ������Ϣ������һ��xml����� 2.����һ��Connection��������Ϣ�Ƿ���Ч 3.�������д��c3p0�������ļ�
 * 
 * @author u
 *
 */
public class LsTableFunction extends AquilaFunction {

	@SuppressWarnings("static-access")
	public LsTableFunction() {
		this.addOption(OptionBuilder.withDescription("list all table.").withLongOpt("").hasArg().create());
	}

	public Object executeFunction(CommandLine line) {
		try {
			lsTable();
		} catch (IOException | SQLException | InterruptedException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private void lsTable() throws IOException, SQLException, InterruptedException {
		ShellEnvironment.println("list all data type map:");
		List<Table> list = Constants.TABLES;
		for(Table table : list){
			ShellEnvironment.println(table);
		}
		ShellEnvironment.println("list all data type map successfully!");
	}
}
