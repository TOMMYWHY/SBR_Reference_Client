
package au.gov.sbr.core.client.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.util.HashMap;
import java.util.Map;

import java.net.URLConnection;

/**
 * Creates the singleton and loads the ContentType resource files from the
 * assembly.
 * 
 * @author SBR
 */
public class FileUtil {

	private static final Map<String, String> contentTypeMap;

	static {
		contentTypeMap = new HashMap<String, String>();
		// Load properties file from class path
	}

	/**
	 * Returns the ContentType as nothing if the file name stated does not
	 * exist.
	 * 
	 * @param filename
	 *            name of the file
	 * @return an empty String.
	 */
	public static String getContentType(String filename) {
		
		String type = null;

		try
		{
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			type = fileNameMap.getContentTypeFor(filename);
			
			return type;
		}
		catch (Exception e)
		{
			return type;
		}
	}

	/**
	 * Returns the ContentType of the file.
	 * 
	 * @param file
	 *            name of the file
	 * @return the ContentType of the file.
	 */
	public static String getContentType(File file) {
		return getContentType(file.getPath());
	}

	/**
	 * Returns the ContentType Map.
	 * 
	 * @return the ContentType Map.
	 */
	public static Map<String, String> getContentTypeMap() {
		return contentTypeMap;
	}

	/** 
	 * Safely close multiple inputstreams, outputstreams, readers and writers.
	 * @param cl Closeable interfaces
	 */
	public static void safeClose(final Closeable... cl) {
		if (cl == null) {
			throw new IllegalArgumentException("Closeables must not be null.");
		}

		for (Closeable c : cl) {
			try {
				c.close();
			} catch (IOException io) {
				//do nothing
			}
		}
	}	
}
