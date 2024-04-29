package org.quiltmc.quilt_gradle.remapping.ext;

import org.gradle.api.Action;
import org.gradle.api.InvalidUserCodeException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.Attribute;
import org.quiltmc.quilt_gradle.remapping.RemapTransform;
import org.quiltmc.quilt_gradle.remapping.settings.RemapSettings;
import org.quiltmc.quilt_gradle.remapping.settings.RemapSettingsImpl;

import java.io.File;

public class MappingsExtensionImpl implements MappingsExtension {
    private final DependencyHandler deps;

    private String targetMapppings;
    private String defaultSourceMappings = "intermediary";
    private String minecraftDependency;

    public MappingsExtensionImpl(DependencyHandler deps) {
        this.deps = deps;
    }

    @Override
    public void addMappings(String fromNamespace, String toNamespace, Object notation) {
        Dependency dep = this.deps.add("mappings", notation);

        System.out.println("registering transform " + fromNamespace + " to " + toNamespace);

        // register a transform
        this.deps.registerTransform(RemapTransform.class, spec -> {
            spec.getFrom().attribute(RemapTransform.MAPPINGS, fromNamespace);
            spec.getTo().attribute(RemapTransform.MAPPINGS, toNamespace);
//            spec.getParameters().getMappingsFile().set(new File("aaa"));
        });
    }

    @Override
    public void setTargetMappings(String namespace) {
        if (this.targetMapppings != null) {
            throw new InvalidUserCodeException("Cannot set target mappings more than once!");
        }

        this.targetMapppings = namespace;
    }

    @Override
    public void setDefaultSourceMappings(String namespace) {
        this.defaultSourceMappings = namespace;
    }

    @Override
    public ModuleDependency remap(Object notation, Action<RemapSettings> action) {
        ModuleDependency dep = this.moduleOrThrow(notation);
        // grab the Minecraft dependency here for convenience
        if (this.minecraftDependency == null && "net.minecraft".equals(dep.getGroup())) {
            this.minecraftDependency = dep.getGroup() + ':' + dep.getName() + ':' + dep.getVersion();
        } else if (this.minecraftDependency == null) {
            throw new InvalidUserCodeException("Minecraft dependency must be declared first");
        }

        if (this.targetMapppings == null) {
            throw new InvalidUserCodeException("Target mappings must be set first");
        }

        RemapSettingsImpl settings = new RemapSettingsImpl(this.defaultSourceMappings, this.minecraftDependency);
        action.execute(settings);

        // set the requested mappings
        dep.attributes(attributes -> attributes.attribute(RemapTransform.MAPPINGS, this.targetMapppings));

        // modify metadata
        String module = dep.getGroup() + ':' + dep.getName();
        deps.getComponents().withModule(module, metadata -> metadata.allVariants(variant -> {
            // set the source mappings
            variant.attributes(attrs -> attrs.attribute(RemapTransform.MAPPINGS, settings.sourceMappings));
            // add remap context to dependencies so that it's present in the transform
            // Minecraft is included by default
            variant.withDependencies(dependencies -> settings.remapContext.forEach(dependencies::add));
        }));

        return dep;
    }

    @Override
    public ModuleDependency remap(Object notation) {
        return this.remap(notation, settings -> {});
    }

    private ModuleDependency moduleOrThrow(Object notation) {
        Dependency dep = notation instanceof Dependency d ? d : this.deps.create(notation);
        if (dep instanceof ModuleDependency module)
            return module;
        throw new InvalidUserDataException("Cannot remap non-module dependency: " + notation);
    }
}
