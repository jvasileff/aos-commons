package org.anodyneos.commons.xml.sax;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ElementProcessor extends org.xml.sax.helpers.DefaultHandler {

    protected BaseContext ctx;

    public ElementProcessor(BaseContext ctx) {
        this.ctx = ctx;
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        return getProcessorFor(uri, localName, qName);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        throw new SAXParseException("Element not allowed here: <" + qName + ">;", getContext().getLocator());
    }

    @Override
    public final void setDocumentLocator(Locator locator) {
        // should get the locator from the context
    }
    public Locator getLocator() {
        return ctx.getLocator();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String s = new String(ch, start, length);
        if (s.trim().length() != 0) {
            throw new SAXParseException("Element content not allowed here;", getContext().getLocator());
        }
    }

    public BaseContext getContext() {
        return ctx;
    }
}

