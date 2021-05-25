package ru.hse.java.implementor;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


public class SimpleImplementor implements Implementor {
    String outputDirectory;
    static final String tab = "    ";

    public SimpleImplementor(String outputDir) {
        outputDirectory = outputDir;
    }

    @Override
    public String implementFromDirectory(String directoryPath, String className) throws ImplementorException {
        try {
            URL jar = new URL("file://" + directoryPath);
            ClassLoader cl = new URLClassLoader(new URL[]{jar});
            Class<?> classToImplement = cl.loadClass(className);

            return implement(classToImplement);
        } catch (MalformedURLException e) {
            throw new ImplementorException("Путь до входного класса не найден", e);
        } catch (ClassNotFoundException e) {
            throw new ImplementorException("Входной класс не найден.", e);
        } catch (IOException e) {
            throw new ImplementorException("Невозможно записать сгенерированный класс.", e);
        }
    }

    private static final Pattern dotPattern = Pattern.compile("\\.");
    private static final Pattern newLinePattern = Pattern.compile(File.separator);

    @NotNull
    private String implement(Class<?> classToImplement) throws IOException, ImplementorException {
        if (!classToImplement.isInterface() && (!Modifier.isAbstract(classToImplement.getModifiers()))) {
            throw new ImplementorException("Невозможно создать наследника класса.");
        }

        String packageName = classToImplement.getPackage().getName();

        if (packageName.startsWith("java.")) {
            packageName = "";
        } else if (!packageName.isEmpty()) packageName += File.separator;


        Path dir = Paths.get(outputDirectory).
                resolve(dotPattern.matcher(packageName).replaceAll(File.separator));

        //noinspection ResultOfMethodCallIgnored
        dir.toFile().mkdirs();

        String filename = dir + File.separator + implName(classToImplement) + ".java";
        try (FileWriter fileWriter = new FileWriter(filename, false)) {
            print(classToImplement, fileWriter);
        }

        return newLinePattern.matcher(packageName).replaceAll(".") + implName(classToImplement);
    }

    @NotNull
    private static String implName(Class<?> classToImplement) {
        return classToImplement.getSimpleName() + "Impl";
    }

    @Override
    public String implementFromStandardLibrary(String className) throws ImplementorException {
        try {
            Class<?> c = Class.forName(className);
            return implement(c);
        } catch (ClassNotFoundException e) {
            throw new ImplementorException("Входной класс не найден.", e);
        } catch (IOException e) {
            throw new ImplementorException("Невозможно записать сгенерированный класс.", e);
        }
    }

    private static void print(Class<?> clazz, FileWriter writer) throws IOException {
        printPackage(clazz, writer);

        writer.append("public class ").
                append(implName(clazz)).
                append(clazz.isInterface() ? " implements " : " extends ").
                append(clazz.getCanonicalName()).append(" {\n");


        printConstructor(clazz, writer);

        printMethods(clazz, writer);

        writer.append("}\n");
    }

    private static void printMethods(Class<?> clazz, FileWriter writer) throws IOException {
        for (Method m : methodsToGen(clazz)) {
            writer.append(tab + "@Override").append("\n");
            writer.append(tab).append(Modifier.toString(m.getModifiers() & ~Modifier.ABSTRACT)).append(" ");
            writer.append(m.getReturnType().getCanonicalName()).append(" ");
            writer.append(m.getName()).append("(");
            Class<?>[] params = m.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                Class<?> param = params[i];
                writer.append(param.getCanonicalName()).append(" ").append("param").append(String.valueOf(i));
                if (i != params.length - 1) {
                    writer.append(", ");
                }
            }
            writer.append(")");
            printThrows(writer, m.getExceptionTypes());

            writer.append(" {" + tab).append("\n").append(tab + tab + "return ").append(getDefaultValue(m.getReturnType())).append(";\n");
            writer.append(tab + "}").append("\n\n");
        }
    }

    private static void printConstructor(Class<?> clazz, FileWriter writer) throws IOException {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (!(Modifier.isPublic(constructor.getModifiers()) ||
                    Modifier.isProtected(constructor.getModifiers())) || constructor.getParameterCount() == 0)
                continue;

            Class<?>[] params = constructor.getParameterTypes();
            writer.append("\n").
                    append(tab + "public ").append(implName(clazz)).append("()");

            printThrows(writer, constructor.getExceptionTypes());
            writer.append(" {").append("\n").
                    append(tab + tab + "super(");

            String comma = "";
            for (Class<?> param : params) {
                writer.append(comma).append("(").
                        append(param.getCanonicalName()).append(") ").
                        append(getDefaultValue(param));
                comma = ", ";
            }
            writer.append(");\n" + tab + "}\n");
            break;
        }
    }

    private static void printPackage(Class<?> clazz, FileWriter writer) throws IOException {
        final String packageName = clazz.getPackage().getName();
        if (!packageName.isEmpty() && !packageName.startsWith("java.")) {
            writer.append("package ").append(packageName).append(";\n");
        }
    }

    private static void printThrows(FileWriter out, Class<?>[] exceptionTypes) throws IOException {
        String before = " throws ";
        for (Class<?> exceptionType : exceptionTypes) {
            out.append(before).
                    append(exceptionType.getCanonicalName());
            before = ", ";
        }
    }

    private static String getDefaultValue(Class<?> type) {
        if (type.isPrimitive()) {
            if (Boolean.TYPE.equals(type)) {
                return "false";
            } else if (Void.TYPE.equals(type)) {
                return "";
            }
            return "0";
        } else {
            return "null";
        }
    }

    private static Set<Method> methodsToGen(Class<?> clazz) {
        return methodsToGen(clazz, new HashSet<>());
    }

    private static Set<Method> methodsToGen(Class<?> clazz, Set<Method> methods) {
        if (clazz == null)
            return new HashSet<>();

        for (Class<?> i : clazz.getInterfaces()) {
            addMethods(i, methodsToGen(i), methods);
        }

        addMethods(clazz, methodsToGen(clazz.getSuperclass(), methods), methods);

        addMethods(clazz, Set.of(clazz.getDeclaredMethods()), methods);

        return new HashSet<>(methods);
    }

    private static void addMethods(Class<?> clazz, Set<Method> toAdd, Set<Method> methods) {

        for (Method m : toAdd) {
            int modifiers = m.getModifiers();
            if (!(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) ||
                    Modifier.isStatic(modifiers) ||
                    m.isBridge()) {
                continue;
            }

            Method toRemove = null;
            for (Method method : methods) {
                if (methodEq(method, m)) {
                    toRemove = method;
                    break;
                }
            }

            if (toRemove != null) {
                methods.remove(toRemove);
            }

            if (Modifier.isAbstract(modifiers) || clazz.isInterface()) {
                methods.add(m);
            }
        }
    }

    private static boolean methodEq(Method m1, Method m2) {
        if (!m1.getName().equals(m2.getName())) {
            return false;
        }

        Class<?>[] param1 = m1.getParameterTypes();
        Class<?>[] param2 = m2.getParameterTypes();
        if (param1.length != param2.length) {
            return false;
        }

        for (int i = 0; i < param1.length; ++i) {
            if (!param1[i].getCanonicalName().equals(param2[i].getCanonicalName())) {
                return false;
            }
        }

        return true;
    }

}


