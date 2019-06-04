/*
 * (c) 2013 FCCI Insurance Group All Rights Reserved.
 */
package com.rothsmith.test.ftp;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.sshd.SshServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.rothsmith.test.ftp.FtpTestException;
import com.rothsmith.test.ftp.SFtpTestServer;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * Tests for {@link SFtpTestServer}.
 * 
 * @author drothauser
 * 
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class SFtpTestServerTest {

	/**
	 * SLF4J Logger for SFtpTestServerTest.
	 */
	private static final Logger LOGGER = LoggerFactory
	    .getLogger(SFtpTestServerTest.class);

	/**
	 * SFTP/SSH Port.
	 */
	private static final int SFTP_PORT = 2222;

	// START SNIPPET: sftp-server-setup

	/**
	 * Test SFTP Server instance.
	 */
	private static SFtpTestServer sshServer;

	/**
	 * Start the test FTPS server.
	 * 
	 * @throws Exception
	 *             possible error
	 */
	@BeforeClass
	@SuppressWarnings("PMD.SignatureDeclareThrowsException")
	public static void setUpBeforeClass() throws Exception {

		sshServer = new SFtpTestServer("remote-username");
		sshServer.startServer();

		SshServer sshd = sshServer.getSshd();
		LOGGER.info("Started SSH Server, version " + sshd.getVersion());

	}

	// END SNIPPET: sftp-server-setup

	/**
	 * Test connect to the FTPS server and list files.
	 * 
	 * @throws FtpException
	 *             possible FTP problem
	 * @throws IOException
	 *             possible I/O error
	 */
	@Test
	public void testSFtpListFiles() throws FtpException, IOException {

		String user = "remote-username";
		String password = "remote-password";
		String server = "localhost";
		int port = SFTP_PORT;

		try {

			JSch jsch = new JSch();

			Hashtable<String, String> config = // NOPMD Jsch needs Hashtable
			    new Hashtable<String, String>();
			config.put("StrictHostKeyChecking", "no");
			JSch.setConfig(config);

			Session session = jsch.getSession(user, server, port);
			session.setPassword(password);

			session.connect();

			session.openChannel("sftp");

			ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");

			sftp.connect();

			LOGGER.info("SFTP Session Connected: " + session.isConnected());
			LOGGER.info("HOST: " + session.getHost());

			@SuppressWarnings("unchecked")
			Vector<LsEntry> dirlist = sftp.ls(sftp.pwd()); // NOPMD ls can only
			                                               // return a Vector.

			for (LsEntry lsEntry : dirlist) {
				LOGGER.info("File name = " + lsEntry.getFilename());
			}

		} catch (JSchException e) {
			String errmsg =
			    String.format(
			        "JSchException caught in testSFtpListFiles() with"
			            + " [server=%s,port=%d,user=%s,password=%s]: %s",
			        server, port, user, password, e);
			LOGGER.error(errmsg, e);
			fail(errmsg);
		} catch (SftpException e) {
			String errmsg =
			    String.format(
			        "SftpException caught in testSFtpListFiles() with"
			            + " [server=%s,port=%d,user=%s,password=%s]: %s",
			        server, port, user, password, e);
			LOGGER.error(errmsg, e);
			fail(errmsg);
		}

	}

	/**
	 * Test connect to the FTPS server using public key authentication.
	 * 
	 * @throws FtpException
	 *             possible FTP problem
	 * @throws IOException
	 *             possible I/O error
	 */
	@Test
	@Ignore
	public void testSFtpPubkeyAuth() throws FtpException, IOException {

		String user = "remote-username";
		String password = "remote-password";
		String server = "localhost";
		int port = SFTP_PORT;

		String privateKeyfile = "testid_rsa.key";
		String passphrase = "password";

		try {

			JSch jsch = new JSch();

			Hashtable<String, String> config = // NOPMD Jsch needs Hashtable
			    new Hashtable<String, String>();
			config.put("StrictHostKeyChecking", "no");
			JSch.setConfig(config);

			URL url = ClassLoader.getSystemResource(privateKeyfile);
			File pvtkeyFile = FileUtils.toFile(url);
			byte[] pvtkey = FileUtils.readFileToByteArray(pvtkeyFile);

			jsch.addIdentity(user, pvtkey, null,
			    passphrase.getBytes("UTF-8"));

			Session session = jsch.getSession(user, server, port);

			session.connect();

			ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");

			sftp.connect();

			LOGGER.info("SFTP Session Connected: " + session.isConnected());
			LOGGER.info("HOST: " + session.getHost());

			@SuppressWarnings("unchecked")
			Vector<LsEntry> dirlist = sftp.ls(sftp.pwd()); // NOPMD ls can only
			                                               // return a Vector.

			for (LsEntry lsEntry : dirlist) {
				LOGGER.info("File name = " + lsEntry.getFilename());
			}

		} catch (JSchException e) {
			String errmsg =
			    String.format(
			        "JSchException caught in testSFtpListFiles() with"
			            + " [server=%s,port=%d,user=%s,password=%s]: %s",
			        server, port, user, password, e);
			LOGGER.error(errmsg, e);
			fail(errmsg);
		} catch (SftpException e) {
			String errmsg =
			    String.format(
			        "SftpException caught in testSFtpListFiles() with"
			            + " [server=%s,port=%d,user=%s,password=%s]: %s",
			        server, port, user, password, e);
			LOGGER.error(errmsg, e);
			fail(errmsg);
		}

	}

	/**
	 * Test connect to the FTPS server with invalid user id.
	 * 
	 * @throws JSchException
	 *             possible JSch error
	 */
	@Test(expected = JSchException.class)
	public void testSFtpBadUser() throws JSchException {

		String user = "bogus";
		String password = "remote-password";
		String server = "localhost";
		int port = SFTP_PORT;

		JSch jsch = new JSch();

		Hashtable<String, String> config = // NOPMD Jsch needs Hashtable
		    new Hashtable<String, String>();
		config.put("StrictHostKeyChecking", "no");
		JSch.setConfig(config);

		Session session = jsch.getSession(user, server, port);
		session.setPassword(password);

		session.connect();

	}

	/**
	 * Test starting the server with a bogus server name.
	 * 
	 * @throws FtpTestException
	 *             possible server error
	 */
	@Test(expected = FtpTestException.class)
	public void testSSHServerBadStart() throws FtpTestException {

		sshServer.stopServer();
		sshServer.getSshd().setHost("bogus");
		sshServer.startServer();

	}

	// START SNIPPET: sftp-server-teardown

	/**
	 * Stop server after finished with all testing.
	 * 
	 * @throws FtpTestException
	 *             possible error shutting down the FTPS server
	 */
	@AfterClass
	public static void tearDownAfterClass() throws FtpTestException {

		sshServer.stopServer();
	}

	// END SNIPPET: sftp-server-teardown

}
