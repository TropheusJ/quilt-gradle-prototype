package org.quiltmc.quilt_gradle;

import java.net.URI;

import org.quiltmc.quilt_gradle.extension.QuiltExtension;
import org.quiltmc.quilt_gradle.extension.QuiltExtensionImpl;
import org.quiltmc.quilt_gradle.minecraft.mcmaven.McMavenConnectorFactory;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransportFactory;

import javax.inject.Inject;

public abstract class QuiltGradlePlugin implements Plugin<Project> {
	private final RepositoryTransportFactory repositoryTransportFactory;

	@Inject
	public QuiltGradlePlugin(RepositoryTransportFactory repositoryTransportFactory) {
		this.repositoryTransportFactory = repositoryTransportFactory;
	}

	@Override
	public void apply(Project project) {
		McMavenConnectorFactory.inject(repositoryTransportFactory, project.getGradle().getGradleUserHomeDir());

		QuiltExtension extension = project.getExtensions().create(
				QuiltExtension.class, "quilt", QuiltExtensionImpl.class
		);

		project.getRepositories().maven(repo -> {
			repo.setName("Minecraft Libraries");
			repo.setUrl(URI.create("https://libraries.minecraft.net/"));
		});

		project.getRepositories().maven(repo -> {
			repo.setName("Minecraft");
            repo.setUrl(URI.create("mcmaven:///"));
        });
	}
}
