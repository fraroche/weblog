package fr.si2m.monitoring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.Principal;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class for Servlet: ExplorerServlet
 *
 */
public class ExplorerServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	static final long serialVersionUID = 1L;

	static StringBuilder css = new StringBuilder();
	static StringBuilder header = new StringBuilder();
	static StringBuilder footer = new StringBuilder("</body></html>");
	static {

		css.append("<style type=\"text/css\">")
		.append("body {")
		.append("	font-family: \"trebuchet ms\", helvetica, sans-serif;")
		.append("	font-size: 90%;")
		.append("	font-size-adjust: none;")
		.append("	font-style: normal;")
		.append("	font-variant: normal;")
		.append("	font-weight: normal;")
		.append("	line-height: normal;")
		.append("}")
		.append("")
		.append("h1 {")
		.append("	color: black;")
		.append("	font-size: 160%;")
		.append("}")
		.append("")
		.append(".objet {")
		.append("	background-color: #F5F5F5;")
		.append("	border: 1px dotted #575757;")
		.append("	color: #575757;")
		.append("	font-size: 90%;")
		.append("	padding: 0 5px;")
		.append("}")
		.append("")
		.append(".objet li {")
		.append("	list-style-image: none;")
		.append("	list-style-position: outside;")
		.append("	list-style-type: square;")
		.append("}")
		.append("")
		.append("li {")
		.append("	font-size: 100%;")
		.append("	list-style-image: url(images/pucegrise.gif);")
		.append("}")
		.append("")
		.append(".objet p {")
		.append("	font-weight: bold;")
		.append("}")
		.append("")
		.append("p {")
		.append("	font-size: 100%;")
		.append("	text-align: justify;")
		.append("}")
		.append("")
		.append(".green {")
		.append("	color: green;")
		.append("}")
		.append(".red {")
		.append("	color: red;")
		.append("}")
		.append("</style>");

		header.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">")
		.append("<html>")
		.append("<head>")
		.append(css)
		.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">")
		.append("<title>Explorer</title>")
		.append("</head>")
		.append("<body>");
	}
	public ExplorerServlet() {
		super();
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		boolean tomcat = request.isUserInRole("tomcat");
		System.out.println("ExplorerServlet.doPost() - request.isUserInRole(\"tomcat\"):" +tomcat);
		boolean foobar = request.isUserInRole("foobar");
		System.out.println("ExplorerServlet.doPost() - request.isUserInRole(\"foobar\"):" +foobar);
		boolean COMMUN = request.isUserInRole("COMMUN");
		System.out.println("ExplorerServlet.doPost() - request.isUserInRole(\"COMMUN\"):" +COMMUN);
		Principal princ = request.getUserPrincipal();
		File currentFolder = (File) request.getSession().getAttribute("currentFolder");
		String file = request.getParameter("file");
		String folder = request.getParameter("folder");

		if (currentFolder == null) {
			Context ic = null;
			String contextRoot = null;
			try {
				ic = new InitialContext();
				contextRoot = (String) ic.lookup("var/contextRoot");
				currentFolder = new File(contextRoot.substring(contextRoot.indexOf(":")+1));
				if (currentFolder.isFile()) {
					currentFolder = currentFolder.getParentFile();
				}
				request.getSession().setAttribute("currentFolder", currentFolder);
			} catch (final NamingException e) {
				e.printStackTrace();
			}
		}

		if (file != null) {
			final File child = this.getChild(currentFolder, file);
			if ((child != null) && child.isFile() && child.canRead()) {
				try {
					final InputStream is = child.toURL().openStream();
					final OutputStream os = response.getOutputStream();
					response.setContentType("text/plain");
					response.setHeader("Content-Disposition", "attachment;filename="+file);
					final byte buf[] = new byte[4096];
					for (int count = is.read(buf); count > -1; count = is.read(buf)) {
						os.write(buf, 0, count);
					}
					is.close();
					os.close();
				} catch (final Exception e) {
				}
			}
		} else {
			if (folder != null) {
				if ("..".equals(folder)) {
					currentFolder = currentFolder.getParentFile();
				} else if (folder.contains("/")) {
					currentFolder = new File(folder);
				} else {
					currentFolder = this.getChild(currentFolder, folder);
				}
				request.getSession().setAttribute("currentFolder", currentFolder);
			}
			this.printFolderContent(request, response);
		}
	}

	private File getChild(final File currentFolder, final String childName) {
		final File[] children = currentFolder.listFiles();
		for (int i = children.length, j=i; i>0; i--) {
			final File childFile = children[j-i];
			if (childName.equals(childFile.getName())) {
				return childFile;
			}
		}
		return null;
	}

	private void printFolderContent(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final Writer out = response.getWriter();
		final File folder = (File) request.getSession().getAttribute("currentFolder");
		final File parent = folder.getParentFile();

		out.write(header.toString());

		out.write("<div class=\"objet\">");
		out.write("<p>Current folder:</p>");

		String href = null;
		String outputPath = "/"+folder.getName();
		for (File tmpFolder = folder.getParentFile(); tmpFolder != null; tmpFolder = tmpFolder.getParentFile()) {
			String folderName = tmpFolder.getName();
			if (folderName.length() > 0) {
				href = "/<a href=\""+request.getContextPath()+"/explorer?folder="+URLEncoder.encode(tmpFolder.toString(),"UTF-8")+"\">"+folderName+"</a>";
			} else {
				href = "<a href=\""+request.getContextPath()+"/explorer?folder="+tmpFolder.toString()+"\">/</a>";
				outputPath = outputPath.substring(1);
			}
			outputPath = href + outputPath;
		}
		out.write(outputPath+"</div><p></p>");


		out.write("<div class=\"objet\">");
		if (parent != null) {
			out.write("<a href=\""+request.getContextPath()+"/explorer?folder=..\" >..</a><br>");
		}
		final File[] children = folder.listFiles();
		if (children != null) { 
			for (int i = children.length, j=i; i>0; i--) {
				final File child = children[j-i];
				String childName = child.getName();
				String childNameEncoded = URLEncoder.encode(childName, "UTF-8");
				if (child.isFile()) {
					out.write("[F] <a href=\""+request.getContextPath()+"/explorer?file="+childNameEncoded+"\" >"+childName+"</a><br>");
				} else if (child.isDirectory()) {
					out.write("[D] <a href=\""+request.getContextPath()+"/explorer?folder="+childNameEncoded+"\" >"+childName+"</a><br>");
				} else {
					out.write("[-] "+childName+"<br>");
				}
			}
		} else {
			out.write("Empty folder");
		}
		out.write("</div>");

		out.write(footer.toString());
	}

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			final URI root = ContextUtil.getClassRootUri(this.getClass());
			System.out.println(root.toString());
			String rt = root.toString();
			while(!rt.startsWith("file")) {
				rt = rt.substring(rt.indexOf(":")+1, rt.lastIndexOf("!"));
			}
			Context ic = new InitialContext();
			ic = ic.createSubcontext("var");
			ic.bind("contextRoot", rt);
		} 
		catch (final StringIndexOutOfBoundsException e) {
			e.printStackTrace();
		}catch (final URISyntaxException e) {
			e.printStackTrace();
		} catch (final NamingException e) {
			e.printStackTrace();
		}
	}
}