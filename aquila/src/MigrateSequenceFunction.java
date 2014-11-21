import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;

import com.highgo.hgdbadmin.model.Sequence;
import com.highgo.hgdbadmin.myutil.MigrateCenter;
import com.highgo.hgdbadmin.myutil.ShellEnvironment;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 1.���ݴ������Ϣ������һ��xml����� 2.����һ��Connection��������Ϣ�Ƿ���Ч 3.�������д��c3p0�������ļ�
 * 
 * @author u
 *
 */
public class MigrateSequenceFunction extends AquilaFunction {

	@SuppressWarnings("static-access")
	public MigrateSequenceFunction() {
		this.addOption(OptionBuilder.withDescription("migrate sequence.").withLongOpt("name").hasArg().create("name"));
	}

	public Object executeFunction(CommandLine line) {
		if (!line.hasOption("name")) {
			ShellEnvironment.println("Required argument --name is missing.");
			return null;
		}
		try {
			String sequence = line.getOptionValue("name");
			migrateSequence(sequence);
		} catch (IOException | SQLException | InterruptedException ex) {
			throw new AquilaException(ShellError.SHELL_0007, "something wrong!");
		}
		return null;
	}

	private void migrateSequence(String sequence) throws IOException, SQLException, InterruptedException {
		ShellEnvironment.println("Create Sequence:" + sequence);
		String[] strs = sequence.split("\\.");// ��"."��Ϊ�ָ���
		MigrateCenter.createSequence(strs[0], strs[1]);
		ShellEnvironment.println("Create Sequence:" + sequence + " successfully!");
	}
}
