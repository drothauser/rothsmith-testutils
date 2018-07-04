/*
 * (c) 2013 FCCI Insurance Group All Rights Reserved.
 */
package com.rothsmith.test.ftp;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates an Apache Mina SSH server for testing.
 * 
 * @author drothauser
 * 
 */
public class SFtpTestServer {

	/**
	 * SLF4J Logger for SFtpTestServer.
	 */
	private static final Logger LOGGER =
	    LoggerFactory.getLogger(SFtpTestServer.class);

	/**
	 * Default SFTP/SSH port.
	 */
	public static final int DEFAULT_PORT = 22;

	/**
	 * Default SFTP server host.
	 */
	public static final String DEFAULT_HOST = "localhost";

	/**
	 * Default SFTP server host.
	 */
	public static final String DEFAULT_USER = "fccitest";

	/**
	 * {@link SshServer} instance for testing SFTP.
	 */
	private SshServer sshd;

	/**
	 * Constructor that initializes the SFTP server using the default server
	 * host (localhost), SSH port and a user id.
	 * 
	 * @param user
	 *            FTP user account id
	 * 
	 * @throws FtpTestException
	 *             possible error starting the server
	 */
	public SFtpTestServer(final String user) throws FtpTestException {

		this(DEFAULT_HOST, DEFAULT_PORT, user);
	}

	/**
	 * Constructor that initializes the SFTP server using the default server
	 * host (localhost), SSH port and a user id.
	 * 
	 * @throws FtpTestException
	 *             possible error starting the server
	 */
	public SFtpTestServer() throws FtpTestException {

		this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_USER);
	}

	/**
	 * Constructor that initializes the SFTP server.
	 * 
	 * @param host
	 *            SFTP server host
	 * @param port
	 *            SSH/SFTP server port number
	 * @param user
	 *            FTP user account id
	 * @throws FtpTestException
	 *             possible error starting the server
	 */
	public SFtpTestServer(final String host, final int port, final String user)
	        throws FtpTestException {

		sshd = SshServer.setUpDefaultServer();
		sshd.setHost(host);
		sshd.setPort(port);
		sshd.setKeyPairProvider(
		    new SimpleGeneratorHostKeyProvider("hostkey.ser"));

		sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
			@Override
			public boolean authenticate(String username, String pwd,
		        ServerSession session) {
				LOGGER.info("Authenticating user...");
				// Very lenient authentication process :-) no password check.
				return StringUtils.equals(username, user);
			}
		});

		sshd.setPublickeyAuthenticator(new PublickeyAuthenticator() {
			@Override
			public boolean authenticate(String username, PublicKey key,
		        ServerSession session) {
				LOGGER.info("Authenticating user and public key...");
				// Very lenient authentication process :-) no key verification.
				return StringUtils.equals(username, user);
			}
		});

		sshd.setCommandFactory(new ScpCommandFactory());

		List<NamedFactory<Command>> namedFactoryList =
		    new ArrayList<NamedFactory<Command>>();
		namedFactoryList.add(new SftpSubsystem.Factory());
		sshd.setSubsystemFactories(namedFactoryList);

		String defaultHomeDir = FileUtils.getTempDirectoryPath() + "sftp-test";
		try {
			FileUtils.forceMkdir(new File(defaultHomeDir));
			LOGGER.info("Created home directory: " + defaultHomeDir);
		} catch (IOException e) {
			throw new FtpTestException(e.getMessage(), e);
		}

		sshd.setFileSystemFactory(new VirtualFileSystemFactory(defaultHomeDir));

	}

	/**
	 * Start the server.
	 * 
	 * @throws FtpTestException
	 *             Possible error starting the server
	 */
	public void startServer() throws FtpTestException {

		try {
			sshd.start();
		} catch (IOException e) {
			String message = "Could not start SFTP server: " + e;
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
			sshd.stop();
		} catch (Exception e) {
			String message = "Could not stop SFTP server: " + e;
			LOGGER.error(message, e);
			throw new FtpTestException(message, e);
		}
	}

	/**
	 * @return The instance of the SFTP/SSH server
	 */
	public SshServer getSshd() {
		return sshd;
	}

	/**
	 * Manually start up a SFTP server.
	 * 
	 * @param args
	 *            command line arguments
	 * @throws FtpTestException
	 *             possible error running the server
	 */
	public static void main(String[] args) throws FtpTestException {

		Options options = new Options();
		options.addOption(Option.builder("h").argName("host").longOpt("host")
		    .hasArg().desc("Server host").required(false).build());
		options.addOption(Option.builder("p").argName("port").longOpt("host")
		    .hasArg().desc("Server port").required(false).type(Integer.class)
		    .build());
		options.addOption(Option.builder("u").argName("user").longOpt("user")
		    .hasArg().desc("Server user id").required(false).build());
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(options, args);
			String host =
			    cmd.hasOption('h') ? cmd.getOptionValue('h') : DEFAULT_HOST;
			int port = cmd.hasOption('p')
			    ? Integer.parseInt(cmd.getOptionValue('p')) : DEFAULT_PORT;
			String user =
			    cmd.hasOption('u') ? cmd.getOptionValue('u') : DEFAULT_USER;

			SFtpTestServer sshServer = new SFtpTestServer(host, port, user);
			sshServer.startServer();

			SshServer sshd = sshServer.getSshd();
			LOGGER.info(String.format(
			    "%nStarted SSH Server, Version %s%n"
			        + "host=%s%nport=%s%nuser=%s%n",
			    sshd.getVersion(), host, port, user));
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("SFtpTestServer", options);
			throw new FtpTestException("Unable to parse commandline arguments");
		}
	}

}
