package org.colombbus.objectpackager;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.colombbus.objectpackager.TObjectMetadataFactory.createMetadata;

@SuppressWarnings("nls")
public class TObjectMetadataLoaderTest {

	private static final File BASE_DIR = new File("src/test/resources/org/colombbus/objectpackager");

	private TObjectMetadataLoader loader;
	private List<TObjectMetadata> metadatas;

	@Before
	public void setUp() throws Exception {
		loader = new TObjectMetadataLoader();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoad_1_6_6_data() {
		loader.load(new File(BASE_DIR, "Translation_1.6.6.txt"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLoad_nullArg() {
		loader.load(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLoad_invalidFile() {
		loader.load(new File(BASE_DIR, "badTranslation.txt"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLoad_twice() {
		loader.load(new File(BASE_DIR, "Translation_1.6.6.txt"));
		loader.load(new File(BASE_DIR, "Translation_1.6.6.txt"));
	}

	@Test
	public void testGetAllMetadata_1_6_6_data() {
		loader.load(new File(BASE_DIR, "Translation_1.6.6.txt"));
		metadatas = loader.getAllMetadata();
		assertNotNull(metadatas);
		assertEquals(66, metadatas.size());

		assertMetadataDefined("Vector3D, ,Vecteur3D,Vector3D");
		assertMetadataDefined("Chronometer, ,Chronometre,Cronometro");
		assertMetadataDefined("Character,character,Personnage,Personaje");
	}

	private void assertMetadataDefined(String description) {
		TObjectMetadata expectedMetadata = createMetadata(description);
		assertTrue(metadatas.contains(expectedMetadata));
	}

	@Test
	public void testGetTranslations_noLoad() {
		metadatas = loader.getAllMetadata();
		assertNotNull(metadatas);
		assertTrue(metadatas.isEmpty());
	}
}
