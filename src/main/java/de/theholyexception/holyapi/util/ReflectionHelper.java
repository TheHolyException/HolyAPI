package de.theholyexception.holyapi.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ReflectionHelper {
    public static List<Field> findAnnotatedFieldsInPackage(String packageName, Class<? extends Annotation> annotationClass) throws ClassNotFoundException, IOException, URISyntaxException {
        List<Field> annotatedFields = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File packageDir = new File(resource.toURI());
            if (packageDir.isDirectory()) {
                findAndAddAnnotatedFields(packageName, packageDir, annotationClass, annotatedFields);
            }
        }

        return annotatedFields;
    }

    private static void findAndAddAnnotatedFields(String packageName, File directory, Class<? extends Annotation> annotationClass, List<Field> annotatedFields) throws ClassNotFoundException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String subPackageName = packageName + "." + file.getName();
                    findAndAddAnnotatedFields(subPackageName, file, annotationClass, annotatedFields);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(annotationClass)) {
                            annotatedFields.add(field);
                        }
                    }
                }
            }
        }
    }

    public static String getCallerClass() {
        return Thread.currentThread().getStackTrace()[3].getClassName();
    }

    public static String getCallerClass(int depth) {
        return Thread.currentThread().getStackTrace()[depth].getClassName();
    }
}
