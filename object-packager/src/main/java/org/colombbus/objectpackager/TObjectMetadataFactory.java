package org.colombbus.objectpackager;

class TObjectMetadataFactory {

	private TObjectMetadataFactory() {
	}

	/**
	 * Create a {@link TObjectMetadata} (Tangara object metadata) from its
	 * textual description
	 *
	 * <pre>
	 * A TObject metadata textual description is a string with this
	 * CSV template:
	 * <english classname>, (<package name>)?, <french classname>, <spanish classname>
	 *
	 * The <package name> is optional but the next comma shall stay.
	 * For example:
	 *  Drawing, ,Dessin,Diseno
	 *  Sprite,sprite,Animation,Animacion
	 * </pre>
	 *
	 * @param description
	 *            textual representation of the TObject metadata
	 * @return the corresponding {@link TObjectMetadata} instance, never
	 *         <code>null</code>
	 */
	public static TObjectMetadata createMetadata(String description) {
		if (description == null)
			throw new IllegalArgumentException("no description"); //$NON-NLS-1$

		String[] fields = description.split(","); //$NON-NLS-1$
		if (fields.length != 4)
			throw new IllegalArgumentException("too many fields"); //$NON-NLS-1$

		TObjectMetadata translation = new TObjectMetadata();

		String englishClassname = toClassname(fields[0]);
		translation.setI18NObjectName(Language.ENGLISH, englishClassname);

		String packagename = toPackagename(fields[1]);
		translation.setPackageName(packagename);

		String frenchClassname = toClassname(fields[2]);
		translation.setI18NObjectName(Language.FRENCH, frenchClassname);

		String spanishClassname = toClassname(fields[3]);
		translation.setI18NObjectName(Language.SPANISH, spanishClassname);

		return translation;
	}

	private static String toClassname(String field) {
		String classname = field.trim();
		if (classname.length() == 0)
			throw new IllegalArgumentException("empty classname"); //$NON-NLS-1$
		char firstChar = classname.charAt(0);
		if (Character.isUpperCase(firstChar) == false) {
			String msg = String.format("Invalid class name '%s', not starting with uppercase", classname); //$NON-NLS-1$
			throw new IllegalArgumentException(msg);
		}
		return classname;
	}

	private static String toPackagename(String field) {
		String packagename = field.trim();
		if (packagename.length() == 0)
			return null;

		boolean inLowerCase = packagename.toLowerCase().equals(packagename);
		if (inLowerCase == false) {
			String msg = String.format("Invalid package name '%s', not in lower case", packagename); //$NON-NLS-1$
			throw new IllegalArgumentException(msg);
		}

		return packagename;
	}

}
