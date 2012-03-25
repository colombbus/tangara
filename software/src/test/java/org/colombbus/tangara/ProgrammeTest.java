package org.colombbus.tangara;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bsh.EvalError;
import bsh.Interpreter;

public class ProgrammeTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// @Test
	public void testStreamRedirection() {
		System.out.print("coucou");
		assertEquals(0, Program.instance().getOutContent().length());
		assertEquals(0, Program.instance().getErrContent().length());
		Program.init();
		System.out.print("hello");
		assertEquals("hello", Program.instance().getOutContent());
		Program.end();
	}

	@Test
	public void testParser() {
		Interpreter bsh = new Interpreter();

		// Evaluate statements and expressions
		try {
			bsh.eval("foo=Math.sin(0.5)");
			bsh.eval("bar=foo*5; bar=Math.cos(bar);");
		} catch (EvalError e) {
			e.printStackTrace();
			fail();
		}
	}

}
