import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jline.ConsoleReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.xml.sax.SAXException;

import com.highgo.hgdbadmin.myutil.DBUtil;
import com.highgo.hgdbadmin.myutil.ShellEnvironment;

/**
 * 1.���ݴ������Ϣ������һ��xml����� 2.����һ��Connection��������Ϣ�Ƿ���Ч 3.�������д��c3p0�������ļ�
 * 
 * @author u
 *
 */
public class CreateConnectionFunction extends AquilaFunction {
	@SuppressWarnings("static-access")
	public CreateConnectionFunction() {
		this.addOption(OptionBuilder.withDescription("create connection.").withLongOpt("name").hasArg()
				.create("name"));
	}

	public Object executeFunction(CommandLine line) {
		if (!line.hasOption("name")) {
			ShellEnvironment.println("Required argument --name is missing");
			return null;
		}
		try {
			createConnection(line.getOptionValue("name"));
		} catch (IOException ex) {
			throw new AquilaException(ShellError.SHELL_0007, "create");
		}
		return null;
	}

	private void createConnection(String sname) throws IOException {
		ShellEnvironment.println("create connection:"+ sname);
		ConsoleReader reader = new ConsoleReader();
//		reader.printString("Name:");
//		reader.flushConsole();
//		String name = reader.readLine();
		reader.printString("Connection Configuration:");
		reader.printNewline();
		reader.printString("JDBC Driver Class:");
		reader.flushConsole();
		String driver = reader.readLine();
		reader.printString("Connection String:");
		reader.flushConsole();
		String url = reader.readLine();
		reader.printString("username:");
		reader.flushConsole();
		String username = reader.readLine();
		reader.printString("password:");
		reader.flushConsole();
		String password = reader.readLine();

		// ����ط�Ҫ���������Ϣ������Ч�Ե��жϣ�����AquilaException��׼��
		// TODO
		boolean bool = DBUtil.testConnection(driver, url, username, password);
		// c3p0�������ļ���λ�ã���һ��Ҫ��Resource�Ǹ�����ϵ����
		// ��һ��������һЩc3p0���������
		if (bool) {
			try {
				XMLUtil.saveConnection(Constants.PATH, sname, driver, url, username, password);
			} catch (ParserConfigurationException | SAXException | TransformerException e) {
				e.printStackTrace();
			}
			ShellEnvironment.println("create connection " + sname + " successfully!");
		} else {
			ShellEnvironment.println("create connection " + sname + " failed!");
		}
	}
}
