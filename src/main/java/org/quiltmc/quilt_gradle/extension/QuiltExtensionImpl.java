package org.quiltmc.quilt_gradle.extension;

import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class QuiltExtensionImpl implements QuiltExtension {
    @Inject
    protected abstract Project getProject();
}
