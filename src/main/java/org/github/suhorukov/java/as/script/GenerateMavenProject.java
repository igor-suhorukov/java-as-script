package org.github.suhorukov.java.as.script;

import com.github.igorsuhorukov.codehaus.plexus.util.IOUtil;
import org.github.suhorukov.java.as.script.helper.Utils;
import org.github.suhorukov.java.as.script.helper.MavenGeneratorResolver;

import java.io.*;
import java.util.*;

/**
 */
public class GenerateMavenProject {

    public static void main(String[] args) throws Exception{

        ScriptRunner scriptRunner = new ScriptRunner() {
            @Override
            public void runScript(String scriptText, String[] scriptArgs) throws Exception {

                MavenGeneratorResolver dependenciesResolver = new MavenGeneratorResolver();
                CompilationResult compilationResult = new JavaCompiler().compileScript(scriptText, dependenciesResolver);
                String scriptClassName = getScriptClassName(compilationResult);
                File projectDir = createProjectDirectory();
                File javaDir = createSourceDirectory(projectDir);
                File scriptDirectory = getScriptDirectory(compilationResult, scriptClassName, javaDir);

                writePomFile(Utils.generatePomContent(dependenciesResolver, scriptClassName), projectDir);

                writeScriptFile(scriptText, compilationResult, scriptClassName, scriptDirectory);
            }
        };
        scriptRunner.run(args);
    }

    private static void writePomFile(String pomXml, File projectDir) throws IOException {
        try (OutputStream pomStream= new FileOutputStream(new File(projectDir, "pom.xml"))){
            IOUtil.copy(new StringReader(pomXml), pomStream);
        }
    }

    private static void writeScriptFile(String scriptText, CompilationResult compilationResult, String scriptClassName,
                                        File scriptDirectory) throws IOException {
        File scriptFile = new File(scriptDirectory, getScriptName(compilationResult, scriptClassName) + ".java");
        try (OutputStream javaStream = new FileOutputStream(scriptFile)){
            IOUtil.copy(new StringReader(scriptText), javaStream);
        }
    }

    private static File createSourceDirectory(File projectDir) {
        File javaDir = new File(projectDir, "src/main/java");
        javaDir.mkdirs();
        return javaDir;
    }

    private static File createProjectDirectory() {
        File projectDir = new File("script-" + UUID.randomUUID().toString());
        projectDir.mkdir();
        return projectDir;
    }

    private static String getScriptClassName(CompilationResult compilationResult) throws IOException {
        String scriptName = "Script";
        if(compilationResult.getPublicClassName().isPresent()){
            scriptName = Utils.getFullClassName(compilationResult);
        }
        return scriptName;
    }

    private static String getScriptName(CompilationResult compilationResult, String scriptName) {
        String scriptFile;
        if(compilationResult.getPublicClassName().isPresent()) {
            scriptFile = compilationResult.getPublicClassName().get();
        } else {
            scriptFile = scriptName;
        }
        return scriptFile;
    }

    private static File getScriptDirectory(CompilationResult compilationResult, String scriptName, File javaDir) throws IOException {
        File scriptDirectoryPath;
        if(compilationResult.getPublicClassName().isPresent()){
            int classNameIndex = scriptName.lastIndexOf(compilationResult.getPublicClassName().get()) - 1;
            scriptDirectoryPath = getPackageDir(scriptName, javaDir, classNameIndex);
        } else {
            String firstClass = compilationResult.getCompiler().listCompiledClasses().iterator().next();
            int classNameIndex = firstClass.lastIndexOf('.');
            scriptDirectoryPath = getPackageDir(firstClass, javaDir, classNameIndex);

        }
        if(!scriptDirectoryPath.exists()){
            scriptDirectoryPath.mkdirs();
        }
        return scriptDirectoryPath;
    }

    private static File getPackageDir(String scriptName, File javaDir, int classNameIndex) {
        File scriptPath;
        if(classNameIndex >1){
            String path = scriptName.substring(0, classNameIndex).replace('.','/');
            scriptPath = new File(javaDir, path);
        } else {
            scriptPath = javaDir;
        }
        return scriptPath;
    }

}
