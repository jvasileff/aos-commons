package org.anodyneos.commons.xml.sax;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Title: SAXErrorHandler
 * Description: Simple error handler for parsing XML
 * Copyright: Copyright (c) 2000
 * Company: MarketingCentral
 * @author John Vasileff
 * @version 1.0
 */

public class SAXErrorHandler implements ErrorHandler {

    public SAXErrorHandler() {
    }
    public void error(SAXParseException ex) throws org.xml.sax.SAXException {
        System.err.println("[SAX Error] "+
            getLocationString(ex)+": "+
            ex.getMessage());
        //throw ex;
    }
    public void fatalError(SAXParseException ex) throws org.xml.sax.SAXException {
        System.err.println("[SAX Fatal Error] "+
            getLocationString(ex)+": "+
            ex.getMessage());
        throw ex;
    }
    public void warning(SAXParseException ex) throws org.xml.sax.SAXException {
        System.err.println("[SAX Warning] "+
            getLocationString(ex)+": "+
            ex.getMessage());
    }

    /** Returns a string of the location. */
    private String getLocationString(SAXParseException ex) {
        StringBuffer str = new StringBuffer();

        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
                systemId = systemId.substring(index + 1);
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();
    }
}
