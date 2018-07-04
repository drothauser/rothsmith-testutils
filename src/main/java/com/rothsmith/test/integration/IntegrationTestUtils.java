/*
 * (c) 2012 FCCI Insurance Group All Rights Reserved.
 */
package com.rothsmith.test.integration;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience methods for Junit and integration tests.
 * 
 * @author drothauser
 * 
 */
public final class IntegrationTestUtils {

	/**
	 * SLF4J Logger for FtpsTestUtils.
	 */
	private static final Logger LOGGER = LoggerFactory
	    .getLogger(IntegrationTestUtils.class);

	/**
	 * Private constructor to thwart instantiation of a utility class.
	 */
	private IntegrationTestUtils() {
	}

	/**
	 * This method grabs the JaCoCo -javaagent parameter from the JVM arguments.
	 * Note that this parameter will be passed in from the Maven
	 * verify/pre-integration-test phase. See fcci-common/pom.xml - profile =
	 * it-coverage.
	 * 
	 * @return JaCoCo -javaagent JVM argument
	 */
	public static String fetchJacocoAgent() {

		String jaCoCoAgent = "";

		RuntimeMXBean runtimemxBean = ManagementFactory.getRuntimeMXBean();
		List<String> aList = runtimemxBean.getInputArguments();
		for (int i = 0; i < aList.size(); i++) {
			String agentArg = "-javaagent";
			String jvmarg = aList.get(i);
			LOGGER.debug("JVM Arg = " + jvmarg);
			if (agentArg.equalsIgnoreCase(StringUtils.left(jvmarg,
			    agentArg.length()))) {
				jaCoCoAgent = jvmarg;
				LOGGER.info("Jacoco JavaAgent = " + jaCoCoAgent);
				break;
			}
		}

		if (StringUtils.isEmpty(jaCoCoAgent)) {
			LOGGER.info("No JaCoCo coverage agent was detected. "
			    + "Code coverage will not be performed.");
		}

		return jaCoCoAgent;
	}

	/**
	 * This method runs a command using the information in the cmdList
	 * parameter.
	 * 
	 * @param cmdList
	 *            A {@link List} containing the command to run and its
	 *            arguments.
	 * @param workingDir
	 *            The directory that the command will be run from.
	 * @return 0 if successful, otherwise 1
	 * @throws IOException
	 *             possible I/O error
	 */
	public static int runIntegrationTest(List<String> cmdList,
	    File workingDir) throws IOException {

		String[] cmdArray = cmdList.toArray(new String[cmdList.size()]);

		Process p = Runtime.getRuntime().exec(cmdArray, null, workingDir);

		List<String> lines = IOUtils.readLines(p.getInputStream());
		List<String> errlines = IOUtils.readLines(p.getErrorStream());
		LOGGER.info("stdout:\n" + StringUtils.join(lines, '\n'));
		String errtext = StringUtils.join(errlines, '\n');
		if (!StringUtils.isBlank(errtext)) {
			LOGGER.error("stderr:\n" + errtext);
		}
		return p.exitValue();
	}

}
