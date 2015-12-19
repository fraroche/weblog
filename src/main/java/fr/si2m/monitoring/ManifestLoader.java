package fr.si2m.monitoring;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class ManifestLoader {
	private static Logger logger = null;
	
	private static Manifest manifest;
	static {
		logger = Logger.getLogger(ManifestLoader.class);
		manifest = getWebAppManifest(ManifestLoader.class);
		if (manifest != null) {
			logger.debug("WebApp - Manifest-Version: "+manifest.getMainAttributes().getValue("Manifest-Version"));
		} else {
			logger.info("Manifest is null");
		}
	}
	
	public static Manifest getManifest() {
		return manifest;
	}
	public static Manifest getWebAppManifest(final Class myClass) {
		Manifest man = null;
		try {
			final URI root = ContextUtil.getClassRootUri(myClass);
			String rootPath = root.toString();
			final int end = rootPath.lastIndexOf("WEB-INF");
			if (end == -1) {
				return null;
			}
			final String endPath = rootPath.substring(end);
			rootPath = rootPath.substring(0,end);
			for(final Matcher recherche = Pattern.compile("!").matcher(endPath);
				recherche.find();) {
				rootPath = rootPath.substring(rootPath.indexOf(":")+1);
			}
			final URL manifestUrl = new URL(rootPath+JarFile.MANIFEST_NAME);
			man = new Manifest(manifestUrl.openStream());
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return man;
	}
}