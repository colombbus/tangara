package org.colombbus.tangara.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.KeyStroke;

import org.apache.log4j.BasicConfigurator;
import org.colombbus.tangara.TypedResourceBundle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TypedResourceBundleTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BasicConfigurator.configure();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		bundle = new TypedResourceBundle(
				"org.colombbus.tangara.util.typedresourcebundletest");
	}

	@After
	public void tearDown() throws Exception {
	}

	// @Test
	// public void testTypedResourceBundleString() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testTypedResourceBundleResourceBundle() {
	// fail("Not yet implemented");
	// }

	@Test
	public void testGetString() {
		assertEquals("Hello, world", bundle.getString("test.message"));
	}

	@Test
	public void testGetFont() {
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		Font[] fonts = ge.getAllFonts();
		if (fonts != null) {
			for (Font font : fonts) {
				System.out.println(font.getFamily());
			}
		}

		Font font=null;
		try {
			font = bundle.getFont("test.font1");
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		}
		assertNotNull(font);
		assertEquals("Arial", font.getFamily());
		assertTrue(font.isItalic());
		assertEquals(16, font.getSize());

		try {
			font = bundle.getFont("test.font2");
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		}
		assertNotNull(font);
		assertEquals("Times New Roman", font.getFamily());
		assertTrue(font.isPlain());
		assertEquals(30, font.getSize());

		Font f = Font.decode("SansSerif-20");
		assertEquals(20, f.getSize());
		assertEquals("SansSerif", f.getFamily());

		Font f2Model = new Font("Lucida Grande", Font.PLAIN, 13);
		Font f2Bundle=null;
		try {
			f2Bundle = bundle.getFont("test.font3");
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		}
		assertEquals(f2Model, f2Bundle);
	}

	@Test
	public void testGetColor() {

		Color bundleColor=null;
		try {
			bundleColor = bundle.getColor("test.color1");
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		}
		Color orgColor = new Color(50, 100, 200);
		assertEquals(orgColor, bundleColor);

		// assertEquals(50, color.getRed());
		// assertEquals(100, color.getGreen());
		// assertEquals(200, color.getBlue());
		// assertEquals(255, color.getAlpha());
		//
		// color = bundle.getColor("test.color2");
		// assertEquals(50, color.getRed());
		// assertEquals(100, color.getGreen());
		// assertEquals(200, color.getBlue());
		// assertEquals(150, color.getAlpha());
		//
		// color = bundle.getColor("test.color3");
		// assertEquals(Color.BLACK, color);
		//
		// color = bundle.getColor("test.color4");
		// assertEquals(Color.BLUE, color);
	}

	@Test
	public void testGetColor2() {
		String colorStr = "#FF00FF";
		Pattern p = Pattern.compile("#[0-9A-F]*");
		assertTrue(p.matcher(colorStr).matches());
		assertEquals(7, colorStr.length());

		// int colorInt = Integer.parseInt(colorStr.substring(1));
		Color color = parseColor(colorStr);
		assertEquals(15 * 16 + 15, color.getRed());
		assertEquals(0, color.getGreen());
		assertEquals(15 * 16 + 15, color.getBlue());
	}

	private Color parseColor(String value) {
		Pattern p = Pattern.compile("#[0-9A-F]*");
		if (p.matcher(value).matches() && value.length() == 7) {
			String redHex = value.substring(1, 3);
			String greenHex = value.substring(3, 5);
			String blueHex = value.substring(5, 7);
			int red = Integer.parseInt(redHex, 16);
			int green = Integer.parseInt(greenHex, 16);
			int blue = Integer.parseInt(blueHex, 16);
			return new Color(red, green, blue);
		}
		return null;
	}

	@Test
	public void testLoadAction() {

	}

	@Test
	public void testKeystroke() {
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_X,
				InputEvent.ALT_DOWN_MASK);
		KeyStroke ks2 = KeyStroke.getKeyStroke("alt X");
		assertEquals(ks, ks2);

		int kc = KeyStroke.getKeyStroke("X").getKeyCode();
		assertEquals(KeyEvent.VK_X, kc);

		KeyStroke kc3 = KeyStroke.getKeyStroke("meta X");
		KeyStroke ks3 = KeyStroke.getKeyStroke(KeyEvent.VK_X,
				InputEvent.META_DOWN_MASK);
		assertEquals(ks3, kc3);
	}


	@Test
	public void testKeystrokeReplacement() {
		int ctrlAlt = InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK;
		int metaAlt = (ctrlAlt & ~InputEvent.CTRL_DOWN_MASK)
				| InputEvent.META_DOWN_MASK;
		assertEquals(InputEvent.ALT_DOWN_MASK | InputEvent.META_DOWN_MASK,
				metaAlt);
	}

	private TypedResourceBundle bundle;
}
