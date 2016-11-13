package org.github.suhorukov;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScriptRunnerUtils {

    public static void runScript(String scriptText, String[] scriptArgs)
            throws Exception {

        runScript(new JavaCompiler().compileScript(scriptText), scriptArgs);
    }

    public static void runScript(CompilationResult compilationResult, String[] scriptArgs) {
        try {
            runScriptInternal(compilationResult, scriptArgs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void runScriptInternal(CompilationResult compilationResult, String[] scriptArgs) throws Exception {

        ClassLoader compilerClassLoader = compilationResult.getCompiler().getClassLoader();
        Set<String> compiledClasses = compilationResult.getCompiler().listCompiledClasses();
        if(compilationResult.getPublicClassName().isPresent()) {
            String fullClassName = compiledClasses.stream().filter(className ->
                    className.endsWith(compilationResult.getPublicClassName().get())).findFirst().get();
            try {
                invokeMainMethod(compilerClassLoader.loadClass(fullClassName), scriptArgs);
            } catch (NoSuchMethodException e){
                throw new IllegalArgumentException(
                        "Script entry point is not found. Please specify psvm method for class "+fullClassName);
            }
        } else {
            List<Class<?>> mainClasses = compiledClasses.stream().map(className -> {
                try {
                    return compilerClassLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).filter(clazz -> {
                try {
                    clazz.getMethod("main", String[].class);
                    return true;
                } catch (NoSuchMethodException e) {
                    return false;
                }
            }).collect(Collectors.toList());

            if(mainClasses.size() > 1){
                throw new IllegalArgumentException("Script entry point is ambiguous. Classes with psvm methods: " +
                        mainClasses);
            } else if( mainClasses.isEmpty()){
                throw new IllegalArgumentException(
                        "Script entry point is not found. Please specify class with java psvm method");
            }
            Class<?> clazz = mainClasses.iterator().next();
            invokeMainMethod(clazz, scriptArgs);
        }
    }

    private static void invokeMainMethod(Class<?> clazz, String[] args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method main = clazz.getMethod("main", String[].class);
        main.setAccessible(true);
        main.invoke(null, (Object) args);
    }
}
