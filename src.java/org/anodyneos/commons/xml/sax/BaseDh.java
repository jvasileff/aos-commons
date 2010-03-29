package org.anodyneos.commons.xml.sax;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BaseDh extends DefaultHandler {

    private static final Log log = LogFactory.getLog(BaseDh.class);

    protected BaseContext ctx;
    private ElementProcessor topProcessor;
    private Stack<ElementProcessor> processorStack = new Stack<ElementProcessor>();

    /**
     *  endPrefixMapping() calls are made after endElement() pops the processorStack
     *  but apply to the elementProcessor for which the mappings were created.
     */
    private ElementProcessor lastProcessor;

    /** map keys are prefixes, values are namespace URIs */
    private List<String[]> cachedStartPrefixMappings = new ArrayList<String[]>();

    public BaseDh (ElementProcessor topProcessor) {
        this.topProcessor = topProcessor;
        this.ctx = topProcessor.getContext();
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        if (log.isDebugEnabled()) {
            log.debug(MessageFormat.format("startElement(''{0}'', ''{1}'', ''{2}'', attributes)",
                    new String[] { uri, localName, qName }));
        }

        ElementProcessor oldProcessor;
        ElementProcessor newProcessor;

        if (processorStack.empty()) {
            newProcessor = topProcessor;
        } else {
            oldProcessor = processorStack.peek();
            newProcessor = oldProcessor.getProcessorFor(uri, localName, qName, attributes);
        }
        processorStack.push(newProcessor);

        ctx.getNamespaceSupport().pushContext();
        for (Iterator<String[]> it = cachedStartPrefixMappings.iterator(); it.hasNext();) {
            String[] mapping = it.next();
            ctx.getNamespaceSupport().declarePrefix(mapping[0], mapping[1]);
            newProcessor.startPrefixMapping(mapping[0], mapping[1]);
        }
        cachedStartPrefixMappings.clear();

        // http://www.w3.org/TR/REC-xml/#sec-white-space
        String xmlSpace = attributes.getValue("xml:space");
        if("preserve".equals(xmlSpace)) {
           ctx.pushXmlSpacePreserve(true);
        } else if ("default".equals(xmlSpace)) {
           ctx.pushXmlSpacePreserve(false);
        } else {
           ctx.pushXmlSpacePreserve(ctx.isXmlSpacePreserve());
        }

        newProcessor.startElement(uri, localName, qName, attributes);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (log.isDebugEnabled()) {
            log.debug(MessageFormat.format("endElement(''{0}'', ''{1}'', ''{2}'')",
                    new String[] { uri, localName, qName }));
        }

        ElementProcessor processor = processorStack.pop();
        processor.endElement(uri, localName, qName);
        // NOTE: we are popping the context even though calls may be made to endPrefixMapping()
        ctx.getNamespaceSupport().popContext();
        ctx.popXmlSpacePreserve();
        lastProcessor = processor;
    }

    public void characters(char[] chars, int start, int length) throws SAXException {
        ElementProcessor processor = processorStack.peek();
        processor.characters(chars, start, length);
    }

    public void startDocument() throws SAXException {
        if (log.isDebugEnabled()) {
            log.debug("startDocument()");
        }
    }

    public void endDocument() throws SAXException {
        if (log.isDebugEnabled()) {
            log.debug("endDocument()");
        }
    }

    public void setDocumentLocator(Locator locator) {
        if (log.isDebugEnabled()) {
            log.debug("setDocumentLocator(locator)");
        }
        ctx.setLocator(locator);
    }

    public void startPrefixMapping(java.lang.String prefix, java.lang.String uri) throws SAXException {
        if (log.isDebugEnabled()) {
            log.debug(MessageFormat.format("startPrefixMapping(''{0}'', ''{1}'')",
                    new String[] { prefix, uri }));
        }
        cachedStartPrefixMappings.add(new String[] { prefix, uri });
    }

    public void endPrefixMapping(java.lang.String prefix) throws SAXException {
        if (log.isDebugEnabled()) {
            log.debug(MessageFormat.format("endPrefixMapping(''{0}'')",
                    new String[] { prefix }));
        }
        if (null != lastProcessor) {
            lastProcessor.endPrefixMapping(prefix);
        }
    }

}
