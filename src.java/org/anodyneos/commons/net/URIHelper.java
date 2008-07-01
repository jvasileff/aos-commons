package org.anodyneos.commons.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * <code>URIHelper</code> s match <code>URI</code> s to <code>URIHandler</code>
 * s and provide helper methods to resolve <code>URI</code>s. Subclasses may
 * also resolve URIs in a customer manner, such as using an XML Catalog. A more
 * appropriate name would be <code>URIResolver</code>, but that name is
 * already in use.
 *
 * @see URI
 * @see URIHandler
 */
public abstract class URIHelper {

    protected URIHelper() {
    }

    /**
     *  Returns a URIHandler that should be used to resolve the given URI or
     *  null if no available URIHandler is suitable.
     *
     *  @return The URIHandler or null.
     */
    protected abstract URIHandler getURIHandler(URI uri);

    /**
     *  Returns a URLConnection for the given URI or null if the URI cannot be
     *  resolved.
     *
     *  @return The URLConnection or null.
     *  @throws IOException if an IOException occurs
     */
    public URLConnection openConnection(URI uri) throws IOException {
        URIHandler uriHandler = getURIHandler(uri);
        if (null == uriHandler) {
            return null;
        }
        return uriHandler.openConnection(uri);
    }

    /**
     *  Returns an InputStream for the given URI or null if the URI cannot be
     *  resolved.
     *
     *  @return The InputStream or null.
     *  @throws IOException if an IOException occurs
     */
    public InputStream openStream(URI uri) throws IOException {
        URIHandler uriHandler = getURIHandler(uri);
        if (null == uriHandler) {
            return null;
        }
        return uriHandler.openStream(uri);
    }

    /**
     *  Returns a URL for the given URI or null if the URI cannot be resolved.
     *  Note: there is no guarantee that the returned URL points to an existing
     *  resource, only that the URI was able to be resolved to a URL.
     *
     *  @return The URL or null.
     */
    public URL toURL(URI uri) {
        return getURIHandler(uri).toURL(uri);
    }

}
