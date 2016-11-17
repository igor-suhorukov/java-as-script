package org.github.suhorukov;

import com.github.igorsuhorukov.codehaus.plexus.util.IOUtil;
import com.github.igorsuhorukov.url.handler.UniversalURLStreamHandlerFactory;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.net.URL.setURLStreamHandlerFactory;

public class ScriptRunner {

    public static void main(String[] args) {
        try {
            new ScriptRunner().run(args);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    protected void run(String[] args) throws Exception {
        String[] scriptParams;
        if(args.length>1) {
            scriptParams = Arrays.copyOfRange(args, 1, args.length);
            System.out.println(scriptParams.length);
        } else {
            scriptParams = new String[0];
        }

        String scriptPath = args.length>0 ? args[0]: null;
        if(scriptPath==null || scriptPath.isEmpty()){
            throw new IllegalArgumentException("Non empty script path as first java args are required. Please provide it!");
        }
        setURLStreamHandlerFactory(new UniversalURLStreamHandlerFactory());
        URL scriptURL;
        try {
            scriptURL = new URL(scriptPath);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid format of provided script path = "+scriptPath, e);
        }
        try (InputStream scriptStream = scriptURL.openStream()){
            runScript(IOUtil.toString(scriptStream), scriptParams);
        }


    }

    public void runScript(String scriptText, String[] scriptArgs)
            throws Exception {

        runScript(new JavaCompiler().compileScript(scriptText), scriptArgs);
    }

    public void runScript(CompilationResult compilationResult, String[] scriptArgs) {
        try {
            runScriptInternal(compilationResult, scriptArgs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void runScriptInternal(CompilationResult compilationResult, String[] scriptArgs) throws Exception {

        ClassLoader compilerClassLoader = compilationResult.getCompiler().getClassLoader();
        if(compilationResult.getPublicClassName().isPresent()) {
            String fullClassName = CompilerUtils.getFullClassName(compilationResult);
            try {
                invokeMainMethod(compilerClassLoader.loadClass(fullClassName), scriptArgs);
            } catch (NoSuchMethodException e){
                throw new IllegalArgumentException(
                        "Script entry point is not found. Please specify psvm method for class "+fullClassName);
            }
        } else {
            List<Class<?>> mainClasses = compilationResult.getCompiler().listCompiledClasses().stream().map(className -> {
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
