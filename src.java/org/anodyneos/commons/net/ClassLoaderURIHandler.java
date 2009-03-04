package org.anodyneos.commons.net;

import java.net.URI;
import java.net.URL;

/**
 * This class's toURL method resolves Opaque URIs of the form
 * "<i>classpath</i>:com/example/package/Resource"
 * to URLs for the give ClassLoader resource. The URI scheme specific part of
 * the URI should not include leading backslash characters.
 *
 * NOTE: The scheme "classpath" is only an example. This class is not scheme
 * specific and users of this class may use another scheme name.
 *
 * @author jvas
 *
 */
public class ClassLoaderURIHandler extends AbstractURIHandler implements URIHandler {

    private ClassLoader classLoader;

    public ClassLoaderURIHandler() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
        if (this.classLoader == null) {
            this.classLoader = this.getClass().getClassLoader();
        }
    }

    public ClassLoaderURIHandler(ClassLoader cl) {
        this.classLoader = cl;
    }

    /**
     *  Returns a URL for the given URI or null if the URI cannot be resolved.
     *
     *  @return The URL or null.
     */
    public URL toURL(URI uri) {
        assert(uri.isOpaque());
        if (null == classLoader) {
            return null;
        } else {
            String path = uri.getSchemeSpecificPart();

            if (null == path) {
                return null;
            } else {
                return classLoader.getResource(path);
            }
        }
    }

    public ClassLoader getClassLoader() { return classLoader; }
    public void setClassLoader(ClassLoader classLoader) { this.classLoader = classLoader; }

}
