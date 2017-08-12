package org.github.suhorukov.java.as.script;

import com.github.igorsuhorukov.maven.DependenciesResolver;
import com.github.igorsuhorukov.maven.MavenDependenciesResolver;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.jdk.SimpleClassPathCompiler;
import org.eclipse.jdt.core.compiler.IProblem;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JavaCompiler {

    public CompilationResult compileScript(String scriptText) throws CompileException {
        return compileScript(scriptText, new MavenDependenciesResolver());
    }

    public CompilationResult compileScript(String scriptText, DependenciesResolver dependenciesResolver)
            throws CompileException {

        Set<String> dependencies = parseDependencies(scriptText);
        SimpleClassPathCompiler simpleCompiler = new SimpleClassPathCompiler(dependenciesResolver.resolve(dependencies));
        setCompilerOptions(simpleCompiler);
        Optional<String> publicClassName = compileScript(scriptText, simpleCompiler);
        return new CompilationResult(simpleCompiler, publicClassName);
    }

    protected void setCompilerOptions(SimpleClassPathCompiler simpleCompiler) {
        simpleCompiler.setCompilerOptions("-8");
        simpleCompiler.setDebuggingInformation(true, true, true);
    }

    private Set<String> parseDependencies(String src) {
        Pattern dependencyPattern = Pattern.compile("^//dependency\\:(\\S+)\\s*$", Pattern.DOTALL);
        return Arrays.stream(src.split("\n"))
                .map(line -> {
                    Matcher match = dependencyPattern.matcher(line);
                    return match.matches() ? match.group(1) : null;
                }).filter(line -> line!=null).collect(Collectors.toSet());
    }

    protected Optional<String> compileScript(String scriptText, SimpleClassPathCompiler simpleCompiler)
            throws CompileException {

        String publicClassName = null;
        try {
            simpleCompiler.cook(UUID.randomUUID().toString()+".java", scriptText);
        } catch (CompileException e){
            String message = e.getMessage();
            if(message.contains(Integer.toString(IProblem.PublicClassMustMatchFileName))){
                publicClassName = parsePublicClassMustMatchFileNameException(e, message);
                simpleCompiler.cook(publicClassName+".java", scriptText);
            } else {
                throw e;
            }
        }
        return Optional.ofNullable(publicClassName);
    }

    protected String parsePublicClassMustMatchFileNameException(CompileException e, String message) {
        String publicClassName;Pattern pattern = Pattern.compile(".*The public type (\\S+) must be defined in its own file.*");
        Matcher matcher = pattern.matcher(message);
        if(matcher.matches()){
            publicClassName = matcher.group(1);
        } else {
            throw new IllegalArgumentException("Invalid compiler error format",e);
        }
        return publicClassName;
    }
}
