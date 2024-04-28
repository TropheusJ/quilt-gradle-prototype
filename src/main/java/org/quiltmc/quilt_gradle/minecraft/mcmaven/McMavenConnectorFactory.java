package org.quiltmc.quilt_gradle.minecraft.mcmaven;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.quiltmc.quilt_gradle.util.NoOpResourceLister;
import org.quiltmc.quilt_gradle.util.NoOpUploader;
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransportFactory;
import org.gradle.authentication.Authentication;
import org.gradle.internal.resource.connector.ResourceConnectorFactory;
import org.gradle.internal.resource.connector.ResourceConnectorSpecification;
import org.gradle.internal.resource.transfer.DefaultExternalResourceConnector;
import org.gradle.internal.resource.transfer.ExternalResourceConnector;

public class McMavenConnectorFactory implements ResourceConnectorFactory {
	private final File gradleHome;

    public McMavenConnectorFactory(File gradleHome) {
        this.gradleHome = gradleHome;
    }

    @Override
	public Set<String> getSupportedProtocols() {
		return Set.of("mcmaven");
	}

	@Override
	public Set<Class<? extends Authentication>> getSupportedAuthentication() {
		return Set.of();
	}

	@Override
	public ExternalResourceConnector createResourceConnector(ResourceConnectorSpecification connectionDetails) {
		return new DefaultExternalResourceConnector(
				new McMavenResourceAccessor(this.gradleHome),
				NoOpResourceLister.INSTANCE,
				NoOpUploader.INSTANCE
		);
	}

	public static void inject(RepositoryTransportFactory factory, File gradleHome) {
		try {
			Field registeredProtocols = RepositoryTransportFactory.class.getDeclaredField("registeredProtocols");
			registeredProtocols.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<ResourceConnectorFactory> list = (List<ResourceConnectorFactory>) registeredProtocols.get(factory);
			// don't add if it already exists
			if (list.stream().anyMatch(f -> f instanceof McMavenConnectorFactory))
				return;

			list.add(new McMavenConnectorFactory(gradleHome));
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Error accessing gradle internals", e);
		}
	}
}
