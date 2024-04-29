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

		// Set up the remapping transforms
		DependencyHandler dependencies = project.getDependencies();
		// register the attribute
		dependencies.getAttributesSchema().attribute(RemapTransform.MAPPINGS);
		// register the transform itself
		dependencies.registerTransform(RemapTransform.class, spec -> {
			spec.getFrom().attribute(RemapTransform.MAPPINGS, "obfuscated");
			spec.getTo().attribute(RemapTransform.MAPPINGS, "mojmap");
			spec.getParameters().getMappingsFile().set(project.file("client.txt"));
		});
		// all jar artifacts have mappings set to obfuscated
		dependencies.getArtifactTypes().getByName("jar").getAttributes()
				.attribute(RemapTransform.MAPPINGS, "obfuscated");

		// request all dependencies to be in mojmap
		project.getConfigurations().configureEach(configuration -> {
			if (configuration.isCanBeResolved()) {
				configuration.getAttributes().attribute(RemapTransform.MAPPINGS, "mojmap");
			}
		});



		//
		dependencies.getComponents().all(details -> details.allVariants(variant -> variant.withDependencies(deps -> {
//			deps.add("net.minecraft:minecraft-client:1.20.4");
		})));
	}
}
