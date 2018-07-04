/*
 * (c) 2012 FCCI Insurance Group All Rights Reserved.
 */
package com.rothsmith.test.ftp;

/**
 * Exception class for FTP related errors during testing.
 * 
 * @author drothauser
 * 
 */
public class FtpTestException
        extends Exception {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = -3416814412046024624L;

	/**
	 * Constructor for FtpTestException.
	 * 
	 * @param message
	 *            Message text explaining the exception.
	 */
	public FtpTestException(final String message) {
		super(message);
	}

	/**
	 * Constructor for FtpTestException.
	 * 
	 * @param message
	 *            Message text explaining the exception.
	 * @param e
	 *            root cause of this exception.
	 */
	@SuppressWarnings("checkstyle:parametername")
	public FtpTestException(final String message, final Throwable e) {
		super(message, e);
	}
}
