package org.anodyneos.commons.net;

import java.net.URL;

public class ClassLoaderURIHandler extends URIHandler {

    private ClassLoader classLoader;

    public ClassLoaderURIHandler(ClassLoader cl) {
        this.classLoader = cl;
    }

    /**
     *  Returns a URL for the given URI or null if the URI cannot be resolved.
     *
     *  @return The URL or null.
     */
    public URL toURL(URI uri) {
        String path = uri.getPath();
        // strip off leading '/'
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return classLoader.getResource(path);
    }

}
