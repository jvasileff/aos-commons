package org.anodyneos.commons.net;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class's toURL method resolves non-Opaque URIs of the form
 * "<i>file</i>:/path/to/Resource"
 * to URLs having a leading path of the template URL provided in the constructor.  URLs returned by toURL()
 * will have the same scheme, authority, and leading path of the template URL, and the same trailing path,
 * query, and fragment components of the passed in URI.
 *
 * The template URL <i>must</i> have a fully qualified path part that ends in a "/".  URIs passed to toURL()
 * <i>must</i> have a fully qualified path part.
 *
 * NOTE: The scheme "file" is only an example. This class is not scheme
 * specific and users of this class may use another scheme name.
 *
 * @author jvas
 *
 */
public class URLChangeRootURIHandler extends AbstractURIHandler implements URIHandler {

    private static final Log log = LogFactory.getLog(URLChangeRootURIHandler.class);

    private URI templateURI;

    public URLChangeRootURIHandler(URL templateURL) throws URISyntaxException {
        log.warn("This class has never been tested.");
        URI uri = templateURL.toURI();
        uri = uri.normalize();
        if (uri.isOpaque()) {
            throw new URISyntaxException(uri.toString(),
                    "Opaque URLs are not supported; scheme specific part must have a leading '/': " + uri.toString());
        }
        if (! uri.getPath().endsWith("/")) {
            throw new URISyntaxException(uri.toString(),
                    "URL path must be a directory and end in '/': " + uri.toString());
        }
        this.templateURI = uri;
    }

    /**
     *  Returns a URL for the given URI or null if the URI cannot be resolved.
     *
     *  @return The URL or null.
     */
    public URL toURL(URI uri) {
        String path = uri.getPath();
        if (null == path || ! path.startsWith("/")) {
            return null;
        } else {
            try {
                URI newURI = new URI(templateURI.getScheme()
                        ,templateURI.getUserInfo()
                        ,templateURI.getHost()
                        ,templateURI.getPort()
                        ,templateURI.getPath() + uri.getPath().substring(1)
                        ,uri.getQuery()
                        ,uri.getFragment());

                newURI = newURI.normalize();
                if (! newURI.getPath().startsWith(templateURI.getPath())) {
                    return null;
                } else {
                    return newURI.toURL();
                }
            } catch (URISyntaxException e) {
                return null;
            } catch (MalformedURLException e) {
                return null;
            }
        }
    }

}
