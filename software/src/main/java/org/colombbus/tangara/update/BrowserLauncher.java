package org.colombbus.tangara.update;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>
 * Bare Bones Browser Launch Version 2.0 (May 26, 2009) By Dem Pilafian
 * </p>
 * <p>
 * Supports: Mac OS X, GNU/Linux, Unix, Windows XP/Vista
 * </p>
 * <p>
 * Example Usage:
 * </p>
 *
 * <pre>
 * String url = &quot;http://www.centerkey.com/&quot;;
 * BrowserLauncher.openURL(url);
 * </pre>
 * <p>
 * Public Domain Software -- Free to Use as You Like
 * </p>
 *
 * @see <a href="http://www.centerkey.com/java/browser/">Original web site</a>
 */
class BrowserLauncher {
    private static final String APPLE_LAUNCHER_CLASS = "com.apple.eio.FileManager";//$NON-NLS-1$

    private static final String WINDOWS_LAUNCHER_CMD = "rundll32 url.dll,FileProtocolHandler ";//$NON-NLS-1$

    private static final String[] UNIX_BROWSER_LIST = { "firefox", //$NON-NLS-1$
            "opera", //$NON-NLS-1$
            "konqueror", //$NON-NLS-1$
            "epiphany",//$NON-NLS-1$
            "seamonkey",//$NON-NLS-1$
            "galeon",//$NON-NLS-1$
            "kazehakase", //$NON-NLS-1$
            "mozilla", //$NON-NLS-1$
            "netscape"//$NON-NLS-1$
    };

    public static void browse(String url) throws Exception {
        String osName = System.getProperty("os.name"); //$NON-NLS-1$

        if (osName.startsWith("Mac OS")) { //$NON-NLS-1$
            openURLOnApple(url);
        } else if (osName.startsWith("Windows")) { //$NON-NLS-1$
            openURLOnWindows(url);
        } else {
            openURLOnUnix(url);
        }
    }

    private static void openURLOnUnix(String url) throws InterruptedException, IOException,
            Exception {
        String browser = findUnixBrowser();
        Runtime.getRuntime().exec(new String[] { browser, url });
    }

    private static String findUnixBrowser() throws InterruptedException, IOException, Exception {
        for (String browser : UNIX_BROWSER_LIST) {
            String[] selectBrowserCmd = { "which", browser };//$NON-NLS-1$
            boolean browserFound = Runtime.getRuntime().exec(selectBrowserCmd).waitFor() == 0;
            if (browserFound)
                return browser;
        }
        throw new Exception("No browser found among " + UNIX_BROWSER_LIST.toString()); //$NON-NLS-1$
    }

    private static void openURLOnWindows(String url) throws IOException {
        Runtime.getRuntime().exec(WINDOWS_LAUNCHER_CMD + url);
    }

    private static void openURLOnApple(String url) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> fileMgr = Class.forName(APPLE_LAUNCHER_CLASS);

        final String methodName = "openURL";//$NON-NLS-1$
        final Class<?>[] methodArgTypes = { String.class };
        Method openURL = fileMgr.getDeclaredMethod(methodName, methodArgTypes);

        openURL.invoke(null, new Object[] { url });
    }

    private BrowserLauncher() {
        throw new IllegalStateException("singleton class"); //$NON-NLS-1$
    }

}
