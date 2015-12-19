<%@page import="org.apache.commons.logging.Log"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<script language="JavaScript">

var ajaxLibrairie = (function() {
	// Membres privés
	var method = "POST";
	function getXhr(ajaxCallBackImpl) {
		var xhr = null;
		if(window.XMLHttpRequest) {// Firefox et autres
			xhr = new XMLHttpRequest();
		} else if(window.ActiveXObject) { // Internet Explorer 
			try {
				xhr = new ActiveXObject("Msxml2.XMLHTTP");
			} catch (e) {
				xhr = new ActiveXObject("Microsoft.XMLHTTP");
			}
		} else { // XMLHttpRequest non supporté par le navigateur 
			alert("Votre navigateur ne supporte pas les objets XMLHTTPRequest..."); 
			xhr = false; 
		}
		if (xhr) {
			xhr.onreadystatechange = function() {
				if(xhr.readyState == 4 && xhr.status == 200) {
					ajaxCallBackImpl(xhr);
				}
			}
		}
		return xhr;
	}
	
	// Membres publics
	return {
		"setMethod" : function(arg0) {
			method = arg0;
		}, 
		"getMethod" : function() {
			return method;
		}, 
		"ajaxCall" : function(url, args, ajaxCallBackImpl) {
			xhr = getXhr(ajaxCallBackImpl);
			try {
				xhr.open(this.getMethod(), url, true);
				xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
				// ne pas oublier de poster les arguments
				xhr.send(args);
			} catch (e) {
				alert(e);
				throw e;
			}
		}
	};
})();
function submitChangeLevel(loggerName) {
	var options = document.getElementsByName(loggerName)[0].options;
	var level;
	var levelName;
	for (var i=0; i<options.length; i++) {
		if (options[i].selected==true) {
			level = options[i].value;
			levelName = options[i].text;
			break;
		}
	}
	var pathname = '<%=request.getContextPath()%>'+'/monitoring?logger='+loggerName+'&level='+level;
	if (ajaxLibrairie.ajaxCall) {
		ajaxLibrairie.ajaxCall("monitoring", "logger="+loggerName+"&level="+level, doNothingWithResponse(loggerName, levelName)); 
	}
}
function doNothingWithResponse(logger, level) {
	return function(xhr) {
		// do nothing, we dont care about the response
		alert("logger '"+ logger + "' has changed to '" + level + "'");
	}
}
</script>
<style type="text/css">
body {
	font-family: "trebuchet ms", helvetica, sans-serif;
	font-size: 90%;
	font-size-adjust: none;
	font-style: normal;
	font-variant: normal;
	font-weight: normal;
	line-height: normal;
}

h1 {
	color: black;
	font-size: 160%;
}

.objet {
	background-color: #F5F5F5;
	border: 1px dotted #575757;
	color: #575757;
	font-size: 90%;
	padding: 0 5px;
}

.objet li {
	list-style-image: none;
	list-style-position: outside;
	list-style-type: square;
}

li {
	font-size: 100%;
	list-style-image: url(images/pucegrise.gif);
}

.objet p {
	font-weight: bold;
}

p {
	font-size: 100%;
	text-align: justify;
}

.green {
	color: green;
}
.red {
	color: red;
}
</style>
<meta http-equiv="Pragma" content="no-cache">
<%@page import="fr.si2m.monitoring.Log4JLoader"%>
<%@page import="fr.si2m.monitoring.ManifestLoader" %>
<%@page import="fr.si2m.monitoring.ContextUtil"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.jar.Manifest" %>
<%@page import="java.net.URL"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.SQLException"%>
<%@page import="javax.sql.DataSource" %>
<%@page import="javax.naming.InitialContext" %>
<%@page import="javax.naming.NamingException" %>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="org.apache.log4j.Level"%>

<%@page import="java.sql.ResultSet"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<form action="monitoring.jsp" method=post>
<h1>Monitoring <%=ManifestLoader.getManifest().getMainAttributes().getValue("Application-Name")%></h1>
<div class="objet">
<p>Informations application</p>
<ul>
	<li>
		Version <span class="green"><%=ManifestLoader.getManifest().getMainAttributes().getValue("Manifest-Version")%></span>
	</li>
	<li>
		Ressources JNDI: 
		<ul>
<%
		ArrayList jndiElts = ContextUtil.getLocalNameSpaceElements();
		InitialContext ic = null;
		DataSource ds = null;
		Connection cn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			ic = new InitialContext();
			for (int i=jndiElts.size(), j=i; i>0; i--) {
				String jndiElt = (String) jndiElts.get(j-i);
				Object lkUpElt = null;
				try {
					lkUpElt = ic.lookup(jndiElt);
				} catch (NamingException e) {
					//KO
					out.println("<li><b>"+jndiElt.substring(14)+"</b>: <span class=\"red\"><b>KO</b></span></li>");
					continue;
				}
				if (lkUpElt instanceof URL) {
					//OK
					out.println("<li><b>"+jndiElt.substring(14)+"</b>: '"+lkUpElt.toString()+"'</li>");
				} else {
					//OK
					out.println("<li><b>"+jndiElt.substring(14)+"</b>: <span class=\"green\">OK</span></li>");
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}
%>
		</ul>
	</li>
</ul>
<%
	try {
		cn.close();
	} catch (Exception e) {
	}
%>
</div>
<p/>

<div class="objet">
<p>Logs application</p>
<ul>
<%
	ArrayList log4files = Log4JLoader.getInstance().getFileList();
	for (int i=log4files.size(), j=i; i>0; i--) {
		String logFile = (String)log4files.get(j-i);
		out.println("<li>");
		out.print("<a href=\""+request.getContextPath()+"/monitoring?logfile="+URLEncoder.encode(logFile, "UTF-8")+"\" >");
		out.print(logFile.substring(logFile.lastIndexOf("/")+1));
		out.println("</a>");
		out.println("</li>");
	}
%>
</ul>
</div>
<p/>
<div class="objet">
<p>Logs configuration</p>
<table>
<%
	ArrayList loggers = Log4JLoader.getInstance().getLoggers();
	for (int i=loggers.size(), j=i; i>0; i--) {
		Logger logger = (Logger)loggers.get(j-i);
%>
		<tr>
			<td width="25"></td>
			<td><%=logger.getName() %></td>
			<td>
				<select name="<%=logger.getName() %>" onchange="submitChangeLevel('<%=logger.getName() %>')">
					<option value="<%=Level.ALL_INT%>" <%=(Level.ALL_INT == logger.getEffectiveLevel().toInt()?"selected":"")%>><%=Level.ALL.toString() %></option>
					<%
					Level traceLevel = Level.toLevel("TRACE");
					if (Level.DEBUG_INT != traceLevel.toInt()) {
						out.print("<option value=\""+traceLevel.toInt()+"\" ");
						out.print((traceLevel.toInt() == logger.getEffectiveLevel().toInt()?"selected":"")+">");
						out.print(traceLevel.toString());
						out.println("</option>");
					}
					%>
					<option value="<%=Level.DEBUG_INT %>" <%=(Level.DEBUG_INT == logger.getEffectiveLevel().toInt()?"selected":"")%>><%=Level.DEBUG.toString() %></option>
					<option value="<%=Level.INFO_INT %>" <%=(Level.INFO_INT == logger.getEffectiveLevel().toInt()?"selected":"")%>><%=Level.INFO.toString() %></option>
					<option value="<%=Level.WARN_INT %>" <%=(Level.WARN_INT == logger.getEffectiveLevel().toInt()?"selected":"")%>><%=Level.WARN.toString() %></option>
					<option value="<%=Level.ERROR_INT %>" <%=(Level.ERROR_INT == logger.getEffectiveLevel().toInt()?"selected":"")%>><%=Level.ERROR.toString() %></option>
					<option value="<%=Level.FATAL_INT %>" <%=(Level.FATAL_INT == logger.getEffectiveLevel().toInt()?"selected":"")%>><%=Level.FATAL.toString() %></option>
					<option value="<%=Level.OFF_INT %>" <%=(Level.OFF_INT == logger.getEffectiveLevel().toInt()?"selected":"")%>><%=Level.OFF.toString() %></option>
				</select>
			</td>
		</tr>
<%	
	}
%>
</table>
</div>
</form>
</body>
</html>