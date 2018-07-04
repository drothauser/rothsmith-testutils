/*
 * (c) 2012 FCCI Insurance Group All Rights Reserved.
 */
package com.rothsmith.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rothsmith.test.integration.IntegrationTestUtils;

/**
 * Tests for {@link IntegrationTestUtils}.
 * 
 * @author drothauser
 * 
 */
public class IntegrationTestUtilsTest {

	// START SNIPPET: jacoco-init

	/**
	 * Field to hold the JaCoCo code coverage javaagent JVM argument passed in
	 * from the Maven verify/pre-integration-test phase. See fcci-common/pom.xml
	 * - profile = it-coverage.
	 */
	private static String jaCoCoAgent;

	/**
	 * Initialize the jaCoCoAgent JVM argument for use when running integration
	 * tests.
	 */
	@BeforeClass
	public static void setUpBeforeClass() {

		jaCoCoAgent = IntegrationTestUtils.fetchJacocoAgent();

	}

	// END SNIPPET: jacoco-init

	/**
	 * Test method for
	 * {@link com.rothsmith.test.integration.IntegrationTestUtils#fetchJacocoAgent()}
	 * .
	 */
	@Test
	public void testFetchJacocoAgent() {

		String jacocoAgent = IntegrationTestUtils.fetchJacocoAgent();
		assertFalse("A JaCoCo agent was expected as a JVM parameter.",
		    StringUtils.isEmpty(jacocoAgent));

	}

	/**
	 * Test method for
	 * {@link IntegrationTestUtils#runIntegrationTest(List, File)}.
	 * 
	 * @throws IOException
	 *             possible I/O error
	 */
	@Test
	public void testRunIntegrationTest() throws IOException {

		// START SNIPPET: runIntegrationTest

		File jobrunnerBat = new File("target/jobrunner.bat");

		List<String> cmdList = new ArrayList<String>();
		cmdList.add("cmd");
		cmdList.add("/C");
		cmdList.add(jobrunnerBat.getCanonicalPath());
		cmdList.add("com.fcci.foo.FooJob");
		cmdList.add("dsfile=datasources.xml");
		cmdList.add("foo=bar");

		// JVM arguments to set memory limits and use the JaCoCo agent to
		// capture code coverage metrics:
		cmdList.add("javaopts");
		cmdList.add(String.format("\"%s\"", jaCoCoAgent));
		cmdList.add(String.format("\"%s %s\"", jaCoCoAgent,
		    "-Xms128m -Xmx256m -XX:MaxPermSize=128m"));

		File workingDir =
		    new File(FilenameUtils.getFullPath(jobrunnerBat
		        .getCanonicalPath()));

		// Use the IntegrationTestUtils.runIntegrationTest method to run the
		// test:
		int exitValue =
		    IntegrationTestUtils.runIntegrationTest(cmdList, workingDir);

		// END SNIPPET: runIntegrationTest

		// It's ok for this test to fail:
		assertEquals(1, exitValue);
	}

}
