package org.colombbus.objectpackager;

import static org.junit.Assert.*;
import java.io.File;
import java.util.List;

import org.junit.*;
import static org.colombbus.objectpackager.TObjectMetadataFactory.createMetadata;

@SuppressWarnings("nls")
public class FileObjectCollectorTest {

	private static final File TEST1_DIR = new File("src/test/data/org.colombbus.objectpackage.FileObjectCollector/test1");
	private FileObjectCollector collector;
	private TObjectMetadata objectMetadata;

	@Before
	public void setUp() throws Exception {
		collector = new FileObjectCollector(TEST1_DIR);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	@Test
	public void testFileObjectCollector() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testCollect() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testGetFiles() {
		fail("Not yet implemented");
	}

	@Test
	public void testComplete_writeMessage_1_6_6_data() {
		objectMetadata = createMetadata("WriteMessages, ,EcritureMessages,EscrituraMensajes");
		collector.collect(objectMetadata);
		assertCollectedFiles("WriteMessages.class", "WriteMessages$1.class", "WriteMessages$2.class", "en/WriteMessages.class",
				"fr/EcritureMessages.class", "es/EscrituraMensajes.class");
	}

	private void assertCollectedFiles(String... expectedRelativePaths) {
		List<File> actualFiles = collector.getFiles();
		assertEquals(expectedRelativePaths.length, actualFiles.size());
		for (String expectedRelativePath : expectedRelativePaths) {
			String filePath = "org/colombbus/tangara/objects/" + expectedRelativePath;
			File expectedFile = new File(TEST1_DIR, filePath);
			assertTrue("File not found: " + expectedFile.getPath(), actualFiles.contains(expectedFile));
		}
	}

	@Test
	public void testComplete_sprite_1_6_6_data() {
		objectMetadata = createMetadata("Sprite,sprite,Animation,Animacion");
		collector.collect(objectMetadata);
		assertCollectedFiles("Sprite.class", "Sprite$1.class", "Sprite$PictureDraggingMouse.class",
				"Sprite$PictureDraggingMouseMotion.class", "Sprite$TransparentFilter.class", "sprite/CollisionManager.class",
				"sprite/DefaultCollisionManager.class", "sprite/EllipticalCollisionManager.class",
				"sprite/RectangularCollisionManager.class", "sprite/SpriteMovement.class", "en/Sprite.class", "fr/Animation.class",
				"es/Animacion.class");
	}
}
