package org.colombbus.objectpackager;

import static org.junit.Assert.*;
import static org.colombbus.objectpackager.TObjectMetadataFactory.createMetadata;
import org.junit.*;

@SuppressWarnings("nls")
public class TObjectMetadataFactoryTest {

	private TObjectMetadata metadata;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateTranslation_nullArg() {
		createMetadata(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateMetadata_tooManyFields() {
		createMetadata("SprintEN, sprint, sprintFR, SprintES, SprintBR");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateMetadata_notEnoughFields() {
		createMetadata("SprintEN, sprint, sprintFR");
	}

	@Test
	public void testCreateMetadata_noPackage() {
		metadata = createMetadata("Sound, ,Son,Sonido");
		assertMetadata("Sound", null, "Son", "Sonido");
	}

	private void assertMetadata(String expectedEnglishClassname, String expectedPackagename, String expectedFrenchClassname,
			String expectedSpanishClassname) {
		assertNotNull(metadata);
		assertEquals(expectedEnglishClassname, metadata.getI18NObjectName(Language.ENGLISH));
		assertEquals(expectedPackagename, metadata.getPackageName());
		assertEquals(expectedFrenchClassname, metadata.getI18NObjectName(Language.FRENCH));
		assertEquals(expectedSpanishClassname, metadata.getI18NObjectName(Language.SPANISH));
	}

	@Test
	public void testCreateMetadata_withPackage() {
		metadata = createMetadata("Sprite,sprite,Animation,Animacion");
		assertMetadata("Sprite", "sprite", "Animation", "Animacion");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateMetadata_emptyClassname() {
		createMetadata("Sprite,sprite,,Animacion");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateMetadata_badClassname() {
		createMetadata("Sprite,sprite,animation,Animacion");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateMetadata_badPackagename() {
		createMetadata("Sprite,spritE,Animation,Animacion");
	}
}