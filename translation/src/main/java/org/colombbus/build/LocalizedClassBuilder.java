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

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Class to write the code of the localized class
 */
public class LocalizedClassBuilder {

	private static final String ARGMENT_PREFIX = "p"; //$NON-NLS-1$

	private static final String ARG_SEPARATOR = ", "; //$NON-NLS-1$

	private LocalizeDictionary localizeDictonary;

	private String baseQualifiedClassName;
	private String typeParameter;

	private String basePackage;
	private String baseSimpleClassName;

	private StringBuilder implementationCode = new StringBuilder();
	private StringBuilder classCode;

	public void setLocalizeDictonary(LocalizeDictionary localizeDictionary) {
		this.localizeDictonary = localizeDictionary;
	}

	/**
	 * Set the qualified name of the class to localize
	 *
	 * @param qualifiedClassName
	 *            qualified name of the parent class
	 */
	public void setBaseClass(String baseClass) {
		this.baseQualifiedClassName = baseClass;

		int lastDotIdx = baseQualifiedClassName.lastIndexOf('.');
		basePackage = baseQualifiedClassName.substring(0, lastDotIdx);
		baseSimpleClassName = baseQualifiedClassName.substring(lastDotIdx + 1);
	}

	public void setTypeParameter(String typeParameter) {
		// TODO handle a list of type parameters
		this.typeParameter = typeParameter;
	}

	public void declareConstructor(ExecutableElement parentConstuctor, String localizationKey) {
		if (isLocalizable(localizationKey) == false)
			return;

		String headerCode = methodHeaderCode(parentConstuctor, localizationKey);

		StringBuilder constructorCode = new StringBuilder();
		constructorCode.append(headerCode);
		constructorCode.append("\t{\n\t\t"); //$NON-NLS-1$
		String argumentsCode = argumentsCall(parentConstuctor);
		constructorCode.append("super(").append(argumentsCode).append(");\n"); //$NON-NLS-1$ //$NON-NLS-2$
		constructorCode.append("\t}\n\n"); //$NON-NLS-1$

		implementationCode.append(constructorCode);
		resetClassCode();
	}

	private boolean isLocalizable(String localizationKey) {
		return localizeDictonary.isLocalized(localizationKey);
	}

	private String methodHeaderCode(ExecutableElement parentMethod, String localizationKey) {
		String modifiersCode = modifiersCode(parentMethod);
		String returnTypeCode = returnTypeCode(parentMethod);
		String methodName = localizeDictonary.localize(localizationKey);
		String argDeclCode = argumentsDeclaration(parentMethod);
		String throwsDeclCode = throwsDeclarationCode(parentMethod);
		return String.format("%s %s %s(%s) %s\n", modifiersCode, returnTypeCode, methodName, argDeclCode, throwsDeclCode); //$NON-NLS-1$
	}

	private static String modifiersCode(ExecutableElement parentMethod) {
		StringBuilder code = new StringBuilder("\t"); //$NON-NLS-1$
		for (Modifier modifier : parentMethod.getModifiers()) {
			String modifierCode = modifier.toString();
			code.append(modifierCode).append(" "); //$NON-NLS-1$
		}
		return code.toString();
	}

	private static String returnTypeCode(ExecutableElement parentMethod) {
		if( isConstructor(parentMethod))
			return ""; //$NON-NLS-1$

		TypeMirror returnType = parentMethod.getReturnType();
		return returnType.toString();
	}

	private static boolean isConstructor(ExecutableElement parentMethod) {
		return parentMethod.getKind() == ElementKind.CONSTRUCTOR;
	}

	private static String argumentsDeclaration(ExecutableElement parentMethod) {
		StringBuilder code = new StringBuilder();

		int paramIdx = 0;
		for (VariableElement parameter : parentMethod.getParameters()) {
			if (paramIdx > 0)
				code.append(ARG_SEPARATOR);

			String argumentType = parameter.asType().toString();
			String argumentName = ARGMENT_PREFIX + paramIdx;
			code.append(argumentType).append(" ").append(argumentName); //$NON-NLS-1$
			paramIdx++;
		}

		return code.toString();
	}

	private static String argumentsCall(ExecutableElement parentMethod) {
		StringBuilder code = new StringBuilder();

		int paramIdx = 0;
		for (VariableElement parameter : parentMethod.getParameters()) {
			if (paramIdx > 0)
				code.append(ARG_SEPARATOR);

			String argumentName = ARGMENT_PREFIX + paramIdx;
			code.append(argumentName);
			paramIdx++;
		}

		return code.toString();
	}

	private static String throwsDeclarationCode(ExecutableElement parentMethod) {
		int thrownTypeCount = parentMethod.getThrownTypes().size();
		if (thrownTypeCount == 0)
			return ""; //$NON-NLS-1$

		StringBuilder code = new StringBuilder(" throws "); //$NON-NLS-1$
		boolean firstThrownType = true;
		for (TypeMirror throwType : parentMethod.getThrownTypes()) {
			if (firstThrownType)
				firstThrownType = false;
			else
				code.append(ARG_SEPARATOR);

			String thrownTypeCode = throwType.toString();
			code.append(thrownTypeCode);
		}

		return code.toString();
	}

	private void resetClassCode() {
		this.classCode = null;
	}

	public void declareMethod(ExecutableElement parentMethod, String localizationKey) {
		if (isLocalizable(localizationKey) == false)
			return;

		String annotationCode = usageAnnotationCode(localizationKey);
		String headerCode = methodHeaderCode(parentMethod, localizationKey);
		String parentMethodName = parentMethod.getSimpleName().toString();
		String arguments =  argumentsCall(parentMethod);
		String parentCallCode = String.format("super.%s(%s);\n", parentMethodName, arguments); //$NON-NLS-1$

		StringBuilder methodCode = new StringBuilder();
		methodCode.append(annotationCode);
		methodCode.append(headerCode);
		methodCode.append("\t{\n\t\t"); //$NON-NLS-1$
		if (returningValue(parentMethod))
			methodCode.append("return "); //$NON-NLS-1$
		methodCode.append(parentCallCode);
		methodCode.append("\t}\n\n"); //$NON-NLS-1$

		implementationCode.append(methodCode);
		resetClassCode();
	}

	private String usageAnnotationCode(String localizationKey) {
		if (localizeDictonary.isCallUsageDefined(localizationKey) == false)
			return ""; //$NON-NLS-1$

		String simpleCallCode = localizeDictonary.getSimpleCallCode(localizationKey);
		String prototypeCallCode = localizeDictonary.getPrototypeCallCode(localizationKey);
		boolean defaultCall = localizeDictonary.isDefaultCall(localizationKey);

		StringBuilder code = new StringBuilder("\t@Usage(value=\""); //$NON-NLS-1$
		code.append(simpleCallCode);
		code.append("\",prototype=\""); //$NON-NLS-1$
		code.append(prototypeCallCode);
		code.append("\""); //$NON-NLS-1$
		if (defaultCall) {
			code.append(",first="); //$NON-NLS-1$
			code.append(defaultCall);
		}
		code.append(")\n"); //$NON-NLS-1$
		return code.toString();
	}

	private static boolean returningValue(ExecutableElement parentMethod) {
		boolean isConstructor = parentMethod.getKind() == ElementKind.CONSTRUCTOR;
		if (isConstructor)
			return false;

		TypeMirror returnType = parentMethod.getReturnType();
		String typeName = returnType.toString();
		return typeName.compareTo("void") != 0; //$NON-NLS-1$
	}

	public String getClassCode() {
		if (classCode == null)
			generateClassCode();
		return classCode.toString();
	}

	private void generateClassCode() {
		initClassCode();

		declarePackage();
		declareImports();
		declareHeader();
		declareImplementation();
		declareFooter();
	}

	private void initClassCode() {
		classCode = new StringBuilder();
	}

	private void declarePackage() {
		String language = localizeDictonary.getLanguage();
		String packageDecl = String.format("package %s.%s;\n", basePackage, language); //$NON-NLS-1$
		classCode.append(packageDecl);
	}

	private void declareImports() {
		classCode.append("import org.colombbus.tangara.Usage;\n"); //$NON-NLS-1$
	}

	private void declareHeader() {
		String genericDecl = ""; //$NON-NLS-1$
		if (this.typeParameter != null) {
			genericDecl = String.format("<%s>", typeParameter); //$NON-NLS-1$
		}

		String localizedSimpleClassName = localizeDictonary.localize(baseSimpleClassName);
		String classDecl = String.format("public class %s%s extends %s%s\n", localizedSimpleClassName, genericDecl, baseQualifiedClassName, //$NON-NLS-1$
				genericDecl);
		classCode.append(classDecl);

		classCode.append("{\n"); //$NON-NLS-1$
	}

	private void declareImplementation() {
		classCode.append(implementationCode);
	}

	private void declareFooter() {
		classCode.append("}\n"); //$NON-NLS-1$
	}
}
