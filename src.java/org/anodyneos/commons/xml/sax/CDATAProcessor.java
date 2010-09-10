package org.anodyneos.commons.xml.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class CDATAProcessor extends ElementProcessor {

    private StringBuffer sb;
    private boolean endElement = false;
    private Attributes attributes;

    public CDATAProcessor(BaseContext ctx) {
        super(ctx);
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attrs) throws SAXException {
        this.attributes = new AttributesImpl(attrs);
    }

    @Override
    public void characters(char[] chars, int start, int length) {
        if (length > 0) {
            if (null == sb) {
                sb = new StringBuffer();
            }
            sb.append(chars, start, length);
        }
    }

    /**
     * Subclasses that override this method must call super.endElement()
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        endElement = true;
    }

    /**
     * @return null if endElement has not yet been called or if the element had
     *         no text content. Otherwise returns CDATA content of the element
     */
    public String getCDATA() {
        return null == sb ? null : sb.toString();
    }

    /**
     * @return true if endElement() has been called, false otherwise.
     */
    public boolean wasEndElementCalled() {
        return endElement;
    }

    public Attributes getAttributes() {
        return attributes;
    }

}
