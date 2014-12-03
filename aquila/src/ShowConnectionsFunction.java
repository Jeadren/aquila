import java.io.IOException;
import java.util.List;

import jline.ConsoleReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;

import com.highgo.hgdbadmin.myutil.Constants;

/**
 * 1.���ݴ������Ϣ������һ��xml����� 2.����һ��Connection��������Ϣ�Ƿ���Ч 3.�������д��c3p0�������ļ�
 * 
 * @author u
 *
 */
public class ShowConnectionsFunction extends AquilaFunction {
	@SuppressWarnings("static-access")
	public ShowConnectionsFunction() {
		this.addOption(OptionBuilder.withDescription("show all connections.").withLongOpt("").hasArg().create());
	}

	public Object executeFunction(CommandLine line) {
		try {
			getAllConnection();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private void getAllConnection() throws IOException {
		ConsoleReader reader = new ConsoleReader();
		reader.printString("all connections:");
		reader.printNewline();
		List<AConnection> list = XMLUtil.getConnections(Constants.PATH);
		for (int i = 0; i < list.size(); i++) {
			reader.printString("Connection" + i + ":");
			reader.printNewline();
			reader.printString(list.get(i).toString());
		}
		reader.flushConsole();
	}

	public static void main(String[] args) {
		ShowConnectionsFunction scf = new ShowConnectionsFunction();
	}
}
