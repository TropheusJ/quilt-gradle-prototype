package org.quiltmc.quilt_gradle.remapping.settings;

public interface RemapSettings {
    /**
     * Change the source mappings of this dependency.
     * Defaults to 'intermediary'.
     */
    void setSourceMappings(String namespace);

    /**
     * Add another dependency that will be present when remapping this one.
     * This is needed in some cases when dependencies have undeclared dependencies.<br>
     * Example: {@code addRemapContext("my.org:my-other-mod:1.0.0")}
     */
    void addRemapContext(String dependency);
}
