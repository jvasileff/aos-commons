package org.anodyneos.commons.xml.xsl;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.anodyneos.commons.xml.UnifiedResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.XMLFilter;

public interface TemplatesCache {

    void setTransformerFactory(TransformerFactory tFactory);
    TransformerFactory getTransformerFactory();

    void setSAXParserFactory(SAXParserFactory saxParserFactory);
    SAXParserFactory getSAXParserFactory();

    void setUnifiedResolver(UnifiedResolver resolver);
    UnifiedResolver getUnifiedResolver();

    void setErrorListener(ErrorListener errorListener);
    ErrorListener getErrorListener();

    void setErrorHandler(ErrorHandler errorHandler);
    ErrorHandler getErrorHandler();

    void setCacheEnabled(boolean cacheEnabled);
    boolean getCacheEnabled();

    void clearCache();
    int getCacheSize();

    // FILTERS

    XMLFilter getXMLFilter(Source source) throws TransformerConfigurationException, IOException;
    XMLFilter getXMLFilter(URI uri) throws TransformerConfigurationException, IOException;
    XMLFilter getXMLFilter(URL url) throws TransformerConfigurationException, IOException;

    // TRANSFORMER HANDLERS

    TransformerHandler getTransformerHandler() throws TransformerConfigurationException, IOException;
    TransformerHandler getTransformerHandler(Source source) throws TransformerConfigurationException, IOException;
    TransformerHandler getTransformerHandler(URI uri) throws TransformerConfigurationException, IOException;
    TransformerHandler getTransformerHandler(URL url) throws TransformerConfigurationException, IOException;

    // TRANSFORMERS

    Transformer getTransformer() throws TransformerConfigurationException;
    Transformer getTransformer(Source source) throws TransformerConfigurationException, IOException;
    Transformer getTransformer(URI uri) throws TransformerConfigurationException, IOException;
    Transformer getTransformer(URL url) throws TransformerConfigurationException, IOException;

}
