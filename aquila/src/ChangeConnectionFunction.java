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
 * @author u
 *
 */
public class ChangeConnectionFunction extends AquilaFunction {
	@SuppressWarnings("static-access")
	public ChangeConnectionFunction() {
		this.addOption(OptionBuilder.withDescription("change sth").withLongOpt("name").hasArg().create("name"));
	}

	public Object executeFunction(CommandLine line) {
		if (!line.hasOption("name")) {
			ShellEnvironment.println("Required argument --name is missing");
			;
			return null;
		}
		try {
			changeConnection(line.getOptionValue("name"));
		} catch (IOException ex) {
			throw new AquilaException(ShellError.SHELL_0007, "create");
		}
		return null;
	}

	/**
	 * 1.�����������Ϣ����createʱ���� 2.��֤��Ϣ���� 3.ɾ��ԭ���� 4.д�뵱ǰ���
	 * 
	 * @param sname
	 * @throws IOException
	 */
	private void changeConnection(String sname) throws IOException {
		ShellEnvironment.println("change connection:");
		ConsoleReader reader = new ConsoleReader();
		reader.printString("Name:");
		reader.flushConsole();
		String name = reader.readLine();
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
		// TODO Ŀǰ������ӵķ��������׳�����
		boolean bool = DBUtil.testConnection(driver, url, username, password);

		// c3p0�������ļ���λ�ã���һ��Ҫ��Resource�Ǹ�����ϵ����
		// ��һ��������һЩc3p0���������
		if (bool) {
			try {
				XMLUtil.deleteConnection(name);
				XMLUtil.saveConnection(Constants.PATH, name, driver, url, username, password);
			} catch (ParserConfigurationException | SAXException | TransformerException e) {
				e.printStackTrace();
			}
			ShellEnvironment.println("change connection " + name + " successfully!");
		} else {
			ShellEnvironment.println("change connection " + name + " failed!");
		}
	}
}
