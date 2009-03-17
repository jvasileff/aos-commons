package org.anodyneos.commons.net;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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

    //private static final Log log = LogFactory.getLog(URLChangeRootURIHandler.class);

    private URL rootURL;
    private URI rootURI;

    public URLChangeRootURIHandler() {
        // expect future call to setRootURL
    }

    /*
    public URLChangeRootURIHandler(String rootURL) throws URISyntaxException, MalformedURLException {
        setRootURL(rootURL);
    }
    */

    public URLChangeRootURIHandler(URL rootURL) throws URISyntaxException {
        setRootURL(rootURL);
    }

    /**
     *  Returns a URL for the given URI or null if the URI cannot be resolved.
     *
     *  @return The URL or null.
     */
    public URL toURL(URI uri) {
        if (null == rootURI) {
            return  null;
        } else {
            String path = uri.getPath();
            if (null == path || ! path.startsWith("/")) {
                return null;
            } else {
                try {
                    URI newURI = new URI(rootURI.getScheme()
                            ,rootURI.getUserInfo()
                            ,rootURI.getHost()
                            ,rootURI.getPort()
                            ,rootURI.getPath() + uri.getPath().substring(1)
                            ,uri.getQuery()
                            ,uri.getFragment());

                    newURI = newURI.normalize();
                    if (! newURI.getPath().startsWith(rootURI.getPath())) {
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

    public URL getRootURL() {
        return rootURL;
    }

    /*
    public void setRootURL(String rootURL) throws URISyntaxException, MalformedURLException {
        setRootURL(new URL(rootURL));
    }
    */

    public void setRootURL(URL rootURL) throws URISyntaxException {
        URI uri = rootURL.toURI();
        uri = uri.normalize();
        if (uri.isOpaque()) {
            throw new URISyntaxException(uri.toString(),
                    "Opaque URLs are not supported; scheme specific part must have a leading '/': " + uri.toString());
        }
        if (! uri.getPath().endsWith("/")) {
            uri = new URI(uri.toString() + "/");
        }
        this.rootURL = rootURL;
        this.rootURI = uri;
    }

}
