import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jline.ConsoleReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.xml.sax.SAXException;

import com.highgo.hgdbadmin.myutil.DataTypeMap;
import com.highgo.hgdbadmin.myutil.ShellEnvironment;

/**
 * 1.���ݴ������Ϣ������һ��xml����� 2.����һ��Connection��������Ϣ�Ƿ���Ч 3.�������д��c3p0�������ļ�
 * 
 * @author u
 *
 */
public class CreateDataTypeMapFunction extends AquilaFunction {
	@SuppressWarnings("static-access")
	public CreateDataTypeMapFunction() {
		this.addOption(OptionBuilder.withDescription("create a data type map.").withLongOpt("s").hasArg().create("s"));
		this.addOption(OptionBuilder.withDescription("create a data type map.").withLongOpt("d").hasArg().create("d"));
	}

	public Object executeFunction(CommandLine line) {
		if (!line.hasOption("s") || !line.hasOption("d")) {
			ShellEnvironment.println("Required argument --s|--d is missing!");
			return null;
		}
		try {
			createDataTypeMap(line.getOptionValue("s"), line.getOptionValue("d"));
		} catch (IOException ex) {
			throw new AquilaException(ShellError.SHELL_0007, "create");
		}
		return null;
	}

	private void createDataTypeMap(String s, String d) throws IOException {
		ShellEnvironment.println("create data type map:" + s + " <==> " + d);
		DataTypeMap.add(s, d);
		ShellEnvironment.println("create data type map:" + s + " <==> " + d + " successfully!");
	}
}
