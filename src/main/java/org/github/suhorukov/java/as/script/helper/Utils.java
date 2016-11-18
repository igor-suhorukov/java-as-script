package org.github.suhorukov.java.as.script.helper;

import com.github.igorsuhorukov.codehaus.plexus.util.IOUtil;
import com.github.igorsuhorukov.eclipse.aether.artifact.Artifact;
import com.github.igorsuhorukov.springframework.util.StringUtils;
import org.github.suhorukov.java.as.script.CompilationResult;
import org.github.suhorukov.java.as.script.GenerateMavenProject;

import java.io.IOException;
import java.util.Set;

/**
 */
public class Utils {

    public static String getFullClassName(CompilationResult compilationResult) throws IOException {
        Set<String> strings = compilationResult.getCompiler().listCompiledClasses();
        return strings.stream().filter(
                className ->
                        className.endsWith(compilationResult.getPublicClassName().get())).findFirst().get();
    }

    public static String generatePomContent(MavenGeneratorResolver dependenciesResolver, String scriptName) throws IOException {
        StringBuilder dependenciesBuilder = new StringBuilder();
        for(Artifact artifact: dependenciesResolver.getArtifacts()){
            dependenciesBuilder.append("\t\t\t\t<dependency>\n");
            dependenciesBuilder.append("\t\t\t\t\t\t<groupId>").append(artifact.getGroupId()).append("</groupId>\n");
            dependenciesBuilder.append("\t\t\t\t\t\t<artifactId>").append(artifact.getArtifactId())
                    .append("</artifactId>\n");

            if(StringUtils.hasText(artifact.getExtension()) && !"jar".equals(artifact.getExtension())) {
                dependenciesBuilder.append("\t\t\t\t\t\t<type>").append(artifact.getExtension()).append("</type>\n");
            }
            if(StringUtils.hasText(artifact.getClassifier())) {
                dependenciesBuilder.append("\t\t\t\t\t\t<classifier>").append(artifact.getClassifier())
                        .append("</classifier>\n");
            }
            dependenciesBuilder.append("\t\t\t\t\t\t<version>").append(artifact.getVersion()).append("</version>\n");
            dependenciesBuilder.append("\t\t\t\t</dependency>\n");
        }
        String pomXml = IOUtil.toString(GenerateMavenProject.class.getResourceAsStream("/pom_template.xml"));
        pomXml = pomXml.replace("${dependency}",dependenciesBuilder.toString());
        pomXml = pomXml.replace("${groupId}","com.github.java-as-script");
        pomXml = pomXml.replace("${artifactId}",scriptName);
        return pomXml;
    }
}
