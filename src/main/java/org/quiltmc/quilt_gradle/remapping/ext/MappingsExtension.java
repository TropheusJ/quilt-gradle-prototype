package org.quiltmc.quilt_gradle.remapping.ext;

import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;
import org.quiltmc.quilt_gradle.remapping.settings.RemapSettings;

public interface MappingsExtension {
    /**
     * Add a new set of mappings between two namespaces.
     * This should be done first.
     */
    void addMappings(String fromNamespace, String toNamespace, Object notation);

    /**
     * After adding all used mappings using {@link #addMappings(String, String, Object)},
     * declare the target namespace. This is the mappings set that will actually be used by the project.
     */
    void setTargetMappings(String namespace);

    /**
     * Set the default source mappings. These are the mappings that dependencies
     * are assumed to be using, unless otherwise specified. Defaults to 'intermediary'.
     */
    void setDefaultSourceMappings(String namespace);

    /**
     * Declare that the provided dependency should be remapped.<br>
     * Only module dependencies are remappable, this will fail for other types.<br>
     * Example usage: {@code remap(implementation("org.myorg:my-mod:1.0.0"))}
     *
     * @param notation a dependency, in any notation.
     */
    ModuleDependency remap(Object notation);

    /**
     * A shortcut for depending on Minecraft.
     */
    default ModuleDependency remapMinecraft(Object notation) {
        return remap(notation, settings -> settings.setSourceMappings("obfuscated"));
    }

    /**
     * Remap the given dependency, configuring how it will be remapped.
     * @see #remap(Object)
     */
    ModuleDependency remap(Object notation, Action<RemapSettings> action);
}
