package org.anodyneos.commons.xml.sax;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BaseDh extends DefaultHandler {

    protected BaseContext ctx;
    private ElementProcessor topProcessor;
    private Stack processorStack = new Stack();

    private boolean namespaceContextPushed = false;

    public BaseDh (ElementProcessor topProcessor) {
        this.topProcessor = topProcessor;
        this.ctx = topProcessor.getContext();
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        if (! namespaceContextPushed) {
            ctx.getNamespaceSupport().pushContext();
        }
        namespaceContextPushed = false;

        ElementProcessor oldProcessor;
        ElementProcessor newProcessor;

        if (processorStack.empty()) {
            newProcessor = topProcessor;
        } else {
            oldProcessor = ((ElementProcessor) processorStack.peek());
            newProcessor = oldProcessor.getProcessorFor(uri, localName, qName);
        }
        processorStack.push(newProcessor);
        newProcessor.startElement(uri, localName, qName, attributes);

    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        ElementProcessor processor = (ElementProcessor) processorStack.pop();
        processor.endElement(uri, localName, qName);
        ctx.getNamespaceSupport().popContext();
    }

    public void characters(char[] chars, int start, int length) throws SAXException {
        ElementProcessor processor = (ElementProcessor) processorStack.peek();
        processor.characters(chars, start, length);
    }

    public void endDocument() throws SAXException {
    }

    public void setDocumentLocator(Locator locator) {
        ctx.setLocator(locator);
    }

    public void startPrefixMapping(java.lang.String prefix, java.lang.String uri) throws SAXException {
        if (! namespaceContextPushed) {
            ctx.getNamespaceSupport().pushContext();
            namespaceContextPushed = true;
        }
        ctx.getNamespaceSupport().declarePrefix(prefix, uri);

        if (! processorStack.empty()) {
            ElementProcessor processor = (ElementProcessor) processorStack.peek();
            processor.startPrefixMapping(prefix, uri);
        }
    }

    public void endPrefixMapping(java.lang.String prefix) throws SAXException {
        if (! processorStack.empty()) {
            ElementProcessor processor = (ElementProcessor) processorStack.peek();
            processor.endPrefixMapping(prefix);
        }
    }

}
