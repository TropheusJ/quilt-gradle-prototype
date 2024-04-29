package org.quiltmc.quilt_gradle;

import java.net.URI;

import org.quiltmc.quilt_gradle.extension.QuiltExtension;
import org.quiltmc.quilt_gradle.extension.QuiltExtensionImpl;
import org.quiltmc.quilt_gradle.minecraft.mcmaven.McMavenConnectorFactory;
import org.quiltmc.quilt_gradle.remapping.RemapTransform;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransportFactory;
import org.quiltmc.quilt_gradle.remapping.ext.MappingsExtension;
import org.quiltmc.quilt_gradle.remapping.ext.MappingsExtensionImpl;

import javax.inject.Inject;

public abstract class QuiltGradlePlugin implements Plugin<Project> {
	private final RepositoryTransportFactory repositoryTransportFactory;

	@Inject
	public QuiltGradlePlugin(RepositoryTransportFactory repositoryTransportFactory) {
		this.repositoryTransportFactory = repositoryTransportFactory;
	}

	@Override
	public void apply(Project project) {
		// inject a new URL protocol, used to intercept requests for Minecraft resources.
		McMavenConnectorFactory.inject(repositoryTransportFactory, project.getGradle().getGradleUserHomeDir());

		QuiltExtension extension = project.getExtensions().create(
				QuiltExtension.class, "quilt", QuiltExtensionImpl.class
		);

		// Minecraft repositories
		project.getRepositories().maven(repo -> {
			repo.setName("Minecraft Libraries");
			repo.setUrl(URI.create("https://libraries.minecraft.net/"));
		});

		project.getRepositories().maven(repo -> {
			repo.setName("Minecraft");
            repo.setUrl(URI.create("mcmaven:///"));
        });

		// Set up remapping
		DependencyHandler dependencies = project.getDependencies();
		// register the attribute
		dependencies.getAttributesSchema().attribute(RemapTransform.MAPPINGS);
		// configuration for mappings files
		project.getConfigurations().create("mappings");
		// extension for adding mappings and remapped dependencies
		dependencies.getExtensions().create(
				MappingsExtension.class, "mappings", MappingsExtensionImpl.class,
				dependencies
		);
	}
}
