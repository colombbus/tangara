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

import java.util.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;

/**
 * Parser of {@link Localize} annotation
 *
 * <pre>
 * This utility class is used to get information about localize annotation in a class and its ancestors.
 * </pre>
 */
class LocalizeAnnotationParser {

	private static final String VALUE_PARAM = "value"; //$NON-NLS-1$
	private static final String LOCALIZE_PARENT_PARAM = "localizeParent"; //$NON-NLS-1$
	private static final String LOCALIZE_THIS_PARAM = "localizeThis"; //$NON-NLS-1$

	private TypeElement localizeAnnotationType;

	/**
	 * Current {@link Localize} properties where 'key' is the property name and
	 * 'value' is property value
	 */
	private Map<String, Object> curAnnotationProperties;

	public LocalizeAnnotationParser(ProcessingEnvironment processingEnv) {
		Elements elementUtils = processingEnv.getElementUtils();
		localizeAnnotationType = elementUtils.getTypeElement(Localize.class.getCanonicalName());
	}

	public TypeElement getLocalizeAnnotationType() {
		return localizeAnnotationType;
	}

	/**
	 * Get the 'value' property of the {@link Localize} annotation on the
	 * argument
	 *
	 * @param elem
	 *            a java element, may be a class or ar method
	 * @return the 'value' property, or <code>null</code> if the element has no
	 *         argument
	 */
	public String findValueProperty(Element element) {
		return findElementProperty(element, VALUE_PARAM);
	}

	private <T> T findElementProperty(Element element, String propertyName) {
		for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
			if (isLocalizeAnnotation(annotation)) {
				loadAnnotionProperties(annotation);
				if (isAnnotationPropertyRegistered(propertyName))
					return getAnnotationPropertyValue(propertyName);
			}
		}
		return null;
	}

	private boolean isLocalizeAnnotation(AnnotationMirror annotation) {
		return annotation.getAnnotationType().asElement() == localizeAnnotationType;
	}

	private void loadAnnotionProperties(AnnotationMirror annotation) {
		this.curAnnotationProperties = new HashMap<String, Object>();

		Map<? extends ExecutableElement, ? extends AnnotationValue> valueMap = annotation.getElementValues();
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> annotationProperty : valueMap.entrySet()) {
			registerProperty(annotationProperty);
		}
	}

	private void registerProperty(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> annotationProperty) {
		String propertyName = annotationProperty.getKey().getSimpleName().toString();
		Object propertyValue = annotationProperty.getValue().getValue();
		curAnnotationProperties.put(propertyName, propertyValue);
	}

	private boolean isAnnotationPropertyRegistered(String propertyName) {
		return curAnnotationProperties.containsKey(propertyName);
	}

	@SuppressWarnings("unchecked")
	private <T> T getAnnotationPropertyValue(String propertyName) {
		return (T) curAnnotationProperties.get(propertyName);
	}

	public String findAnnotationValueInClass(Element e) {
		Boolean localizeThisProperty = findElementProperty(e, LOCALIZE_THIS_PARAM);
		String valueProperty = findElementProperty(e, VALUE_PARAM);
		if (Boolean.FALSE.equals(localizeThisProperty))
			return null;
		return valueProperty;
	}

	/**
	 * Get the 'localizeParent' property of the {@link Localize} annotation on
	 * the argument
	 *
	 * @param elem
	 *            a java element, may be a class or ar method
	 * @return the 'value' property, or <code>null</code> if the element has no
	 *         argument
	 */
	public boolean isLocalizeParentEnabled(Element classElement) {
		Boolean propertyValue = findElementProperty(classElement, LOCALIZE_PARENT_PARAM);
		return propertyValue == null ? false : propertyValue.booleanValue();
	}

}
