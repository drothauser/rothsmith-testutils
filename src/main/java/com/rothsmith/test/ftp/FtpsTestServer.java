/*
 * (c) 2012 FCCI Insurance Group All Rights Reserved.
 */
package com.rothsmith.test.ftp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates an Apache FTPS server instance for testing.
 * 
 * @author drothauser
 * 
 */
public class FtpsTestServer {

	/**
	 * SLF4J Logger for FtpsTestServer.
	 */
	private static final Logger LOGGER = LoggerFactory
	    .getLogger(FtpsTestServer.class);

	/**
	 * Maximum number of concurrent logins.
	 */
	private static final int MAX_LOGINS = 30;

	/**
	 * FtpServer instance.
	 */
	private final FtpServer ftpServer;

	/**
	 * Constructor that initializes the FTP server.
	 * 
	 * @param port
	 *            FTP server port number
	 * @param keystoreType
	 *            Keystore file type e.g. JKS, PKCS12
	 * @param keystoreFilePath
	 *            Keystore file path
	 * @param keystorePassword
	 *            password for the keystore file
	 * @param username
	 *            FTP user
	 * @param password
	 *            FTP user password
	 * @param ftproot
	 *            FTP user root directory
	 * @throws FtpTestException
	 *             possible error starting the server
	 */
	public FtpsTestServer(int port, String keystoreType,
	    String keystoreFilePath, String keystorePassword, String username,
	    String password, String ftproot) throws FtpTestException {

		ftpServer =
		    createFtpsServer(port, keystoreType, keystoreFilePath,
		        keystorePassword, username, password, ftproot);
	}

	/**
	 * Create an instance of a FTPS server (FTP over SSL)
	 * 
	 * @param port
	 *            FTP server port number
	 * @param keystoreType
	 *            Keystore file type e.g. JKS, PKCS12
	 * @param keystoreFilePath
	 *            Keystore file path
	 * @param keystorePassword
	 *            password for the keystore file
	 * @param username
	 *            FTP user
	 * @param password
	 *            FTP user password
	 * @param ftproot
	 *            FTP user root directory
	 * 
	 * @return {@link FtpServer} instance configured for FTPS (FTP over SSL).
	 * @throws FtpTestException
	 *             possible error adding user
	 */
	private FtpServer createFtpsServer(int port, String keystoreType,
	    String keystoreFilePath, String keystorePassword, String username,
	    String password, String ftproot) throws FtpTestException {

		FtpServerFactory serverFactory = new FtpServerFactory();

		ConnectionConfigFactory connectionConfig =
		    new ConnectionConfigFactory();
		connectionConfig.setMaxLogins(MAX_LOGINS);
		serverFactory.setConnectionConfig(connectionConfig
		    .createConnectionConfig());

		ListenerFactory factory = new ListenerFactory();
		// set the port of the listener
		factory.setPort(port);

		// define SSL configuration
		SslConfigurationFactory ssl = new SslConfigurationFactory();
		ssl.setKeystoreType(keystoreType);
		ssl.setKeystoreFile(new File(keystoreFilePath));
		ssl.setKeystorePassword(keystorePassword);
		// set the SSL configuration for the listener
		factory.setSslConfiguration(ssl.createSslConfiguration());
		factory.setImplicitSsl(false);

		// replace the default listener
		serverFactory.addListener("default", factory.createListener());
		PropertiesUserManagerFactory userManagerFactory =
		    new PropertiesUserManagerFactory();

		// set up test user:
		UserManager userManager = userManagerFactory.createUserManager();
		// userManagerFactory.setFile(new File(
		// "src/test/resources/ftpsusers.properties"));
		// serverFactory.setUserManager(userManagerFactory.createUserManager());
		BaseUser user = new BaseUser();
		user.setName(username);
		user.setPassword(password);
		user.setHomeDirectory(ftproot);
		List<Authority> authorities = new ArrayList<Authority>();
		authorities.add(new WritePermission());
		user.setAuthorities(authorities);
		try {
			userManager.save(user);
		} catch (FtpException e) {
			String message = "Could not save FTP user: " + e;
			LOGGER.error(message, e);
			throw new FtpTestException(message, e);
		}
		serverFactory.setUserManager(userManager);

		return serverFactory.createServer();

	}

	/**
	 * Start the server.
	 * 
	 * @throws FtpTestException
	 *             Possible error starting the server
	 */
	public void startServer() throws FtpTestException {

		try {
			ftpServer.start();
		} catch (FtpException e) {
			String message = "Could not start FTPS server: " + e;
			LOGGER.error(message, e);
			throw new FtpTestException(message, e);
		}
	}

	/**
	 * Stop the server.
	 * 
	 * @throws FtpTestException
	 *             Possible error starting the server
	 */
	public void stopServer() throws FtpTestException {
		try {
			ftpServer.stop();
		} catch (Exception e) {
			String message = "Could not stop FTPS server: " + e;
			LOGGER.error(message, e);
			throw new FtpTestException(message, e);
		}
	}

	/**
	 * @return the ftpServer
	 */
	public FtpServer getFtpServer() {
		return ftpServer;
	}

}
