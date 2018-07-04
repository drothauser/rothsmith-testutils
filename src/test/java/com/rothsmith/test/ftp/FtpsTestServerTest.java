/*
 * (c) 2013 FCCI Insurance Group All Rights Reserved.
 */
package com.rothsmith.test.ftp;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.net.ssl.KeyManager;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.KeyManagerUtils;
import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rothsmith.properties.PropertyFileInitializer;
import com.rothsmith.test.ftp.FtpTestException;
import com.rothsmith.test.ftp.FtpsTestServer;
import com.rothsmith.test.ftp.FtpsTestUtils;

/**
 * Tests for {@link FtpsTestServer}.
 * 
 * @author drothauser
 * 
 */
public class FtpsTestServerTest {

	/**
	 * SLF4J Logger for FtpsTestServerTest.
	 */
	private static final Logger LOGGER = LoggerFactory
	    .getLogger(FtpsTestServerTest.class);

	/**
	 * FTPS test properties file.
	 */
	private static final String FTPS_PROPERTIES = "ftpstest.properties";

	/**
	 * FTPS test properties file with bad certificate property.
	 */
	private static final String FTPS_PROPERTIES_BAD =
	    "ftpstest-bad.properties";

	// START SNIPPET: ftps-server-setup

	/**
	 * Test FTPS Server instance.
	 */
	private static FtpsTestServer ftpsTestServer;

	/**
	 * Start the test FTPS server.
	 * 
	 * @throws Exception
	 *             possible error
	 */
	@BeforeClass
	@SuppressWarnings("PMD.SignatureDeclareThrowsException")
	public static void setUpBeforeClass() throws Exception {

		ftpsTestServer = FtpsTestUtils.createTestFtpsServer(FTPS_PROPERTIES);
		ftpsTestServer.startServer();

	}

	// END SNIPPET: ftps-server-setup

	/**
	 * Test connect to the FTPS server and list files.
	 * 
	 * @throws FtpException
	 *             possible FTP problem
	 * @throws IOException
	 *             possible I/O error
	 */
	@Test
	public void testFtpsListFiles() throws FtpException, IOException {

		Properties properties =
		    PropertyFileInitializer.initPropertiesClasspath(FTPS_PROPERTIES);

		FTPSClient ftpsClient = new FTPSClient();

		String ftpServer = (String) properties.get("ftp.server");
		int port =
		    NumberUtils.createInteger((String) properties.get("ftp.port"));
		String user = (String) properties.get("ftp.user");
		String password = (String) properties.get("ftp.password");
		String storeType = (String) properties.get("keystore.type");
		String storePath =
		    findKeystoreFileClasspath((String) properties
		        .get("keystore.file"));
		String storePass = (String) properties.get("keystore.password");
		String keyAlias = (String) properties.get("key.alias");
		String keyPass = (String) properties.get("key.password");

		System.setProperty(FTPClient.FTP_SYSTEM_TYPE, "WINDOWS");
		System.setProperty(FTPClient.FTP_SYSTEM_TYPE_DEFAULT, "WINDOWS");

		ftpsClient.setTrustManager(TrustManagerUtils
		    .getAcceptAllTrustManager());

		File storePathFile = new File(storePath);

		KeyManager km;
		try {
			km =
			    KeyManagerUtils.createClientKeyManager(storeType,
			        storePathFile, storePass, keyAlias, keyPass);
		} catch (IOException e) {
			String msg =
			    "IOException caught initializing key manager. "
			        + "Keystore path = " + storePathFile.getAbsolutePath();
			LOGGER.error(msg, e);
			throw new FtpException(msg, e);
		} catch (GeneralSecurityException e) {
			String msg =
			    "GeneralSecurityException caught initializing key manager. "
			        + "Keystore path = " + storePathFile.getAbsolutePath();
			LOGGER.error(msg, e);
			throw new FtpException(msg, e);
		} catch (Exception e) {
			// Catching raw exception to handle a NullPointerException
			// that's
			// thrown when the key alias is bad.
			String msg =
			    "Exception caught initializing key manager. "
			        + "Keystore path = " + storePathFile.getAbsolutePath();
			LOGGER.error(msg, e);
			throw new FtpException(msg, e);
		}

		ftpsClient.setKeyManager(km);

		ftpsClient.connect(ftpServer, port);
		LOGGER.info(ftpsClient.getReplyString());
		ftpsClient.login(user, password);
		LOGGER.info(ftpsClient.getReplyString());

		FTPFile[] ftpFiles = ftpsClient.listFiles();
		for (int i = 0; i < ftpFiles.length; i++) {
			LOGGER.info("File name = " + ftpFiles[i].getName());
		}

		assertFalse("FTPS server should not be stopped", ftpsTestServer
		    .getFtpServer().isStopped());

	}

	/**
	 * Test starting the FTPS server with bad properties.
	 * 
	 * @throws FtpTestException
	 *             possible FTPS server problem
	 */
	@Test(expected = FtpTestException.class)
	public void testStartServerBadProperties() throws FtpTestException {

		ftpsTestServer =
		    FtpsTestUtils.createTestFtpsServer(FTPS_PROPERTIES_BAD);
		ftpsTestServer.startServer();

	}

	// START SNIPPET: ftps-server-teardown

	/**
	 * Stop server after finished with all testing.
	 * 
	 * @throws FtpTestException
	 *             possible error shutting down the FTPS server
	 */
	@AfterClass
	public static void tearDownAfterClass() throws FtpTestException {

		ftpsTestServer.stopServer();
	}

	// END SNIPPET: ftps-server-teardown

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
	private String findKeystoreFileClasspath(String keystoreFile)
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
