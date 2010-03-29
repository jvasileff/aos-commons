package org.anodyneos.commons.xml.sax;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Stack;

import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.helpers.NamespaceSupport;

public class BaseContext {

    private InputSource inputSource;
    private Locator locator;
    private NamespaceSupport namespaceSupport = new NamespaceSupport();
    private Stack<Boolean> xmlSpacePreserve = new Stack<Boolean>();

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
            throws URISyntaxException {

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
            URI baseURI = new URI(baseSystemId);
            locationURI = baseURI.resolve(location);
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

    public boolean isXmlSpacePreserve() {
        if (xmlSpacePreserve.empty()) {
            return false;
        } else {
            return xmlSpacePreserve.peek().booleanValue();
        }
    }

    void pushXmlSpacePreserve(boolean value) {
        xmlSpacePreserve.push(Boolean.valueOf(value));
    }

    void popXmlSpacePreserve() {
        xmlSpacePreserve.pop();
    }

}
