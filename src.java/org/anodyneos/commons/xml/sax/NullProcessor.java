package org.anodyneos.commons.xml.sax;

import org.xml.sax.SAXException;

public class NullProcessor extends ElementProcessor {

    public NullProcessor(BaseContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        return this;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
    }
}

