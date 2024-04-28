package org.quiltmc.quilt_gradle.util.io;

import java.net.URI;

public interface Downloadable {
    URI url();
    int size();
    String sha1();
}
