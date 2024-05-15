package com.switchvov.magicutils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Field Utils
 *
 * @author switch
 * @since 2024/5/8
 */
public class FieldUtils {

    public static List<Field> findFieldByAnnotated(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return findField(clazz, f -> f.isAnnotationPresent(annotationClass));
    }

    public static List<Field> findField(Class<?> clazz, Function<Field, Boolean> f) {
        List<Field> fields = new ArrayList<>();
        while (Objects.nonNull(clazz)) {
            fields.addAll(Arrays.stream(clazz.getDeclaredFields()).filter(f::apply).toList());
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
