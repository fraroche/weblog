package fr.si2m.monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Stack;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class ContextUtil {

	private static ArrayList	localNameSpaceList	= null;

	/**
	 * This method returns the root path of <tt>myClass</tt> input parameter.
	 * 
	 * Example let assume the input class is </tt>fr.common.util.Foo.class</tt>:
	 * <li> 'jar:file:/..../toto.jar!/fr/common/util/Foo.class' -->
	 * file:/..../toto.jar </li>
	 * <li> 'file:/..../WEB-INF/classes/fr/common/util/Foo.class' -->
	 * file:/..../WEB-INF/classes </li>
	 * 
	 * @param myClass:
	 *            the input class
	 * @return root path of the input class
	 * @throws URISyntaxException
	 */
	public static URI getClassRootUri(final Class myClass) throws URISyntaxException {
		return myClass.getProtectionDomain().getCodeSource().getLocation().toURI();
	}

	public static Manifest getManifestFromJar(final URL jarUrl) throws IOException {
		return getManifestFromJar(jarUrl.openStream());
	}

	public static Manifest getManifestFromJar(final InputStream jarInputStream)
			throws IOException
			{
		final ZipInputStream jis = new ZipInputStream(jarInputStream);
		Manifest man = null;
		for (ZipEntry je = jis.getNextEntry(); je != null; je = jis.getNextEntry()) {
			if (JarFile.MANIFEST_NAME.equalsIgnoreCase(je.getName())) {
				man = new Manifest(jis);
				break;
			}
		}
		return man;
			}

	/**
	 * This method returns all the jndi path elements declared in web.xml
	 * 
	 * @return
	 */
	public static ArrayList getLocalNameSpaceElements() {
		if (localNameSpaceList == null) {
			synchronized (ContextUtil.class) {
				if (localNameSpaceList == null) {
					localNameSpaceList = getLocalJndiElements();
				}
			}
		}
		return localNameSpaceList;
	}

	private static ArrayList getLocalJndiElements() {
		final ArrayList localNameSpaceList = new ArrayList();
		try {
			final InitialContext ic = new InitialContext();
			Object ctx = null;
			final Stack stack = new Stack();
			stack.push("java:comp/env");
			while (!stack.empty()) {
				final String root = (String) stack.pop();
				if ((ctx = ic.lookup(root)) instanceof Context) {
					for (final NamingEnumeration enumCtx = ((Context) ctx).list(""); enumCtx.hasMoreElements();) {
						final NameClassPair elt = (NameClassPair) enumCtx.next();
						stack.push(root + "/" + elt.getName());
					}
				} else {
					localNameSpaceList.add(root);
					continue;
				}
			}
		} catch (final NamingException e) {
			e.printStackTrace();
		}
		return localNameSpaceList;
	}
}
