package org.anodyneos.commons.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.anodyneos.commons.net.URI;
import org.anodyneos.commons.net.URIHandler;
import org.anodyneos.commons.net.URIHelper;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @TODO: More configuration for XMLCatalogs. Allow custom properties file,
 * protocolHandler vs. catalog preference?
 */
public class UnifiedResolver extends URIHelper implements EntityResolver, URIResolver  {

    private boolean defaultLookupEnabled;
    private HashMap protocolHandlers = new HashMap();
    private CatalogManager catalogManager = null;
    private Catalog catalog = null;

    // constructors
    public UnifiedResolver() {
        this(true);
    }
    public UnifiedResolver(boolean defaultLookupEnabled) {
        setDefaultLookupEnabled(defaultLookupEnabled);
    }

    // catalog
    public synchronized void setXMLCatalogEnabled(boolean enableXMLCatalog) {
        if (enableXMLCatalog && catalog == null) {
            catalogManager = new CatalogManager();
            catalog = catalogManager.getCatalog();
        } else if (! enableXMLCatalog) {
            catalogManager = null;
            catalog = null;
        }
    }

    // default lookup
    public void setDefaultLookupEnabled(boolean defaultLookupEnabled) {
        this.defaultLookupEnabled = defaultLookupEnabled;
    }
    public boolean isDefaultLookupEnabled() {
        return defaultLookupEnabled;
    }

    // SAX EntityResolver
    public InputSource resolveEntity(String publicId, String systemId)
    throws org.xml.sax.SAXException, java.net.MalformedURLException {

        InputSource inputSource = null;

        URI systemURI;
        try {
            systemURI = new URI(systemId);
        } catch (URI.MalformedURIException e) {
            throw new MalformedURLException(systemId);
        }

        try {
            URLConnection conn = openConnection(publicId, systemURI);
            if (null == conn) {
                if (isDefaultLookupEnabled()) {
                    // Let default resolver handle all other URLs
                    return null;
                } else {
                    // Only internal resolvers are allowed, and none of them were able to find what you're looking for
                    throw new FileNotFoundException("File not found '" + systemId + "'.");
                }
            }
            InputStream inputStream = conn.getInputStream();
            inputSource = new InputSource(inputStream);
            inputSource.setPublicId(publicId);
            inputSource.setSystemId(systemId);
        } catch (java.io.IOException e) {
            throw new SAXException("IOException opening resource: '" + systemId + "'"
                    + " not found.", e);
        }

        return inputSource;
    }

    // trax URIResolver
    public Source resolve(String href, String base)
    throws javax.xml.transform.TransformerException {
        URI uri;
        try {
            if (base != null) {
                uri = new URI(new URI(base), href);
            } else {
                uri = new URI(href);
            }
        } catch (URI.MalformedURIException e) {
            throw new TransformerException(e);
        }

        Source source = null;

        try {
            URLConnection conn = openConnection(uri);
            if (null == conn) {
                if (isDefaultLookupEnabled()) {
                    // Let default resolver handle all other URLs
                    return null;
                } else {
                    // Only internal resolvers are allowed
                    throw new TransformerException("URI not supported for: '" + uri.toString()
                            + "'.");
                }
            }
            InputStream inputStream = conn.getInputStream();
            source = new StreamSource(inputStream, uri.toString());
        } catch (java.io.FileNotFoundException e) {
            throw new TransformerException("Resource base '" + base + "'; href '" + href
                    + "' not found.");
        } catch (java.io.IOException e) {
            throw new TransformerException("IOException opening resource base '" + base
                    + "'; href '" + href + "'.", e);
        }

        return source;
    }

    protected synchronized URIHandler getURIHandler(URI uri) {
        return (URIHandler) protocolHandlers.get(uri.getScheme());
    }

    public synchronized void addProtocolHandler(String protocol, URIHandler uriHandler) {
        protocolHandlers.put(protocol, uriHandler);
    }

    /**
     * Override URIHelper.openStream() to provide support for XML Catalog.
     */
    public InputStream openStream(URI uri) throws IOException {
        return openStream(null, uri);
    }

    /**
     *  Override URIHelper.openConnection() to provide support for XML Catalog.
     */
    public URLConnection openConnection(URI uri) throws IOException {
        return openConnection(null, uri);
    }

    /**
     * Override URIHelper.toURL() to provide support for XML Catalog.
     */
    public URL toURL(URI uri) {
        return toURL(null, uri);
    }

    /**
     * Adds support for XML Catalog.
     */
    public InputStream openStream(String publicId, URI uri) throws IOException {
        String systemId = null;
        if (null != uri) {
            systemId = uri.toString();
        }
        URL catURL = getURLFromCatalog(publicId, systemId);
        if(null != catURL) {
            return catURL.openStream();
        } else if (null != uri) {
            return super.openStream(uri);
        } else {
            return null;
        }
    }

    /**
     * Adds support for XML Catalog.
     */
    public URLConnection openConnection(String publicId, URI uri) throws IOException {
        String systemId = null;
        if (null != uri) {
            systemId = uri.toString();
        }
        URL catURL = getURLFromCatalog(publicId, systemId);
        if(null != catURL) {
            return catURL.openConnection();
        } else if (null != uri) {
            return super.openConnection(uri);
        } else {
            return null;
        }
    }

    /**
     * Adds support for XML Catalog.
     */
    public URL toURL(String publicId, URI uri) {
        String systemId = null;
        if (null != uri) {
            systemId = uri.toString();
        }
        URL catURL = getURLFromCatalog(publicId, systemId);
        if(null != catURL) {
            return catURL;
        } else if (null != uri) {
            return super.toURL(uri);
        } else {
            return null;
        }
    }

    /**
     * Try to get a URL from the XML catalog.
     * @param publicId
     * @param systemId
     * @return the URL or null if catalog does not exist or cannot resolve.
     */
    protected URL getURLFromCatalog(String publicId, String systemId) {
        if(null != catalog) {
            String result = null;
            try {
                result = catalog.resolvePublic(publicId, systemId);
            } catch (Exception e) {
                // nop;
            }
            if (result != null) {
                try {
                    return new URL(result);
                } catch(MalformedURLException e) {
                    // nop;
                }
            }
        }
        // catalog == null or Exception
        return null;
    }
}
