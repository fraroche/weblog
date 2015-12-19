package fr.si2m.monitoring;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.helpers.DefaultHandler;

public class Log4JLoader extends DefaultHandler {
	
	private static Logger		logger		= Logger.getLogger(Log4JLoader.class);
	private static Log4JLoader singleton	= new Log4JLoader();
	
	private Log4JLoader() {
		
	}
	
	private static String systemPropertyConverter(final String pContainsSystemProp) {
		Pattern modele = Pattern.compile("\\$\\{[^\\$\\}]*\\}");
		Matcher recherche = modele.matcher(pContainsSystemProp);
		String output = "";
		int end = 0;
		int start;
		String group = null;
		while (recherche.find()) {
			start = recherche.start();
			group = recherche.group();
			output += pContainsSystemProp.substring(end, start) + System.getProperty(group.substring(2, group.length()-1));
			end = recherche.end();
		}
		output +=  pContainsSystemProp.substring(end);
		return output;
	}

	public static Log4JLoader getInstance() throws Exception {
		return singleton;
	}

	public static void main(final String args[]) throws Exception {
		System.setProperty("foo.bar", "FOO_BAR");
		System.setProperty("toto.titi", "TOTO_TITI");
		String input = "s1${foo.bar}s2${toto.titi}____alpha";
		String out = systemPropertyConverter(input);
		out = systemPropertyConverter("chainesansrienaremplacer");
		
		String file = "/home/nta/workspace/WhiteWeb/tomcat/jboss-log4j.xml";
		PropertyConfigurator.configure(file);
		URL log4jfilePath = new URL("file:" + file);
//		Log4JLoader log4j = new Log4JLoader(log4jfilePath);
//		System.out.println(log4j.getFileList().toString());
	}
	
	public ArrayList getFileList() {
		if (logger.isDebugEnabled()) {
			logger.debug(">< - getFileList()");
		}
		ArrayList lFileList = new ArrayList();
		Enumeration lAppenders = LogManager.getRootLogger().getAllAppenders();
		while (lAppenders.hasMoreElements()) {
			Appender lAppender = (Appender) lAppenders.nextElement();
			if (lAppender instanceof FileAppender) {
				lFileList.add(((FileAppender)lAppender).getFile());
			}
		}
		return lFileList;
	}
	
	public ArrayList getLoggers() {
		Enumeration lAllLoggers = LogManager.getCurrentLoggers();
		ArrayList lLoggers = new ArrayList();
		lLoggers.add(LogManager.getRootLogger());
		while (lAllLoggers.hasMoreElements()) {
			Logger logger = (Logger) lAllLoggers.nextElement();
			if (logger.getLevel() != null) {
				lLoggers.add(logger);
			}
		}
		return lLoggers;
	}
}