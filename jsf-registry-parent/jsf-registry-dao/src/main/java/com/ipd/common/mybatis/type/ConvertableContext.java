/**
 * Copyright 2004-2048 .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipd.common.mybatis.type;

import java.lang.reflect.Method;

public final class ConvertableContext<T, V> {
    private final Class<T> type;
    private final Class<V> valueType;
    private final Method valueMethod;
    private final Method ofMethod;

    private final Object[] zeroParameter = new Object[0];

    ConvertableContext(final Class<T> type,
            final Class<V> valueType,
            final Method valueMethod,
            final Method ofMethod) {
        this.type = type;
        this.valueType = valueType;
        this.valueMethod = valueMethod;
        this.ofMethod = ofMethod;
    }

    public Class<T> getType() {
        return type;
    }

    public Class<V> getValueType() {
        return valueType;
    }

    public V value(final T source) throws Exception {
        return valueType.cast(valueMethod.invoke(source, zeroParameter));
    }

    public T of(final V value) throws Exception {
        return type.cast(ofMethod.invoke(null, value));
    }

    @SuppressWarnings("unchecked")
    public static <T, V> ConvertableContext<T, V> build(final String className)
            throws RuntimeException {
        Class<T> convertableClazz;
        try {
            convertableClazz = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Class not found", cnfe);
        }
        return build(convertableClazz);
    }

    public static <T, V> ConvertableContext<T, V> build(final Class<T> type)
            throws RuntimeException {

        if (!type.isAnnotationPresent(Convertable.class)) {
            throw new RuntimeException(
                    "Class should be annotated by Convertable.");
        }

        Convertable convertable = type
                .getAnnotation(Convertable.class);
        Method valueMethod;
        try {
            valueMethod = type.getMethod(convertable.valueMethod(),
                    new Class[0]);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to obtain method:"
                    + convertable.valueMethod()
                    + "(which should have non-argument).",
                    exception);
        }

        @SuppressWarnings("unchecked")
        Class<V> valueType = (Class<V>) valueMethod.getReturnType();

        Method ofMethod;
        try {
            ofMethod = type.getMethod(convertable.ofMethod(),
                    new Class[] { valueType });
        } catch (Exception exception) {
            throw new RuntimeException("Failed to obtain method:"
                    + convertable.ofMethod() + "(which should have a(n) "
                    + valueType.getName() + " typed argument)", exception);
        }

        return new ConvertableContext<T, V>(type, valueType, valueMethod,
                ofMethod);
    }
}