package org.anodyneos.commons.xml.sax;

import org.anodyneos.commons.net.URI;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.helpers.NamespaceSupport;

public class BaseContext {

    private InputSource inputSource;
    private Locator locator;
    private NamespaceSupport namespaceSupport = new NamespaceSupport();

    public BaseContext(InputSource inputSource) {
        this.inputSource = inputSource;
    }

    public void setLocator(Locator locator) {
        this.locator = locator;
    }
    public Locator getLocator() {
        return locator;
    }
    public InputSource getInputSource() {
        return inputSource;
    }

    public URI uriFromRelative(String location)
            throws URI.MalformedURIException {

        String baseSystemId = null;
        URI locationURI;
        if (null != getLocator()) {
            // use systemId from locator
            baseSystemId = getLocator().getSystemId();
        } else if (null != getInputSource()) {
            // use systemId from inputSource
            baseSystemId = getInputSource().getSystemId();
        }
        if (baseSystemId != null) {
            locationURI = new URI(new URI(baseSystemId), location);
        } else {
            locationURI = new URI(location);
        }
        return locationURI;
    }

    /**
     * @return Returns the namespaceSupport maintained by BaseDh.
     */
    public NamespaceSupport getNamespaceSupport() {
        return namespaceSupport;
    }
}
