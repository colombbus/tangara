package org.colombbus.tangara.net;

import java.lang.reflect.*;

import org.junit.*;
import static org.junit.Assert.*;

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
public class ProxyTest {


	public class MyInvocationHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Test
	public void testProxy() {
		InvocationHandler handler = null;
		Class<?> proxyClass = Proxy.getProxyClass(MyBean.class.getClassLoader(),
				new Class[] { InvocationHandler.class });
		MyBean f = null;
		try {
			f = (MyBean) proxyClass.getConstructor(
					new Class[] { InvocationHandler.class }).newInstance(
					new Object[] { handler });
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail();
		} catch (SecurityException e) {
			e.printStackTrace();
			fail();
		} catch (InstantiationException e) {
			e.printStackTrace();
			fail();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			fail();
		}

		assertNotNull(f);
	}

}
