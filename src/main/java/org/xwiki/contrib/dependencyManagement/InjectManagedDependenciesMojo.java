/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.dependencyManagement;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolver;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolverException;

/**
 * @version $Id: 45afe6a790dc2b72fec66a9f5b0e95e95105bd43 $
 * @since 1.0
 */
@Mojo(name = "inject-managed-dependencies", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true,
    requiresProject = true, requiresDependencyResolution = ResolutionScope.NONE)
public class InjectManagedDependenciesMojo extends AbstractMojo
{
    private static final String PLATFORM_GROUP_ID = "org.xwiki.platform";

    private static final String RENDERING_GROUP_ID = "org.xwiki.rendering";

    private static final String COMMONS_GROUP_ID = "org.xwiki.commons";

    private static final List<String> DEFAULT_GROUP_ID_FILTERS = Arrays.asList(PLATFORM_GROUP_ID, RENDERING_GROUP_ID,
        COMMONS_GROUP_ID);

    private static final String XWIKI_WAR_DEPENDENCIES_ARTIFACT_ID = "xwiki-platform-distribution-war-dependencies";

    private static final String XWIKI_STANDARD_FLAVOR_ARTIFACT_ID = "xwiki-platform-distribution-flavor-common";

    private static final String POM = "pom";

    /**
     * The current Maven session being executed.
     */
    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    protected MavenSession session;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    /**
     * Remote repositories which will be searched for artifacts.
     */
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
    private List<ArtifactRepository> remoteRepositories;

    @Component
    private DependencyResolver dependencyResolver;

    @Parameter(property = "managedDependencySources")
    private List<ManagedDependencySource> managedDependencySources;

    @Parameter(property = "xwikiPlatformVersion", defaultValue = "${platform.version}")
    private String platformVersion;

    @Parameter(property = "useXWikiWARDependencies", defaultValue = "true")
    private boolean useXWikiWARDependencies;

    @Parameter(property = "useXWikiStandardFlavorDependencies", defaultValue = "true")
    private boolean useXWikiStandardFlavorDependencies;

    @Parameter(property = "dependencyGroupIDFilters")
    private List<String> dependencyGroupIDFilters;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        try {
            if (managedDependencySources.isEmpty()) {
                getLog().debug("Initializing managed dependency sources with default sources ...");
                // Initialize the managed dependencies with the XWiki WAR dependencies and Standard flavor dependency
                if (useXWikiWARDependencies) {
                    managedDependencySources.add(new ManagedDependencySource(PLATFORM_GROUP_ID,
                        XWIKI_WAR_DEPENDENCIES_ARTIFACT_ID, platformVersion, POM, null));
                }
                if (useXWikiStandardFlavorDependencies) {
                    managedDependencySources.add(new ManagedDependencySource(PLATFORM_GROUP_ID,
                        XWIKI_STANDARD_FLAVOR_ARTIFACT_ID, platformVersion, POM, null));
                }
            }

            if (dependencyGroupIDFilters.isEmpty()) {
                getLog().debug("Initializing dependency group ID filters with XWiki Standard group IDs");
                dependencyGroupIDFilters.addAll(DEFAULT_GROUP_ID_FILTERS);
            }

            if (getLog().isDebugEnabled()) {
                getLog().debug("Registering managed dependencies form the following artifacts :");
                for (ManagedDependencySource managedDependencySource : managedDependencySources) {
                    getLog().debug(String.format("- [%s]", managedDependencySource.toString()));
                }
            }

            for (ManagedDependencySource managedDependencySource : managedDependencySources) {
                addManagedDependencies(managedDependencySource);
            }
        } catch (DependencyResolverException e) {
            throw new MojoExecutionException("Failed to inject managed dependencies", e);
        }
    }

    private void addManagedDependencies(ManagedDependencySource managedDependencySource) throws DependencyResolverException
    {
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setRemoteRepositories(remoteRepositories);

        Iterable<ArtifactResult> artifactResults =
            dependencyResolver.resolveDependencies(buildingRequest,
                managedDependencySource.toDependableCoordinate(), null);

        for (final ArtifactResult artifactResult : artifactResults) {
            // Check if the result is part of XWiki projects
            Artifact artifact = artifactResult.getArtifact();
            if (dependencyGroupIDFilters.contains(artifact.getGroupId())) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug(
                        String.format("Adding artifact [%s:%s/%s/%s] in scope [%s] to project dependency management",
                            artifact.getGroupId(),
                            artifact.getArtifactId(),
                            artifact.getVersion(),
                            artifact.getType(),
                            artifact.getScope()));
                }

                Dependency dependency = new Dependency();
                dependency.setGroupId(artifact.getGroupId());
                dependency.setArtifactId(artifact.getArtifactId());
                dependency.setVersion(artifact.getVersion());
                dependency.setType(artifact.getType());
                dependency.setScope(artifact.getScope());
                dependency.setClassifier(artifact.getClassifier());

                project.getDependencyManagement().addDependency(dependency);
            }
        }
    }
}
