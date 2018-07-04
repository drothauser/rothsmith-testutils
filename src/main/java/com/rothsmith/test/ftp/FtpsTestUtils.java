/*
 * (c) 2012 FCCI Insurance Group All Rights Reserved.
 */
package com.rothsmith.test.ftp;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ftpserver.ftplet.FtpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rothsmith.properties.PropertyFileInitializer;

/**
 * Convenience methods for running FTPS tests.
 * 
 * @author drothauser
 * 
 */
public final class FtpsTestUtils {

	/**
	 * SLF4J Logger for FtpsTestUtils.
	 */
	private static final Logger LOGGER = LoggerFactory
	    .getLogger(FtpsTestUtils.class);

	/**
	 * Private constructor to thwart instantiation of a utility class.
	 */
	private FtpsTestUtils() {
	}

	/**
	 * Method to configure an instance of {@link FtpsTestServer} using the
	 * specified properties file.
	 * 
	 * @param propsFile
	 *            properties file name expected to be found in the classpath
	 * @return {@link FtpsTestServer} instance
	 * @throws FtpTestException
	 *             error configuring FTPS test server
	 */
	@SuppressWarnings("PMD.SignatureDeclareThrowsException")
	public static FtpsTestServer createTestFtpsServer(String propsFile)
	        throws FtpTestException {

		try {

			Properties properties =
			    PropertyFileInitializer.initPropertiesClasspath(propsFile);
			int port =
			    NumberUtils.createInteger((String) properties
			        .get("ftp.port"));
			String user = (String) properties.get("ftp.user");
			String password = (String) properties.get("ftp.password");
			String homedir = (String) properties.get("ftp.homedir");
			String storeType = (String) properties.get("keystore.type");
			String storePath =
			    findKeystoreFileClasspath((String) properties
			        .get("keystore.file"));
			String storePass = (String) properties.get("keystore.password");
			return new FtpsTestServer(port, storeType, storePath, storePass,
			    user, password, homedir);

		} catch (org.apache.ftpserver.ftplet.FtpException e) {
			String message = "Unable to configure FtpsTestServer: " + e;
			LOGGER.error(message, e);
			throw new FtpTestException(message, e);
		}
	}

	/**
	 * Given the keystore file name, return the absolute file path found in the
	 * classpath.
	 * 
	 * @param keystoreFile
	 *            the name of the keystore file to find
	 * @return the absolute file path of the keystore file
	 * @throws FtpException
	 *             thrown if the keystore file cannot be found in the classpath
	 */
	private static String findKeystoreFileClasspath(String keystoreFile)
	        throws FtpException {
		URL url = ClassLoader.getSystemResource(keystoreFile);
		if (url == null) {
			throw new FtpException(String.format(
			    "Could not find the keystore file - %s, in the classpath",
			    keystoreFile));
		}
		File datasourceFile = new File(url.getFile());
		return datasourceFile.getAbsolutePath();
	}

}
