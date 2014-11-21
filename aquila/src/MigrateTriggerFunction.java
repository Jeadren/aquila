import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;

import com.highgo.hgdbadmin.model.Function;
import com.highgo.hgdbadmin.model.Index;
import com.highgo.hgdbadmin.model.Procedure;
import com.highgo.hgdbadmin.model.Sequence;
import com.highgo.hgdbadmin.model.Trigger;
import com.highgo.hgdbadmin.model.View;
import com.highgo.hgdbadmin.myutil.MigrateCenter;
import com.highgo.hgdbadmin.myutil.ShellEnvironment;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 1.���ݴ������Ϣ������һ��xml����� 2.����һ��Connection��������Ϣ�Ƿ���Ч 3.�������д��c3p0�������ļ�
 * 
 * @author u
 *
 */
public class MigrateTriggerFunction extends AquilaFunction {

	@SuppressWarnings("static-access")
	public MigrateTriggerFunction() {
		this.addOption(OptionBuilder.withDescription("migrate trigger.").withLongOpt("name").hasArg().create("name"));
	}

	public Object executeFunction(CommandLine line) {
		if (!line.hasOption("name")) {
			ShellEnvironment.println("Required argument --name is missing.");
			return null;
		}
		try {
			String trigger = line.getOptionValue("name");
			migrateTrigger(trigger);
		} catch (IOException | SQLException | InterruptedException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private void migrateTrigger(String trigger) throws IOException, SQLException, InterruptedException {
		ShellEnvironment.println("Create trigger:" + trigger);
		MigrateCenter.createTrigger(trigger);
		ShellEnvironment.println("Create Trigger " + trigger + " successfully!");
	}
}
