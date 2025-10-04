package de.theholyexception.holyapi.util;

import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

    /**
     * Finds all classes in a package and its sub-packages.
     *
     * @param packageName The name of the package (e.g. "com.example.project")
     * @return A list of all found classes
     * @throws ClassNotFoundException If the package cannot be found
     * @throws IOException If an error occurs while reading files
     */
    public static List<Class<?>> findAllClasses(String packageName) throws IOException {
        List<Class<?>> classes = new ArrayList<>();

        // Convert the package name to a path
        String path = packageName.replace('.', '/');

        // Get all resources for this path
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        // Go through all found resources
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();

            if ("file".equals(protocol)) {
                // Load classes from a directory
                findClassesInDirectory(new File(resource.getFile()), packageName, classes);
            } else if ("jar".equals(protocol)) {
                // Load classes from a JAR file
                findClassesInJar(resource, path, packageName, classes);
            }
        }

        return classes;
    }

    /**
     * Finds classes in a directory and its subdirectories.
     *
     * @param directory The directory to search
     * @param packageName The current package name
     * @param classes The list to which found classes are added
     * @throws ClassNotFoundException If a class cannot be loaded
     */
    private static void findClassesInDirectory(File directory, String packageName, List<Class<?>> classes) {
        if (!directory.exists()) {
            return;
        }

        // Go through all files in the directory
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();

                if (file.isDirectory()) {
                    // Recursive call for subdirectories
                    findClassesInDirectory(file, packageName + "." + fileName, classes);
                } else if (fileName.endsWith(".class")) {
                    // Add class to the list

                    try {
                        String className = packageName + "." + fileName.substring(0, fileName.length() - 6);
                        classes.add(Class.forName(className));
                    } catch (NoClassDefFoundError ex) {
                        LoggerProxy.log(LogLevel.ERROR, "Failed to load class " + packageName + "." +
                            fileName + " - NoClassDefFoundError:" + ex.getMessage());
                    } catch (ClassNotFoundException ex) {
                        LoggerProxy.log(LogLevel.ERROR, "Failed to load class " + packageName + "." +
                            fileName + " - ClassNotFoundException:" + ex.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Finds classes in a JAR file.
     *
     * @param resource The URL to the JAR file
     * @param path The path within the JAR file
     * @param packageName The current package name
     * @param classes The list to which found classes are added
     * @throws IOException If the JAR file cannot be read
     */
    private static void findClassesInJar(URL resource, String path, String packageName,
                                         List<Class<?>> classes) throws IOException {
        String jarPath = resource.getPath();

        // Extract the JAR file path from the URL
        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring(5);
        }

        int separatorIndex = jarPath.indexOf("!/");
        if (separatorIndex != -1) {
            jarPath = jarPath.substring(0, separatorIndex);
        }

        // Open the JAR file
        try (JarFile jar = new JarFile(jarPath)) {
            // Go through all entries in the JAR file
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // Check if the entry is in the correct package and is a class
                if (entryName.startsWith(path) && entryName.endsWith(".class")) {
                    // Convert path back to a class name
                    String className = entryName.replace('/', '.')
                        .substring(0, entryName.length() - 6);

                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        // Ignore non-loadable classes
                    }
                }
            }
        }
    }

}
