package org.quiltmc.quilt_gradle.util;

import net.fabricmc.tinyremapper.TinyRemapper;

public record TinyRewrapper(TinyRemapper remapper) implements AutoCloseable {
	@Override
	public void close() {
		this.remapper.finish();
	}
}
