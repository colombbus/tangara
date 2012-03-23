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

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.tools.JavaFileObject;

/**
 * Code generator of all the classes from a single localized class
 */
public class LocalizedClassGenerator implements ClassProcessingListener {

	private LocalizeAnnotationParser localizeMetadata;
	private ProcessingEnvironment env;
	private LocalizeDictionary localizeDictionary;
	private ProcessingLogger pLogger;
	private LocalizeIndex indexGenerator;

	private TypeElement localizableClass;
	private LocalizedClassBuilder classBuilder;
	private String localizedQualifiedClassName;
	private String localizedSimpleClassName;

	public void setLocalizeMetadata(LocalizeAnnotationParser localizeMetadata) {
		this.localizeMetadata = localizeMetadata;
	}

	public void setProcessingEnvironment(ProcessingEnvironment env) {
		this.env = env;
	}

	public void setLocalizeDictionary(LocalizeDictionary localizeDictionary) {
		this.localizeDictionary = localizeDictionary;
	}

	public void setProcessingLogger(ProcessingLogger processingLogger) {
		this.pLogger = processingLogger;
	}

	public void setIndexGenerator(LocalizeIndex indexGenerator) {
		this.indexGenerator = indexGenerator;
	}

	@Override
	public void startParsing(TypeElement parsedClass) throws Exception {
		this.localizableClass = parsedClass;

		initLocalizedQualifiedClassName();
		initClassBuilder();
	}

	private void initLocalizedQualifiedClassName() {
		String localizedClassName = localizeMetadata.findAnnotationValueInClass(localizableClass);
		localizedSimpleClassName = localizeDictionary.localize(localizedClassName);

		String language = localizeDictionary.getLanguage();
		localizedQualifiedClassName = String.format("%s.%s.%s", packageName(), language, localizedSimpleClassName); //$NON-NLS-1$
	}

	private void initClassBuilder() {
		classBuilder = new LocalizedClassBuilder();
		classBuilder.setLocalizeDictonary(localizeDictionary);
		String parentQualifiedClassName = localizableClass.getQualifiedName().toString();
		classBuilder.setBaseClass(parentQualifiedClassName);
	}

	private String packageName() {
		return TypeElementHelper.extractPackageName(localizableClass);
	}

	@Override
	public void parseTypeParameters(List<? extends TypeParameterElement> classParameters) {
		if (classParameters.size() > 0) {
			TypeParameterElement typeParameter = classParameters.get(0);
			classBuilder.setTypeParameter(typeParameter.getSimpleName().toString());
		}
	}

	@Override
	public void parseConstructor(ExecutableElement constructor) {
		String localizationKey = localizeMetadata.findValueProperty(constructor);
		if (localizationKey != null)
			classBuilder.declareConstructor(constructor, localizationKey);
	}

	@Override
	public void parseMethod(ExecutableElement method) {
		String localizationKey = localizeMetadata.findValueProperty(method);
		if (localizationKey != null)
			classBuilder.declareMethod(method, localizationKey);
	}

	@Override
	public void endParsing() throws Exception {
		writeLocalizedClassCode();
		registerToIndex();
	}

	private void writeLocalizedClassCode() throws IOException {
		Writer classWriter = null;
		try {

			pLogger.info("Writing localized class %s", localizedQualifiedClassName); //$NON-NLS-1$
			TypeElement localizedClassElem = env.getElementUtils().getTypeElement(localizableClass.getQualifiedName());
			Filer filer = env.getFiler();
			JavaFileObject jfo = filer.createSourceFile(localizedQualifiedClassName, localizedClassElem);
			classWriter = jfo.openWriter();

			String classCode = classBuilder.getClassCode();
			classWriter.append(classCode);

		} finally {
			IOUtils.closeIfNecessary(classWriter);
		}
	}

	private void registerToIndex() throws IOException {
		String language = localizeDictionary.getLanguage();
		indexGenerator.register(language, localizedSimpleClassName);
	}

}
