/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2008-2012 Colombbus (http://www.colombbus.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.colombbus.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

/**
 * Generate localization
 *
 */
public class LocalizeProcessorOld implements Processor {
	private static final String TRANSLATION_PATH_OPT = "translationPath";

	private static final String USAGE_ANNOTATION_CLASS = "org.colombbus.tangara.Usage";

	private static final String VALUE_PARAM = "value";
	private static final String EXPLORE_PARENT_PARAM = "localizeParent";
	private static final String LOCALIZE_THIS_PARAM = "localizeThis";

	private static final String PROPERTIES_FILENAME = "translation.properties";

	private ProcessingEnvironment env;
	private TypeElement annotationDeclaration;
	private final Map<String, Properties> translations = new Hashtable<String, Properties>();

	private final List<Map.Entry<ExecutableElement, TypeElement>> scannedMethods = new ArrayList<Map.Entry<ExecutableElement, TypeElement>>();
	private final List<Map.Entry<ExecutableElement, TypeElement>> scannedConstructors = new ArrayList<Map.Entry<ExecutableElement, TypeElement>>();

	private void printError(String message) {
		if (env != null) {
			env.getMessager().printMessage(Kind.ERROR, message);
		}
	}

	private void printNote(String message) {
		if (env != null) {
			env.getMessager().printMessage(Kind.NOTE, message);
		}
	}

	public LocalizeProcessorOld() {
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(Localize.class.getName());
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.singleton(TRANSLATION_PATH_OPT);
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member,
			String userText) {
		return Collections.emptyList();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_5;
	}

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		env = processingEnv;
		annotationDeclaration = env.getElementUtils().getTypeElement(Localize.class.getCanonicalName());
		try {
			loadTranslations();
		} catch (IOException ignore) {
			printError("Initialization failed");
		}
	}

	private void loadTranslations() throws IOException {
		File cfgFile = buildConfFile();
		Properties translationCfg = loadConfiguration(cfgFile);

		String separator = translationCfg.getProperty("list.separator");

		String filesListProp = translationCfg.getProperty("translationFiles.list");
		List<String> filenameList = splitToStringList(filesListProp, separator);

		String packagesListProp = translationCfg.getProperty("packages.list");
		List<String> packageList = splitToStringList(packagesListProp, separator);

		Iterator<String> packageIt = packageList.iterator();
		for (String filename : filenameList) {
			File translationFile = new File(cfgFile.getParentFile(), filename);
			Properties translation = loadTranslationFile(translationFile);
			String packageName = packageIt.next();
			translations.put(packageName, translation);
		}
	}

	private List<String> splitToStringList(String string, String delim) {
		String[] strArray = string.split(delim);
		List<String> stringList = Arrays.asList(strArray);
		return stringList;
	}

	private Properties loadTranslationFile(File translationFile) throws IOException {
		try {
			Properties translation = loadPropertyFile(translationFile);
			return translation;
		} catch (FileNotFoundException ex) {
			printError("Could not find translation file : " + translationFile.getAbsolutePath());
			throw ex;
		} catch (IOException ioEx) {
			printError("Error while reading translation file : " + translationFile.getAbsolutePath());
			throw ioEx;
		}
	}

	private Properties loadConfiguration(File cfgFile) throws IOException {
		try {
			Properties translationCfg = loadPropertyFile(cfgFile);
			return translationCfg;
		} catch (FileNotFoundException notFoundEx) {
			printError("Could not find translation config file : " + cfgFile.getAbsolutePath());
			throw notFoundEx;
		} catch (IOException ioEx) {
			printError("Error while reading translation config file : " + cfgFile.getAbsolutePath());
			throw ioEx;
		}
	}

	private Properties loadPropertyFile(File propFile) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(propFile);
			Properties props = new Properties();
			props.load(in);
			return props;
		} finally {
			IOUtils.closeIfNecessary(in);
		}
	}

	private File buildConfFile() {
		File cfgFile;
		if (env.getOptions().containsKey(TRANSLATION_PATH_OPT)) {
			String path = env.getOptions().get(TRANSLATION_PATH_OPT);
			cfgFile = new File(path, PROPERTIES_FILENAME);
		} else {
			cfgFile = new File(PROPERTIES_FILENAME);
		}
		return cfgFile;
	}

	private String findAnnotationValueParam(Element elem) {
		for (AnnotationMirror annotation : elem.getAnnotationMirrors()) {
			if (annotation.getAnnotationType().asElement() == annotationDeclaration) {
				Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> valueSet = extractAnnotValues(annotation);
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> annoKeyValue : valueSet) {
					AnnotationValue annoValue = annoKeyValue.getValue(); // get
																			// the
																			// name
					if (annoKeyValue.getKey().getSimpleName().contentEquals(VALUE_PARAM)) {
						return (String) annoValue.getValue(); // get the value
					}
				}
			}
		}
		return null;
	}

	private Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> extractAnnotValues(AnnotationMirror annotation) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> valueMap = annotation.getElementValues();
		Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> valueSet = valueMap.entrySet();
		return valueSet;
	}

	private String findAnnotationValueInClass(Element e) {
		boolean localizeThis = true;
		String name = null;
		for (AnnotationMirror annotation : e.getAnnotationMirrors()) {
			if (annotation.getAnnotationType().asElement() == annotationDeclaration) {
				Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> valueSet = extractAnnotValues(annotation);
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> annoKeyValue : valueSet) {
					// get the name
					AnnotationValue annoValue = annoKeyValue.getValue();
					if (annoKeyValue.getKey().getSimpleName().contentEquals(VALUE_PARAM)) {
						name = (String) annoValue.getValue(); // get the value
					} else if (annoKeyValue.getKey().getSimpleName().contentEquals(LOCALIZE_THIS_PARAM)) {
						// get the value
						localizeThis = (Boolean) annoValue.getValue();
					}
				}
			}
		}
		if (localizeThis)
			return name;
		else
			return null;
	}

	private boolean scanParent(Element e) {
		for (AnnotationMirror annotation : e.getAnnotationMirrors()) {
			if (annotation.getAnnotationType().asElement() == annotationDeclaration) {
				Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> valueSet = extractAnnotValues(annotation);
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> annoKeyValue : valueSet) {
					//get the name
					AnnotationValue annoValue = annoKeyValue.getValue();
					if (annoKeyValue.getKey().getSimpleName().contentEquals(EXPLORE_PARENT_PARAM)) {
						// get the value
						return (Boolean) annoValue.getValue();
					}
				}
			}
		}
		return false;
	}

	private static String localizeConstructor(ExecutableElement aConstructor, Properties translation, String constructorProperty) {
		if (!translation.containsKey(constructorProperty))
			return "";
		String constructorCode = "\t";
		for (Modifier modifier : aConstructor.getModifiers()) {
			constructorCode += modifier.toString() + " ";
		}
		constructorCode += translation.getProperty(constructorProperty) + "(";
		int paramIdx = 0;
		String parameters = "";
		for (VariableElement parameter : aConstructor.getParameters()) {
			if (paramIdx > 0) {
				constructorCode += ",";
				parameters += ",";
			}
			constructorCode += parameter.asType().toString() + " p" + paramIdx;
			parameters += "p" + paramIdx;
			paramIdx++;
		}
		constructorCode += ")";
		if (aConstructor.getThrownTypes().size() > 0) {
			constructorCode += " throws ";
			paramIdx = 0;
			for (TypeMirror reference : aConstructor.getThrownTypes()) {
				if (paramIdx > 0)
					constructorCode += ",";
				constructorCode += reference.toString();
				paramIdx++;
			}
		}
		constructorCode += "\n\t";
		constructorCode += "{\n\t\t";
		constructorCode += "super(" + parameters + ");\n";
		constructorCode += "\t}\n\n";
		return constructorCode;
	}

	private String localizeMethod(ExecutableElement aMethod, Properties translation, String methodProperty) {
		if (!translation.containsKey(methodProperty))
			return "";
		String methodCode = "";
		if ((translation.containsKey(methodProperty + ".v")) && (translation.containsKey(methodProperty + ".p"))) {
			// Usage annotation must be defined
			if ((translation.containsKey(methodProperty + ".f")))
				methodCode += "\t@Usage(value=\"" + translation.getProperty(methodProperty + ".v") + "\",prototype=\""
						+ translation.getProperty(methodProperty + ".p") + "\",first=" + translation.containsKey(methodProperty + ".f")
						+ ")\n";
			else
				methodCode += "\t@Usage(value=\"" + translation.getProperty(methodProperty + ".v") + "\",prototype=\""
						+ translation.getProperty(methodProperty + ".p") + "\")\n";
		}
		methodCode += "\t";
		for (Modifier modifier : aMethod.getModifiers()) {
			methodCode += modifier.toString() + " ";
		}
		methodCode += aMethod.getReturnType().toString() + " ";
		methodCode += translation.getProperty(methodProperty) + "(";
		int paramIdx = 0;
		String parameters = "";
		for (VariableElement parameter : aMethod.getParameters()) {
			if (paramIdx > 0) {
				methodCode += ",";
				parameters += ",";
			}
			methodCode += parameter.asType().toString() + " p" + paramIdx;
			parameters += "p" + paramIdx;
			paramIdx++;
		}
		methodCode += ")";
		if (aMethod.getThrownTypes().size() > 0) {
			methodCode += " throws ";
			paramIdx = 0;
			for (TypeMirror reference : aMethod.getThrownTypes()) {
				if (paramIdx > 0)
					methodCode += ",";
				methodCode += reference.toString();
				paramIdx++;
			}
		}
		methodCode += "\n";
		methodCode += "\t{\n\t\t";
		if (aMethod.getReturnType().toString().compareTo("void") != 0)
			methodCode += "return ";
		methodCode += "super." + aMethod.getSimpleName() + "(" + parameters + ");\n";
		methodCode += "\t}\n\n";
		return methodCode;
	}

	// checks whether a method is overriden by an already scanned one
	private boolean isMethodOverriden(ExecutableElement aMethod) {
		Elements elemUtils = env.getElementUtils();
		for (Entry<ExecutableElement, TypeElement> method : scannedMethods) {
			if (elemUtils.overrides(method.getKey(), aMethod, method.getValue()))
				return true;
		}
		return false;
	}

	private String recScanClass(TypeElement aClass, Properties translation) {
		// java.lang.Object is reached
		if (aClass == null) {
			return "";
		}

		String inheritedCode = "";
		// DO NOT SCAN CONSTRUCTOR: THIS CAUSES PROBLEMS WHEN TRYING TO SEE IF A
		// CONSTRUCTOR IS OVERRIDEN (NO METHOD PROVIDED FOR THAT?) SO ANY CHILD
		// CLASS SHOULD DEFINE THE CONSTRUCTORS THAT CAN BE USED

		// Scan methods
		for (ExecutableElement method : ElementFilter.methodsIn(aClass.getEnclosedElements())) {
			String annotation = findAnnotationValueParam(method);
			if (annotation != null) {
				// Method has to be translated
				// Scan modifiers, check that the method is not private
				boolean isPrivate = method.getModifiers().contains(Modifier.PRIVATE);
				if ((!isPrivate) && (!isMethodOverriden(method)))
					inheritedCode += localizeMethod(method, translation, annotation);
			}
			registerScannedMethod(aClass, method);
		}
		// Check if the superclass has to be scanned
		if (scanParent(aClass)) {
			inheritedCode += recScanClass((TypeElement) env.getTypeUtils().asElement(aClass.getSuperclass()), translation);
		}
		return inheritedCode;
	}

	private void localizeClass(final TypeElement aClass, final String className) {
		Filer filer = env.getFiler();
		printNote("Localizing class " + aClass.getQualifiedName());
		int point_index = aClass.getQualifiedName().toString().lastIndexOf(".");
		String classname = aClass.getQualifiedName().toString().substring(point_index + 1);
		try {
			String packageName = env.getElementUtils().getPackageOf(aClass).getQualifiedName().toString();
			FileObject logFile = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, classname + "_localization.txt");
			Writer logWriter = logFile.openWriter();
			for (String language : translations.keySet()) {
				Properties translationDic = translations.get(language);
				String transClassname = translationDic.getProperty(className);
				String generics = "";

				List<? extends TypeParameterElement> l = aClass.getTypeParameters();
				if (l.size() > 0) {
					// generic typed
					TypeParameterElement genericType = l.get(0);
					generics = "<" + genericType.getSimpleName() + ">";
				}

				String translatedClassName = packageName + "." + language + "." + transClassname;
				printNote("Writing localized class " + translatedClassName);
				// Initialize methods and constructors lists
				scannedMethods.clear();
				scannedConstructors.clear();

				logWriter.append(language + " " + transClassname + "\n");

				// Create the localized class
				TypeElement translatedClassElem = env.getElementUtils().getTypeElement(aClass.getQualifiedName());
				JavaFileObject jfo = filer.createSourceFile(translatedClassName, translatedClassElem);
				Writer classWriter = jfo.openWriter();
				classWriter.append("package " + packageName + "." + language + ";\n");
				classWriter.append("import " + USAGE_ANNOTATION_CLASS + ";\n");
				classWriter.append("public class " + transClassname + generics + " extends " + aClass.getQualifiedName() + generics + "\n");
				classWriter.append("{\n");
				// Scan constructors
				for (ExecutableElement constructor : ElementFilter.constructorsIn(aClass.getEnclosedElements())) {
					String elementAnnotation = findAnnotationValueParam(constructor);
					if (elementAnnotation != null)
						// Constructor has to be translated
						classWriter.append(localizeConstructor(constructor, translationDic, elementAnnotation));
					registerScannedConstructor(aClass, constructor);
				}
				// Scan methods
				for (ExecutableElement method : ElementFilter.methodsIn(aClass.getEnclosedElements())) {
					String elementAnnotation = findAnnotationValueParam(method);
					if (elementAnnotation != null)
						// Method has to be translated
						classWriter.append(localizeMethod(method, translationDic, elementAnnotation));
					registerScannedMethod(aClass, method);
				}
				// Check if the superclass has to be scanned
				if (scanParent(aClass)) {
					TypeElement parentClass = (TypeElement) env.getTypeUtils().asElement(aClass.getSuperclass());
					String inheritedCode = recScanClass(parentClass, translationDic);
					classWriter.append(inheritedCode);
				}
				classWriter.append("}\n");
				classWriter.close();
			}
			// creating a new
			logWriter.flush();
			logWriter.close();
		} catch (Exception e) {
			printError("Error while trying to localize class " + aClass.getQualifiedName() + "\n" + e.getMessage());
		}
	}

	private void registerScannedMethod(TypeElement aClass, ExecutableElement method) {
		scannedMethods.add(new SimpleEntry<ExecutableElement, TypeElement>(method, aClass));
	}

	private void registerScannedConstructor(TypeElement aClass, ExecutableElement constructor) {
		scannedConstructors.add(new SimpleEntry<ExecutableElement, TypeElement>(constructor, aClass));
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (annotations.contains(annotationDeclaration)) {
			for (Element element : roundEnv.getElementsAnnotatedWith(annotationDeclaration)) {
				if (element.getKind() == ElementKind.CLASS) {
					String value = findAnnotationValueInClass(element);
					if (value != null)
						localizeClass((TypeElement) element, value);
				}
			}
		}
		return true;
	}

}