package org.anodyneos.commons.net;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 *  <code>URIHandler</code> subclasses resolve <code>URI</code>s of a specific
 *  type to <code>URL</code>s.  <b>A URIHandler instance should not be used to
 *  indiscriminantly resolve URIs</b>, but should rather be used only after a
 *  URI has been determined to be suitable for the particular URIHandler
 *  instance.  Applications should register URIHandler instances with a
 *  URIHelper instance and the URIHelper should be used to resolve URIs.
 *  <p>
 *  A particular subclass implementation should not be bound to a specific
 *  protocol - in other words, a subclasses should resolve URIs based on host,
 *  port, path, or any other parameters provided by the URI object except
 *  protocol.  This is necessary in order to allow an application to provide a
 *  custom mapping between application specific protocol names and instances of
 *  URIHandler subclasses.
 *  </p><p>
 *  Example:  An application may need to associate a protocol named "classpath"
 *  with a URIHandler that uses URLs returned from the system classloader's
 *  getResource() method.  Another application may wish to do the same, but
 *  with a different protocol name.
 *  </p><p>
 *  A more advanced usage may involve an application using two instances of the
 *  same URIHandler subclass with different configuration settings and protocol
 *  names.  For example, two protocol names could be used with each referring
 *  to a different classloader.  A URIHelper would be used to determine which
 *  URIHandler instance to use based on the URI's protocol.
 *  </p>
 *
 *  @see URI
 *  @see URIHelper
 */
public abstract class URIHandler {

    protected URIHandler() {
    }

    /**
     *  Returns a URLConnection for the given URI or null if the URI cannot be
     *  resolved.
     *
     *  @return The URLConnection or null.
     *  @throws IOException if an IOException occurs
     */
    public URLConnection openConnection(URI uri) throws IOException {
        URL url = toURL(uri);
        if (null == url) {
            return null;
        }
        try {
            return url.openConnection();
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     *  Returns an InputStream for the given URI or null if the URI cannot be
     *  resolved.
     *
     *  @return The InputStream or null.
     *  @throws IOException if an IOException occurs
     */
    public InputStream openStream(URI uri) throws IOException {
        URL url = toURL(uri);
        if (null == url) {
            return null;
        }
        try {
            return url.openStream();
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     *  Returns a URL for the given URI or null if the URI cannot be resolved.
     *  Note: there is no guarantee that the returned URL points to an existing
     *  resource, only that the URI was abled to be resolved to a URL.
     *
     *  @return The URL or null.
     */
    public abstract URL toURL(URI uri);

}
