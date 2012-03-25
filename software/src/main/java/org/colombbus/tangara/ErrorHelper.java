package org.colombbus.tangara;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bsf.BSFException;
import org.apache.log4j.Logger;

public class ErrorHelper {
	
    private static final Pattern regex_java3d_1 = Pattern.compile(".*\\Qcom/sun/j3d/\\E.*",Pattern.DOTALL);
    private static final Pattern regex_java3d_2 = Pattern.compile(".*\\Qjavax/vecmath\\E.*",Pattern.DOTALL);
    private static final Pattern regex_java3d_3 = Pattern.compile(".*\\Qjavax/media/j3d\\E.*",Pattern.DOTALL);
    
    private static final Pattern regex_unkwnown_class = Pattern.compile(".*\\QUnknown class:\\E\\s*([\\p{L}\\d]+)\\s*:.*",Pattern.DOTALL);
    
    
    private static final Pattern regex_unkwnown_method = Pattern.compile(".*\\QMethod\\E\\s*([\\p{L}\\d]+)\\(.*\\Qnot found in class\\E\\s*\\'([\\p{L}\\d\\.]+)\\'.*",Pattern.DOTALL);
    private static final Pattern regex_unkwnown_object = Pattern.compile(".*\\Qundefined variable or class name:\\E\\s*([\\p{L}\\d\\(\\)]+)\\s*:.*",Pattern.DOTALL);
    
    private static final Pattern regex_error_line_1 = Pattern.compile(".*\\Qat Line:\\E\\s*([\\d]+)\\s*:.*",Pattern.DOTALL);
    private static final Pattern regex_error_line_2 = Pattern.compile(".*\\Qat line:\\E\\s*([\\d]+)\\s*,.*",Pattern.DOTALL);
    
    /** Class logger */
    protected static Logger LOG = Logger.getLogger(ErrorHelper.class);

    
	public static ErrorResult process(Throwable toProcess) {
		ErrorResult result = new ErrorResult();
		String message = toProcess.getMessage();
		if (toProcess instanceof NoClassDefFoundError) {
			if (regex_java3d_1.matcher(message).matches()||regex_java3d_2.matcher(message).matches() || regex_java3d_3.matcher(message).matches()) {
				result.setText(Messages.getString("error.java3d_missing"));
				result.setLink(Messages.getString("java3d.download_url"), Messages.getString("java3d.download"));
				return result;
			} 
		} else if (toProcess instanceof BSFException) {
			result.setLine(getErrorLine(toProcess));
			Matcher m1 =regex_unkwnown_class.matcher(message); 
			if (m1.matches()) {
				result.setText(MessageFormat.format(Messages.getString("error.class_unkwnown"),m1.group(1)));
				return result;
			}
			Matcher m2 =regex_unkwnown_method.matcher(message); 
			if (m2.matches()) {
				String className = m2.group(2);
				String simpleClassName = null;
				if (className != null) {
					simpleClassName = className.substring(className.lastIndexOf('.')+1);
				}
				if (simpleClassName != null)
					result.setText(MessageFormat.format(Messages.getString("error.method_unkwnown"),m2.group(1), simpleClassName));
				else
					result.setText(MessageFormat.format(Messages.getString("error.method_unkwnown2"),m2.group(1)));
				return result;
			}
			Matcher m3 =regex_unkwnown_object.matcher(message); 
			if (m3.matches()) {
				result.setText(MessageFormat.format(Messages.getString("error.object_unkwnown"),m3.group(1)));
				return result;
			}
		}
        LOG.warn("Unrecognized execution error: "+message);
		result.setText(Messages.getString("error.incorrect_command"));
		return result;
	}
	
	private static int getErrorLine(Throwable toProcess) {
		int line = -1;
		Matcher m1 =regex_error_line_1.matcher(toProcess.getMessage());
		if (m1.matches()) {
			line = Integer.parseInt(m1.group(1));
			return line;
		} 
		Matcher m2 =regex_error_line_2.matcher(toProcess.getMessage());
		if (m2.matches()) {
			line = Integer.parseInt(m2.group(1));
			return line;
		}
		return line;
	}
}
