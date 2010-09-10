package org.anodyneos.commons.xml.sax;

import org.xml.sax.SAXException;

public final class IntProcessor extends CDATAProcessor {

    private Integer intValue;

    public IntProcessor(BaseContext ctx) {
        super(ctx);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        String contents = getCDATA();
        if (null != contents) {
            intValue = new Integer(getCDATA());
        }
    }

    /**
     * @return null if endElement has not yet been called or if the element had
     *         no text content. Otherwise returns new Integer(contents)
     */
    public Integer getInt() {
        return intValue;
    }

}
