package org.github.suhorukov.java.as.script.helper;

import com.github.igorsuhorukov.eclipse.aether.artifact.Artifact;
import com.github.igorsuhorukov.eclipse.aether.artifact.DefaultArtifact;
import com.github.igorsuhorukov.maven.DependenciesResolver;
import com.github.igorsuhorukov.maven.MavenDependenciesResolver;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 */
public class MavenGeneratorResolver implements DependenciesResolver {

    private List<Artifact> artifacts = Collections.emptyList();

    @Override
    public List<URL> resolve(Set<String> dependenciesUrl) {
        String prefix = "mvn:/";
        artifacts = dependenciesUrl.stream().filter(Objects::nonNull).filter(url -> {
            return url.startsWith(prefix);
        }).map(dep -> new DefaultArtifact(dep.replace(prefix, ""))).collect(Collectors.toList());
        return new MavenDependenciesResolver().resolve(dependenciesUrl);
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }
}
