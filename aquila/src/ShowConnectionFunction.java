import java.io.IOException;
import java.util.List;

import jline.ConsoleReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;

/**
 * 1.���ݴ������Ϣ������һ��xml����� 2.����һ��Connection��������Ϣ�Ƿ���Ч 3.�������д��c3p0�������ļ�
 * 
 * @author u
 *
 */
public class ShowConnectionFunction extends AquilaFunction {
	@SuppressWarnings("static-access")
	public ShowConnectionFunction() {
		this.addOption(OptionBuilder.withDescription("show all connections.").withLongOpt("cn").hasArg().create("cn"));
	}

	public Object executeFunction(CommandLine line) {
		try {
			String cn = line.getOptionValue("cn");
			getAllConnection(cn);
		} catch (IOException ex) {
			throw new AquilaException(ShellError.SHELL_0007, "show");
		}
		return null;
	}

	private void getAllConnection(String cn) throws IOException {
		ConsoleReader reader = new ConsoleReader();
		AConnection ac = XMLUtil.getConnection(Constants.PATH, cn);
		reader.printString(ac.toString());
		reader.flushConsole();
	}

	public static void main(String[] args) {
		ShowConnectionFunction scf = new ShowConnectionFunction();
	}
}
