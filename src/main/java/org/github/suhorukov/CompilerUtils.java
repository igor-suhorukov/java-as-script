package org.github.suhorukov;

import java.io.IOException;
import java.util.Set;

/**
 */
public class CompilerUtils {

    public static String getFullClassName(CompilationResult compilationResult) throws IOException {
        Set<String> strings = compilationResult.getCompiler().listCompiledClasses();
        return strings.stream().filter(
                className ->
                        className.endsWith(compilationResult.getPublicClassName().get())).findFirst().get();
    }
}
