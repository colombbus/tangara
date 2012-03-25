package org.colombbus.tangara;

import java.io.File;
import java.util.Properties;

import org.colombbus.tangara.Configuration;
import static org.junit.Assert.*;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.ObjectRegistry;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Dear reader, this test class is a mess and has not been maintained.
 *
 * <pre>
 * So you have two choices:
 * 	- leave this file code (without any consequences)
 * 	- read and fix this file code (there are consequencies)
 * </pre>
 */
@Ignore
public class ConfigurationTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BasicConfigurator.configure();
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

	@Test
	public void testLoad() {
		Configuration config = Configuration.instance();
		try {
			config.load();
		} catch (ConfigurationException e) {
			e.printStackTrace();
			fail();
		}
		assertTrue(BSFManager.isLanguageRegistered("BeanShell"));
	}

	@Test
	public void testEval() {
		Configuration config = Configuration.instance();
		try {
			config.load();
		} catch (ConfigurationException ex) {
			ex.printStackTrace();
			fail();
		}
		BSFManager bsfManager = new BSFManager();

		Integer i = new Integer(5);
		try {
			bsfManager.declareBean("i", i, Integer.class);
		} catch (BSFException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		BSFEngine engine = null;
		try {
			engine = bsfManager.loadScriptingEngine("BeanShell");
		} catch (BSFException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(engine);

		Integer j = new Integer(7);
		try {
			bsfManager.declareBean("j", j, Integer.class);
		} catch (BSFException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			engine.exec("source", 1, 1, "a=3;\nprint(a);");
			engine.exec("source", 1, 1, "a=a+1;\nprint(a);");
		} catch (BSFException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			engine.exec("source", 1, 1, "print(\"i=\"+i);");
			engine.exec("source", 1, 1, "j=j*10; print(\"j=\"+j);");
		} catch (BSFException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		// BSFDeclaredBean toto = new BSFDeclaredBean();
		// toto.beannew BSFDeclaredBean("i", i, Integer.class);
		ObjectRegistry registry = bsfManager.getObjectRegistry();
		Object jValue = registry.lookup("j");
		if (jValue != null) {
			System.out.println("j-value = " + jValue.toString());
		}
		registry.register("k", new Integer(1000));
		Object kValue = registry.lookup("k");
		assertNotNull(kValue);
		System.out.println("k=" + kValue.toString());
		try {
			engine.exec("source", 1, 1, "print(\"k=\"+k);");
		} catch (BSFException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		try {
			bsfManager.declareBean("k", kValue, Integer.class);
		} catch (BSFException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		try {
			engine.exec("source", 1, 1, "print(\"k=\"+k);");
		} catch (BSFException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		engine.terminate();
	}

	@Test
	public void testVarAccess() {
		Configuration config = Configuration.instance();
		try {
			config.load();
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
			fail();
		}
		BSFManager bsfManager = new BSFManager();
		BSFEngine engine = null;
		try {
			engine = bsfManager.loadScriptingEngine("BeanShell");
		} catch (BSFException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(engine);

		try {
			// ObjectRegistry registry = bsfManager.getObjectRegistry();
			// registry.register("mmm", new Integer(3));
			// engine.exec("source", 1, 1, "print( mmm );");
			engine.exec("source", 1, 1, "mmm = 1000;");
			engine.exec("source", 1, 1, "print( mmm );");
		} catch (BSFException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		engine.terminate();
	}

	@Test
	public void testDoubleProperties() {
		Properties props = new Properties();
		try {
			props.load(getClass().getResourceAsStream("conf1.properties"));
			props.load(getClass().getResourceAsStream("conf2.properties"));
			System.out.println("filename=" + props.getProperty("filename"));
		} catch (Exception ex) {
			fail();
		}

		assertEquals("value1", props.getProperty("name1"));
		assertEquals("value2", props.getProperty("name2"));
	}

	@Test
	public void testSysPropConf() {
		Configuration cfg = Configuration.instance();
		try {
			cfg.load();
		} catch (ConfigurationException e) {
			e.printStackTrace();
			fail();
		}
		assertNull(cfg.getProperty("name1"));
		System.setProperty(Configuration.CONF_SYS_P, "src/test/data/conf2.properties");
		Configuration cfg2 = Configuration.instance();
		try {
			cfg2.load();
		} catch (ConfigurationException e) {
			e.printStackTrace();
			fail();
		}
		assertEquals("value1", cfg2.getProperty("name1"));
		System.getProperties().remove("tangara.configuration");
	}

	@Test
	public void checkSomeProperties() {
		try {
			Configuration.instance().load();
		} catch (ConfigurationException e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(-1, Configuration.instance().getHistoryDepth());
		assertEquals(1, Configuration.instance().getInteger("tangara.level"));
	}

	@Test
	public void testExtension() {
		System.out.println(new File("tangara.jar").getName());
		assertTrue(FilenameUtils.isExtension(new File("tangara.jar").getName().toLowerCase(), "jar"));
		assertTrue(FilenameUtils.isExtension(new File("tangara.JAR").getName().toLowerCase(), "jar"));
	}

}
