package org.anodyneos.commons.xml.sax;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class BaseParser {

    public BaseParser() {
        // super
    }

    public void process(InputSource is, ElementProcessor p) throws SAXException, IOException {
        process(is, p, null);
    }

    public void process(InputSource is, ElementProcessor p, EntityResolver resolver)
            throws SAXException, IOException {
        DefaultHandler dh = new BaseDh(p);
        SAXParser sp = newSAXParser();
        XMLReader reader = sp.getXMLReader();
        //reader.setErrorHandler(new SAXErrorHandler());
        reader.setContentHandler(dh);
        if (resolver != null) {
            reader.setEntityResolver(resolver);
        }
        reader.parse(is);
    }

    private SAXParser newSAXParser() throws SAXException {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setFeature("http://xml.org/sax/features/namespaces", true);
            spf.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            return spf.newSAXParser();
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
    }

}
