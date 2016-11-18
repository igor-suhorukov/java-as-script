package org.github.suhorukov.java.as.script;

import org.codehaus.commons.compiler.jdk.SimpleClassPathCompiler;

import java.util.Optional;

public class CompilationResult {

    private SimpleClassPathCompiler compiler;
    private Optional<String> publicClassName;

    public CompilationResult(SimpleClassPathCompiler compiler, Optional<String> publicClassName) {
        this.compiler = compiler;
        this.publicClassName = publicClassName;
    }

    public SimpleClassPathCompiler getCompiler() {
        return compiler;
    }

    public Optional<String> getPublicClassName() {
        return publicClassName;
    }
}
