package fr.si2m.monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MonitorServlet extends HttpServlet {

	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		String logfile = null;
		if ((logfile = req.getParameter("logfile")) != null) {
			try {
				final URL urlLogPath = new URL("file:"+logfile);
				final InputStream is = urlLogPath.openStream();
				final OutputStream os = resp.getOutputStream();
				resp.setContentType("text/plain");
				resp.setHeader("Content-Disposition", "attachment;filename="+logfile.substring(logfile.lastIndexOf("/") + 1));
				final byte buf[] = new byte[4096];
				for (int count = is.read(buf); count > -1; count = is.read(buf)) {
					os.write(buf, 0, count);
				}
				is.close();
				os.close();
			} catch (final Exception e) {
			}
		}
		String loggerName = null;
		String loggerLevel = null;
		if (((loggerName = req.getParameter("logger"))!=null) &&
			((loggerLevel = req.getParameter("level"))!=null)) {
			Logger logger = null;
			if ("root".equals(loggerName)) {
				logger = Logger.getRootLogger();
			} else {
				logger = Logger.getLogger(loggerName);
			}
			switch (Integer.parseInt(loggerLevel)) {
				case Level.ALL_INT :
					logger.setLevel(Level.ALL);
					break;
				case Level.DEBUG_INT :
					logger.setLevel(Level.DEBUG);
					break;
				case Level.INFO_INT :
					logger.setLevel(Level.INFO);
					break;
				case Level.WARN_INT :
					logger.setLevel(Level.WARN);
					break;
				case Level.ERROR_INT :
					logger.setLevel(Level.ERROR);
					break;
				case Level.FATAL_INT :
					logger.setLevel(Level.FATAL);
					break;
				case Level.OFF_INT :
					logger.setLevel(Level.OFF);
					break;
				default:
					final Level traceLevel = Level.toLevel("TRACE");
					if (Level.DEBUG_INT != traceLevel.toInt()) {
						logger.setLevel(traceLevel);
					}
			}
		}
	}

	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		this.doGet(req, resp);
	}

}
