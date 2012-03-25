package org.colombbus.tangara.net;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <pre>
 * The aim of this class was to test multipart HTTP resquests on both side, i.e.
 * from the client to the server and from the server to the client.
 *
 * On client side, it is possible, via Commons HttpClient to send multipart
 * requests, it is also possible to receive multipart requests. Unfortunatly,
 * the parsing of the multipart is not done. So we have to develop it.
 *
 * On server side, the PHP document is not very clear about the subject. The use
 * of the magic variable $_POST should do the feature. TO TEST!
 *
 * WARN: This is an integration test that requires an external server to run, so
 * it is ignored at this moment
 * </pre>
 */
@Ignore
public class MultipartHTTPTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

//	private void test() {
//		File f = new File("/path/fileToUpload.txt");
//		PostMethod filePost = new PostMethod("http://host/some_path");
//		Part[] parts = new Part[2];
//		try {
//			parts[0] = new StringPart("hello", "world");
//			parts[1] = new FilePart(f.getName(), f);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			fail();
//		}
//		filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost
//				.getParams()));
//		HttpClient client = new HttpClient();
//		try {
//			int status = client.executeMethod(filePost);
//		} catch (HttpException e) {
//			e.printStackTrace();
//			fail();
//		} catch (IOException e) {
//			e.printStackTrace();
//			fail();
//		}
//	}

	@Test
	public void sendAndReceive() {
		PostMethod post = new PostMethod(
				"http://tangara.colombbus.org/multipart.php");
		Part[] parts = new Part[2];
		File f = new File("src/main/resources/org/colombbus/tangara/splash.png");
		assertTrue(f.exists());

		try {
			parts[0] = new StringPart("hello", "world");
			parts[1] = new FilePart("the_file", f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		}
		post.setRequestEntity(new MultipartRequestEntity(parts, post
				.getParams()));
		HttpClient client = new HttpClient();
		int status = 0;
		try {
			status = client.executeMethod(post);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		if (status != HttpStatus.SC_OK) {
			fail("Method failed: " + post.getStatusLine());
		}

	}

}
