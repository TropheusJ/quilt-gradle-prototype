package org.quiltmc.quilt_gradle.util.io;

import java.io.IOException;

public interface IoSupplier<T> {
    T get() throws IOException;
}
