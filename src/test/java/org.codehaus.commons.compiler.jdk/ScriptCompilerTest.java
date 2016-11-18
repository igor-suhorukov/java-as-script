package org.codehaus.commons.compiler.jdk;

import com.github.igorsuhorukov.codehaus.plexus.util.IOUtil;
import org.github.suhorukov.java.as.script.CompilationResult;
import org.github.suhorukov.java.as.script.JavaCompiler;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class ScriptCompilerTest {

    @Test
    public void testClassloader() throws Exception {
        String scriptText = IOUtil.toString(ScriptRunnerTest.class.getResourceAsStream("/ScriptWithMultipleClasses.java"));
        CompilationResult compilationResult = new JavaCompiler().compileScript(scriptText);
        Assert.assertFalse(compilationResult.getPublicClassName().isPresent());
        SimpleClassPathCompiler compiler = compilationResult.getCompiler();
        Set<String> classes = compiler.listCompiledClasses();
        assertTrue(classes.contains("mypackage.T1"));
        assertTrue(classes.contains("mypackage.T2"));
        assertTrue(classes.contains("mypackage.MyInterface"));
        Class<?> t2Class = compiler.getClassLoader().loadClass("mypackage.T2");
        Constructor<?> constructor= t2Class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Runnable runnable = (Runnable) constructor.newInstance();
        runnable.run();
    }

}
