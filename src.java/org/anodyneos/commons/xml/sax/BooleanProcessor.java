package org.anodyneos.commons.xml.sax;

import org.xml.sax.SAXException;

public final class BooleanProcessor extends CDATAProcessor {

    private Boolean b = null;

    public BooleanProcessor(BaseContext ctx) {
        super(ctx);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        String contents = getCDATA();
        if (null != contents) {
            b = Boolean.valueOf(contents.trim());
        }
    }

    /**
     * @return null if endElement has not yet been called or if the element had
     *         no text content. Otherwise returns
     *         Boolean.valueOf(contents.trim())
     */
    public Boolean getBoolean() {
        return b;
    }

}
