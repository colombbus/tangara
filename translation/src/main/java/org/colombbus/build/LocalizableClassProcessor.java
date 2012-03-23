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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;

/**
 * Processor of a localizable class.
 *
 * <pre>
 * A localizable class is a class using {@link Localize} annotation.
 * The usage is:
 * 		LocalizableClassProcessor processor = new {@link #LocalizableClassProcessor(ProcessingEnvironment)};
 * 		// Register all the listeners
 * 		processor.{@link #addClassProcessingListener(ClassProcessingListener)};
 * 	// Process the localized class
 * 	processor.{@link #process(TypeElement)}.
 * </pre>
 */
public class LocalizableClassProcessor {

	private List<ClassProcessingListener> listeners = new ArrayList<ClassProcessingListener>();

	private ProcessingEnvironment env;
	private LocalizeAnnotationParser localizeMetadata;

	private final List<Map.Entry<ExecutableElement, TypeElement>> scannedMethods = new ArrayList<Map.Entry<ExecutableElement, TypeElement>>();
	private final List<Map.Entry<ExecutableElement, TypeElement>> scannedConstructors = new ArrayList<Map.Entry<ExecutableElement, TypeElement>>();

	public LocalizableClassProcessor(ProcessingEnvironment env) {
		this.env = env;
	}

	public void setLocalizeMetadata(LocalizeAnnotationParser localizeMetadata) {
		this.localizeMetadata = localizeMetadata;
	}

	public void addClassProcessingListener(ClassProcessingListener listener) {
		if (listener == null || listeners.contains(listener))
			return;
		listeners.add(listener);
	}

	public void removeClassProcessingListener(ClassProcessingListener listener) {
		listeners.remove(listener);
	}

	public void process(TypeElement classType) throws Exception {
		resetPreviousProcessing();

		fireStartParsing(classType);

		List<? extends TypeParameterElement> classParameters = classType.getTypeParameters();
		fireParseTypeParameters(classParameters);

		for (ExecutableElement constructor : ElementFilter.constructorsIn(classType.getEnclosedElements())) {
			fireParseConstructor(constructor);
			registerScannedConstructor(classType, constructor);
		}

		// Scan methods
		for (ExecutableElement method : ElementFilter.methodsIn(classType.getEnclosedElements())) {
			fireParseMethod(method);
			registerScannedMethod(classType, method);
		}

		if (localizeMetadata.isLocalizeParentEnabled(classType)) {
			TypeElement parentClass = findAncestor(classType);
			scanAncestorClass(parentClass);
		}

		fireEndParse();
	}

	private void resetPreviousProcessing() {
		scannedMethods.clear();
		scannedConstructors.clear();
	}

	private TypeElement findAncestor(TypeElement typeClass) {
		TypeMirror superclass = typeClass.getSuperclass();
		return (TypeElement) env.getTypeUtils().asElement(superclass);
	}

	private void scanAncestorClass(TypeElement ancestorClass) throws Exception {
		if (ancestorClass == null)
			return;

		// Scan methods
		for (ExecutableElement method : ElementFilter.methodsIn(ancestorClass.getEnclosedElements())) {
			if (notParseable(method))
				continue;

			fireParseMethod(method);
			registerScannedMethod(ancestorClass, method);
		}

		if (localizeMetadata.isLocalizeParentEnabled(ancestorClass)) {
			TypeElement ancestorOfTheAncestor = findAncestor(ancestorClass);
			scanAncestorClass(ancestorOfTheAncestor);
		}
	}

	private boolean notParseable(ExecutableElement method) {
		return isPrivate(method) || overrideScannedMethod(method);
	}

	private static boolean isPrivate(ExecutableElement method) {
		return method.getModifiers().contains(Modifier.PRIVATE);
	}

	private boolean overrideScannedMethod(ExecutableElement method) {
		Elements elemUtils = env.getElementUtils();
		for (Entry<ExecutableElement, TypeElement> scannedMethod : scannedMethods) {
			if (elemUtils.overrides(scannedMethod.getKey(), method, scannedMethod.getValue()))
				return true;
		}
		return false;
	}

	private void registerScannedMethod(TypeElement aClass, ExecutableElement method) {
		scannedMethods.add(new SimpleEntry<ExecutableElement, TypeElement>(method, aClass));
	}

	private void registerScannedConstructor(TypeElement aClass, ExecutableElement constructor) {
		scannedConstructors.add(new SimpleEntry<ExecutableElement, TypeElement>(constructor, aClass));
	}

	private void fireStartParsing(TypeElement parsedClass) throws Exception {
		for (ClassProcessingListener listener : listeners) {
			listener.startParsing(parsedClass);
		}
	}

	private void fireParseTypeParameters(List<? extends TypeParameterElement> classParameters) throws Exception {
		for (ClassProcessingListener listener : listeners) {
			listener.parseTypeParameters(classParameters);
		}
	}

	private void fireParseConstructor(ExecutableElement constructor) throws Exception {
		for (ClassProcessingListener listener : listeners) {
			listener.parseConstructor(constructor);
		}
	}

	private void fireParseMethod(ExecutableElement method) throws Exception {
		for (ClassProcessingListener listener : listeners) {
			listener.parseMethod(method);
		}
	}

	private void fireEndParse() throws Exception {
		for (ClassProcessingListener listener : listeners) {
			listener.endParsing();
		}
	}

}
