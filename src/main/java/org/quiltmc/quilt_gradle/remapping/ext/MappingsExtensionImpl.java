package org.quiltmc.quilt_gradle.remapping.ext;

import org.gradle.api.Action;
import org.gradle.api.InvalidUserCodeException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import org.quiltmc.quilt_gradle.remapping.settings.RemapSettings;
import org.quiltmc.quilt_gradle.remapping.settings.RemapSettingsImpl;

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
