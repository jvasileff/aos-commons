package org.anodyneos.commons.net;

import java.net.URI;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class's toURL method resolves non-Opaque URIs of the form
 * "<i>classpath</i>:///com/example/package/Resource" to URLs for the give
 * ClassLoader resource. The scheme, authority, query, and fragment portions of
 * the URI are disregarded. The URI should not include leading backslash
 * characters.
 *
 * NOTE: The scheme "classpath" is only an example. This class is not scheme
 * specific and users of this class may use another scheme name.
 *
 * @author jvas
 */
public class ClassLoaderURIHandler extends AbstractURIHandler implements URIHandler {

    private static final Log log = LogFactory.getLog(ClassLoaderURIHandler.class);

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
    @Override
    public URL toURL(URI uri) {
        if (null == classLoader) {
            log.warn("Returning null; no classloader defined while processing URI: " + uri.toString());
            return null;
        }

        if (uri.isOpaque()) {
            log.warn("Returning null; opaque URI's (scheme-specific part does not begin with a slash) are invalid: "
                    + uri.toString());
            return null;
        }

        String path = uri.getPath();

        // non-opaque, path will never be null
        if (! path.startsWith("/")) {
            // non-opaque URI's may have paths without a leading "/" if they have no scheme.
            log.warn("Returning null; path for URI is not absolute: " + uri.toString());
            return null;
        }

        // strip off leading '/'
        return classLoader.getResource(path.substring(1));
    }

    public ClassLoader getClassLoader() { return classLoader; }
    public void setClassLoader(ClassLoader classLoader) { this.classLoader = classLoader; }

}
