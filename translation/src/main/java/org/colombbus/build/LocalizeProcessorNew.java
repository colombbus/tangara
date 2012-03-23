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
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Processor for generated 'localized' version of classes
 */
public class LocalizeProcessorNew implements Processor {

	private ProcessingEnvironment env;
	private LocalizeAnnotationParser localizeMetadata;
	private List<LocalizeDictionary> localizeDictionaries;
	private ProcessingLogger pLogger = new ProcessingLogger();
	private LocalizeIndex indexGenerator;

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(Localize.class.getName());
	}

	@Override
	public Set<String> getSupportedOptions() {
		return OptionsCst.getOptions();
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member,
			String userText) {
		return Collections.emptyList();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_6;
	}

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		this.env = processingEnv;

		initPLogger();
		initLocalizeMetadata();
		initLocalizeDictionaries();
	}

	private void initPLogger() {
		pLogger.setMessager(env.getMessager());
	}

	private void initLocalizeMetadata() {
		localizeMetadata = new LocalizeAnnotationParser(this.env);
	}

	private void initLocalizeDictionaries() {
		try {

			LocalizeDictionaryLoader loader = new LocalizeDictionaryLoader();
			loader.setLogger(pLogger);
			File confDir = localizeConfigurationDirectory();
			loader.setConfigurationDirectory(confDir);
			loader.loadDictionaries();
			localizeDictionaries = loader.getDictionaries();

		} catch (IOException ignore) {
			pLogger.error("Initialization failed"); //$NON-NLS-1$
		}
	}

	private File localizeConfigurationDirectory() {
		Map<String, String> options = env.getOptions();
		String pathName = options.get(OptionsCst.DICTIONARIES_PATH_OPT);
		if (pathName == null)
			return new File("."); //$NON-NLS-1$
		else
			return new File(pathName);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		TypeElement annotationDeclaration = localizeMetadata.getLocalizeAnnotationType();
		if (annotations.contains(annotationDeclaration))
			process(roundEnv);
		return true;
	}

	private void process(RoundEnvironment roundEnv) {
		TypeElement annotationDeclaration = localizeMetadata.getLocalizeAnnotationType();
		for (Element element : roundEnv.getElementsAnnotatedWith(annotationDeclaration)) {
			processClassIfNecessary(element);
		}
	}

	private void processClassIfNecessary(Element element) {
		if (element.getKind() != ElementKind.CLASS)
			return;

		TypeElement classType = (TypeElement) element;
		if (isLocalizable(classType))
			localize(classType);
	}

	private boolean isLocalizable(TypeElement type) {
		return localizeMetadata.findAnnotationValueInClass(type) != null;
	}

	private void localize(TypeElement localizableClass) {
		pLogger.info("Localizing class %s", localizableClass.getQualifiedName()); //$NON-NLS-1$

		try {
			LocalizableClassProcessor classProcessor = new LocalizableClassProcessor(env);
			classProcessor.setLocalizeMetadata(localizeMetadata);

			indexGenerator = new LocalizeIndex(env, localizableClass);

			for (LocalizeDictionary localizeDictionary : localizeDictionaries) {
				LocalizedClassGenerator classGenerator = createClassGenerator(localizeDictionary);
				classProcessor.addClassProcessingListener(classGenerator);
			}

			classProcessor.process(localizableClass);

		} catch (Exception e) {
			pLogger.error("Error while trying to localize class %s : %s", localizableClass.getQualifiedName(), e.getMessage()); //$NON-NLS-1$
		} finally {
			IOUtils.closeIfNecessary(indexGenerator);
			indexGenerator = null;
		}
	}

	private LocalizedClassGenerator createClassGenerator(LocalizeDictionary localizeDictionary) {
		LocalizedClassGenerator classGenerator = new LocalizedClassGenerator();
		classGenerator.setProcessingLogger(pLogger);
		classGenerator.setLocalizeDictionary(localizeDictionary);
		classGenerator.setLocalizeMetadata(localizeMetadata);
		classGenerator.setProcessingEnvironment(env);
		classGenerator.setIndexGenerator(indexGenerator);
		return classGenerator;
	}

}