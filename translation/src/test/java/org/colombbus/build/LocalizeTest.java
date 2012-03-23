package org.colombbus.build;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import org.colombbus.tangara.Usage;
import org.junit.*;

@SuppressWarnings("nls")
public class LocalizeTest {

	private static final String TRANSLATION_FILES = "translation_fr.properties,translation_en.properties,translation_es.properties";
	private static final String DELIM = ",";

	@Test
	public void testFullName() {
		assertEquals("org.colombbus.build.Localize", Localize.class.getName());
	}

	@Test
	public void testSplit() {
		List<String> r1 = splitToStringList(TRANSLATION_FILES, DELIM);
		List<String> r2 = splitToStringList2(TRANSLATION_FILES, DELIM);
		assertEquals(r1, r2);
	}

	private static List<String> splitToStringList(String string, String delim) {
		List<String> stringList = new ArrayList<String>();
		for (StringTokenizer tokenizer = new StringTokenizer(string, delim); tokenizer.hasMoreTokens();) {
			String fileName = tokenizer.nextToken();
			stringList.add(fileName);
		}
		return stringList;
	}

	private static List<String> splitToStringList2(String string, String delim) {
		String[] strArray = string.split(delim);
		return Arrays.asList(strArray);
	}

	@Test
	public void testParentSampleClass_fr() throws Exception {
		// verify class declared
		ClassLoader classLoader = getClass().getClassLoader();
		Class<?> translatedClass = classLoader.loadClass("org.colombbus.translation.sample.fr.ParentSample_fr");
		Object instance = translatedClass.newInstance();

		// verify method declared
		Method method = translatedClass.getMethod("methode1");
		Object returnedValue = method.invoke(instance);
		assertEquals("parent.method1", returnedValue);

		// verify Usage annotation declared
		Usage usage = method.getAnnotation(Usage.class);
		assertNotNull(usage);
		assertFalse(usage.first());
		assertEquals("m\u00E9thode1()", usage.value());
		assertEquals("m\u00E9thode1()", usage.prototype());
	}

	@Test
	public void testChildSampleClass_fr_method3WithArg() throws Exception {
		// verify class declared
		ClassLoader classLoader = getClass().getClassLoader();
		Class<?> translatedClass = classLoader.loadClass("org.colombbus.translation.sample.fr.ChildSample_fr");
		Object instance = translatedClass.newInstance();

		// verify method3 declared
		Method method3 = translatedClass.getMethod("methode3", String.class);
		Object returnedValue3 = method3.invoke(instance,"arg");
		assertEquals("child.method3", returnedValue3);

		// verify Usage annotation declared
		Usage usage = method3.getAnnotation(Usage.class);
		assertNotNull(usage);
		assertFalse(usage.first());
		assertEquals("m\u00E9thode3()", usage.value());
		assertEquals("m\u00E9thode3(\"l'argument\")", usage.prototype());
	}

	@Test
	public void testChildSampleClass_fr_method3NoArg() throws Exception {
		// verify class declared
		ClassLoader classLoader = getClass().getClassLoader();
		Class<?> translatedClass = classLoader.loadClass("org.colombbus.translation.sample.fr.ChildSample_fr");
		Object instance = translatedClass.newInstance();

		// verify method3 declared
		Method method3 = translatedClass.getMethod("methode3");
		Object returnedValue3 = method3.invoke(instance);
		assertEquals("child.method3", returnedValue3);

		// verify Usage annotation declared
		Usage usage = method3.getAnnotation(Usage.class);
		assertNotNull(usage);
		assertEquals("m\u00E9thode3()", usage.value());
		assertEquals("m\u00E9thode3()", usage.prototype());
		assertTrue(usage.first());
	}


	@Test
	public void testChildSampleClass_fr_constructorNoArg() throws Exception {
		// verify class declared
		ClassLoader classLoader = getClass().getClassLoader();
		Class<?> translatedClass = classLoader.loadClass("org.colombbus.translation.sample.fr.ChildSample_fr");

		// verify method3 declared
		Constructor<?> constructor = translatedClass.getConstructor();
		Object instance = constructor.newInstance();
		assertTrue( translatedClass.isInstance(instance));

		// verify Usage annotation declared
		Usage usage = constructor.getAnnotation(Usage.class);
		assertNull(usage);
	}
}
