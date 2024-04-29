package org.quiltmc.quilt_gradle.remapping.settings;

import java.util.HashSet;
import java.util.Set;

public class RemapSettingsImpl implements RemapSettings {
    public final Set<String> remapContext = new HashSet<>();
    public String sourceMappings;

    public RemapSettingsImpl(String defaultSourceMappings, String minecraftDependency) {
        this.sourceMappings = defaultSourceMappings;
        this.remapContext.add(minecraftDependency);
    }

    @Override
    public void setSourceMappings(String namespace) {
        this.sourceMappings = namespace;
    }

    @Override
    public void addRemapContext(String dependency) {
        this.remapContext.add(dependency);
    }
}
