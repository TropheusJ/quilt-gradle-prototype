package org.quiltmc.quilt_gradle.remapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.OutputConsumerPath.Builder;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.InputArtifactDependencies;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;

import org.quiltmc.quilt_gradle.remapping.RemapTransform.Params;
import org.quiltmc.quilt_gradle.util.TinyRewrapper;

public abstract class RemapTransform implements TransformAction<Params> {
	public static final Attribute<String> MAPPINGS = Attribute.of("org.quiltmc.quilt_gradle.mappings", String.class);

	@InputArtifact
	public abstract Provider<FileSystemLocation> getInput();

	@InputArtifactDependencies
	public abstract FileCollection getDependencies();

	@Override
	public void transform(TransformOutputs outputs) {
		File input = this.getInput().get().getAsFile();
		String name = input.getName();
		System.out.println("remapping " + name + "\n" + "Dependencies: " +
				this.getDependencies().getFiles().stream().map(File::getName).toList());

		String outputName = jarless(name) + "-remapped.jar";
		File output = outputs.file(outputName);

		try {
			Files.copy(input.toPath(), output.toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (true) return;

		try (TinyRewrapper rewrapper = this.createRemapper(); OutputConsumerPath remapOutput = new Builder(output.toPath()).build()) {
			TinyRemapper remapper = rewrapper.remapper();
			remapper.readInputs(input.toPath());
			for (File file : this.getDependencies().getFiles()) {
				remapper.readClassPath(file.toPath());
			}
			System.out.println("Remapping " + name);
			long start = System.currentTimeMillis();
			remapper.apply(remapOutput);
			long ms = System.currentTimeMillis() - start;
			System.out.println("Finished remapping " + name + " in " + ms + "ms");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String jarless(String name) {
		return name.endsWith(".jar") ? name.substring(0, name.length() - ".jar".length()) : name;
	}

	private TinyRewrapper createRemapper() {
		File mappings = null;//this.getParameters().getMappingsFile().get();
		MemoryMappingTree tree = new MemoryMappingTree();
		try {
			MappingReader.read(mappings.toPath(), tree);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		TinyRemapper remapper = TinyRemapper.newRemapper()
				.withMappings(TinyUtils.createMappingProvider(tree, "source", "target"))
				.build();
		return new TinyRewrapper(remapper);
	}

	public abstract static class Params implements TransformParameters {
//		@Input
//		public abstract Property<File> getMappingsFile();
	}
}
