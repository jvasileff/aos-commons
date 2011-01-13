package org.anodyneos.commons.xml.xsl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.anodyneos.commons.xml.UnifiedResolver;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

/**
 *  Cache stylesheets.
 *
 *  <p>
 *      The <code>Templates</code> cache should be a configurable LRU cache.
 *      Not yet implemented.  For now, all <code>Templates</code> objects are
 *      cached until <code>clear()</code> is called.
 *  </p>
 *  <p>
 *      Transformer caching/pooling is not provided.  Mike Kay provided this
 *      response to someones question:
 *  </p>
 *  <pre>
 *  http://www.biglist.com/lists/xsl-list/archives/200109/msg00581.html
 *
 *  * Subject: RE: [xsl] Suggestions to use Transformer concurrently
 *  * From: "Michael Kay" <mhkay@iclway.co.uk>
 *  * Date: Tue, 11 Sep 2001 14:47:51 +0100
 *  * Importance: Normal
 *
 *  &gt; Would anyone suggest how to use Transformer concurrently?  I have read
 *  &gt; a O'Reilly and it suggests that one should use Template class to hold
 *  &gt; the xslt and then create a new instance of Transformer to every
 *  &gt; client.
 *
 *  Yes, that's what you should do. Compile the stylesheet once into a
 *  Templates object, then use it to create a new Transformer for each
 *  transformation, in a different thread if you want. In principle you can
 *  reuse the Transformer once a transformation is finished, but I wouldn't
 *  recommend it. You are certainly NOT allowed to run several transform()
 *  methods in different threads using the same Transformer object.
 *
 *  Mike Kay
 *  </pre>
 */

/*
    TransformerFactory.setURIResolver() used for xsl:import and xsl:include
    Transformer.setURIResolver() used for document()
*/

public class TemplatesCacheImpl implements TemplatesCache {

    private ErrorListener errorListener;
    private ErrorHandler errorHandler;
    private Cache cache = new Cache();
    private boolean cacheEnabled = true;
    private TransformerFactory tFactory;
    private SAXParserFactory saxParserFactory;
    private UnifiedResolver resolver;

    // CONSTRUCTORS
    public TemplatesCacheImpl() {
        setSAXParserFactory(SAXParserFactory.newInstance());
        setTransformerFactory(TransformerFactory.newInstance());
    }

    // PROPERTIES

    /**
     * NOTE: The provided TransformerFactory will be re-configured
     * to use the UnifiedResolver (if or when set.)
     *
     * @param tFactory The TransformerFactory to reconfigure and use.
     */
    @Override
    public void setTransformerFactory(TransformerFactory tFactory) {
        if (null != resolver) {
            tFactory.setURIResolver(resolver);
        }
        this.tFactory = tFactory;
    }

    @Override
    public TransformerFactory getTransformerFactory() {
        return tFactory;
    }

    /**
     * NOTE: The provided SAXParserFactory will be re-configured
     * to disable validation and enable namespaces.
     *
     * @param saxParserFactory The SAXParserFactory to reconfigure and use.
     */
    @Override
    public void setSAXParserFactory(SAXParserFactory saxParserFactory) {
        saxParserFactory.setValidating(false);
        saxParserFactory.setNamespaceAware(true);
        this.saxParserFactory = saxParserFactory;
    }

    @Override
    public SAXParserFactory getSAXParserFactory() {
        return saxParserFactory;
    }

    @Override
    public void setUnifiedResolver(UnifiedResolver resolver) {
        this.resolver = resolver;
        tFactory.setURIResolver(resolver);
    }

    @Override
    public UnifiedResolver getUnifiedResolver() {
        return resolver;
    }

    @Override
    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
        tFactory.setErrorListener(errorListener);
    }

    @Override
    public ErrorListener getErrorListener() {
        return errorListener;
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public void setCacheEnabled(boolean cacheEnabled) { this.cacheEnabled = cacheEnabled; }
    @Override
    public boolean getCacheEnabled() { return this.cacheEnabled; }


    // CACHE MANAGEMENT
    @Override
    public void clearCache() {
        cache.clear();
    }
    @Override
    public int getCacheSize() {
        return cache.size();
    }

    // GET XML FILTER
    @Override
    public XMLFilter getXMLFilter(Source source)
    throws TransformerConfigurationException, IOException {
        XMLFilter f = ((SAXTransformerFactory) tFactory).newXMLFilter(getTemplates(source));
        if (null != errorHandler) {
            f.setErrorHandler(errorHandler);
        }
        return f;
    }

    @Override
    public XMLFilter getXMLFilter(URI uri)
    throws TransformerConfigurationException, IOException {
        XMLFilter f = ((SAXTransformerFactory) tFactory).newXMLFilter(getTemplates(uri));
        if (null != errorHandler) {
            f.setErrorHandler(errorHandler);
        }
        return f;
    }

    @Override
    public XMLFilter getXMLFilter(URL url)
    throws TransformerConfigurationException, IOException {
        XMLFilter f = ((SAXTransformerFactory) tFactory).newXMLFilter(getTemplates(url));
        if (null != errorHandler) {
            f.setErrorHandler(errorHandler);
        }
        return f;
    }

    // GET TRANSFORMER HANDLER
    @Override
    public TransformerHandler getTransformerHandler()
    throws TransformerConfigurationException, IOException {
        return ((SAXTransformerFactory) tFactory).newTransformerHandler();
    }

    @Override
    public TransformerHandler getTransformerHandler(Source source)
    throws TransformerConfigurationException, IOException {
        return ((SAXTransformerFactory) tFactory).newTransformerHandler(getTemplates(source));
    }

    @Override
    public TransformerHandler getTransformerHandler(URI uri)
    throws TransformerConfigurationException, IOException {
        return ((SAXTransformerFactory) tFactory).newTransformerHandler(getTemplates(uri));
    }

    @Override
    public TransformerHandler getTransformerHandler(URL url)
    throws TransformerConfigurationException, IOException {
        return ((SAXTransformerFactory) tFactory).newTransformerHandler(getTemplates(url));
    }

    // GET TRANSFORMER
    @Override
    public Transformer getTransformer() throws TransformerConfigurationException {
        Transformer t =  tFactory.newTransformer();
        if (null != errorListener) {
            t.setErrorListener(errorListener);
        }
        //t.setURIResolver(resolver);
        return t;
    }

    @Override
    public Transformer getTransformer(Source source)
    throws TransformerConfigurationException, IOException {
        Transformer t = getTemplates(source).newTransformer();
        if (null != errorListener) {
            t.setErrorListener(errorListener);
        }
        t.setURIResolver(resolver);
        return t;
    }

    /**
     *  @exception FileNotFoundException
     *  @exception IOException
     *  @exception TransformerConfigurationException
     */
    @Override
    public Transformer getTransformer(URI uri)
    throws TransformerConfigurationException, IOException {
        Transformer t = getTemplates(uri).newTransformer();
        if (null != errorListener) {
            t.setErrorListener(errorListener);
        }
        t.setURIResolver(resolver);
        return t;
    }

    /**
     *  @exception FileNotFoundException
     *  @exception IOException
     *  @exception TransformerConfigurationException
     */
    @Override
    public Transformer getTransformer(URL url)
    throws TransformerConfigurationException, IOException {
        Transformer t = getTemplates(url).newTransformer();
        if (null != errorListener) {
            t.setErrorListener(errorListener);
        }
        t.setURIResolver(resolver);
        return t;
    }

    /**
     *  Get templates.
     *
     *  @exception FileNotFoundException
     *  @exception IOException
     *  @exception TransformerConfigurationException
     */
    private Templates getTemplates(String systemId, URL url)
    throws IOException, TransformerConfigurationException {
        Templates templates;
        Entry oldEntry = cache.get(systemId);
        InputStream is = null;
        try {
            if(url.getProtocol().equals("file")) {
                File resourceFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
                long lastModified = resourceFile.lastModified();
                if (null == oldEntry || (lastModified > oldEntry.lastModified)) {
                    is = new FileInputStream(resourceFile);
                    templates = tFactory.newTemplates(newSource(is, systemId));
                    cache.put(systemId, new Entry(templates, lastModified));
                } else {
                    templates = oldEntry.templates;
                }
            } else {
                URLConnection conn = url.openConnection();
                is = conn.getInputStream();
                long lastModified = conn.getLastModified();
                if (null == oldEntry || (lastModified > oldEntry.lastModified)) {
                    templates = tFactory.newTemplates(newSource(is, systemId));
                    cache.put(systemId, new Entry(templates, lastModified));
                } else {
                    templates = oldEntry.templates;
                }
            }
        } finally {
            try { if(is != null) is.close(); } catch (Exception e) { /* no op */ }
        }
        return templates;
    }

    private Templates getTemplates(Source source)
    throws TransformerConfigurationException, IOException {
        String systemId = source.getSystemId();
        Entry entry = null;
        Templates templates = null;

        // don't bother with lastModified since we don't know what it is
        entry = cache.get(systemId);
        if (null != entry) {
            templates = entry.templates;
        } else {
            templates = tFactory.newTemplates(source);
            cache.put(systemId, new Entry(templates));
        }
        return templates;
    }

    /**
     *  @exception FileNotFoundException
     *  @exception IOException
     *  @exception TransformerConfigurationException
     */
    private Templates getTemplates(URI uri)
    throws TransformerConfigurationException, IOException {
        String systemId = uri.toString();
        Templates templates = null;

        URL url = null;
        // try resolver first
        if (null != resolver) {
            url = resolver.toURL(uri);
        }
        if(url != null) {
            templates = getTemplates(systemId, url);
        } else if (resolver.isDefaultLookupEnabled()) {
            // try default resolver
            url = new URL(uri.toString());
            templates = getTemplates(systemId, url);
        } else {
            // external lookups disabled and not found
            throw new FileNotFoundException(uri.toString());
        }

        return templates;
    }

    /**
     *  @exception FileNotFoundException
     *  @exception IOException
     *  @exception TransformerConfigurationException
     */
    private Templates getTemplates(URL url)
    throws TransformerConfigurationException, IOException {
        try {
            return getTemplates(new URI(url.toExternalForm()));
        } catch (URISyntaxException e) {
            // this should not happen
            throw new Error(e);
        }
    }

    /***************/
    public Transformer getAssociatedTransformer(Document doc) {
        /*
        ArrayList stylesheets = new ArrayList();
        // loop through all PIs.  Return latest match.
        try {
            for (Node child = doc.getFirstChild();
                 child != null;
                 child = child.getNextSibling()) {
                if (child.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                    ProcessingInstruction pi = (ProcessingInstruction) child;
                    if (pi.getNodeName().equals("xml-stylesheet")) {
                        PIA pia = new PIA(pi);
                        if ("text/xsl".equals(pia.getAttribute("type"))) {
                            String tempURL = pia.getAttribute("href");
                            String attribute = pia.getAttribute(attributeName);
                            if ((attribute != null) && (attribute.indexOf(attributeValue) > -1))
                                return tempURL;
                            if (!"yes".equals(pia.getAttribute("alternate")))
                                returnURL = tempURL;
                        }
                    }
                } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                    // no more PIs after first element
                    break;
                }
            }
        } catch (Exception saxExc) {
        }
        */
        return null;
    }

    /**
     *  Handle the xml-stylesheet processing instruction.
     *
     *  @param target The processing instruction target.
     *  @param data The processing instruction data, or null if
     *             none is supplied.
     *  @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     *  @see org.xml.sax.ContentHandler#processingInstruction
     *  @see <a href="http://www.w3.org/TR/xml-stylesheet/">Associating Style Sheets with XML documents, Version 1.0</a>
     */
    public String processingInstruction(String target, String data) throws SAXException {
        return null;
        /*
        if (! target.equals("xml-stylesheet")) {
            return null;
        }
        String href = null;  // CDATA #REQUIRED
        String type = null;  // CDATA #REQUIRED
        String title = null;  // CDATA #IMPLIED
        String media = null;  // CDATA #IMPLIED
        String charset = null;  // CDATA #IMPLIED
        boolean alternate = false;  // (yes|no) "no"
        StringTokenizer tokenizer = new StringTokenizer(data, " \t=\n", true);
        boolean lookedAhead = false;
        Source source = null;

        String token = "";
        while (tokenizer.hasMoreTokens()) {
            if (!lookedAhead) {
                token = tokenizer.nextToken();
            } else {
                lookedAhead = false;
            }
            if (tokenizer.hasMoreTokens() &&
                    (token.equals(" ") || token.equals("\t") || token.equals("="))) {
                continue;
            }

            String name = token;
            if (name.equals("type")) {
                token = tokenizer.nextToken();
                while (tokenizer.hasMoreTokens() &&
                        (token.equals(" " ) || token.equals("\t") || token.equals("="))) {
                    token = tokenizer.nextToken();
                }
                type = token.substring(1, token.length() - 1);
            } else if (name.equals("href")) {
                token = tokenizer.nextToken();
                while (tokenizer.hasMoreTokens() &&
                        (token.equals(" " ) || token.equals("\t") || token.equals("="))) {
                    token = tokenizer.nextToken();
                }
                href = token;
                if (tokenizer.hasMoreTokens()) {
                    token = tokenizer.nextToken();
                    // If the href value has parameters to be passed to a
                    // servlet(something like "foobar?id=12..."),
                    // we want to make sure we get them added to
                    // the href value. Without this check, we would move on
                    // to try to process another attribute and that would be
                    // wrong.
                    // We need to set lookedAhead here to flag that we
                    // already have the next token.
                    while ( token.equals("=") && tokenizer.hasMoreTokens()) {
                        href = href + token + tokenizer.nextToken();
                        if (tokenizer.hasMoreTokens()) {
                            token = tokenizer.nextToken();
                            lookedAhead = true;
                        } else {
                            break;
                        }
                    }
                }
                href = href.substring(1, href.length() - 1);
                try {
                    // Add code to use a URIResolver. Patch from Dmitri Ilyin.
                    if (m_uriResolver != null) {
                        source = m_uriResolver.resolve(href, m_baseID);
                    } else {
                        href = SystemIDResolver.getAbsoluteURI(href, m_baseID);
                        source = new SAXSource(new InputSource(href));
                    }
                } catch(TransformerException te) {
                    throw new org.xml.sax.SAXException(te);
                }
            } else if (name.equals("title")) {
                token = tokenizer.nextToken();
                while (tokenizer.hasMoreTokens() &&
                        (token.equals(" " ) || token.equals("\t") || token.equals("="))) {
                    token = tokenizer.nextToken();
                }
                title = token.substring(1, token.length() - 1);
            } else if (name.equals("media")) {
                token = tokenizer.nextToken();
                while (tokenizer.hasMoreTokens() &&
                        (token.equals(" " ) || token.equals("\t") || token.equals("="))) {
                    token = tokenizer.nextToken();
                }
                media = token.substring(1, token.length() - 1);
            } else if (name.equals("charset")) {
                token = tokenizer.nextToken();
                while (tokenizer.hasMoreTokens() &&
                        (token.equals(" " ) || token.equals("\t") || token.equals("="))) {
                    token = tokenizer.nextToken();
                }
                charset = token.substring(1, token.length() - 1);
            } else if (name.equals("alternate")) {
                token = tokenizer.nextToken();
                while (tokenizer.hasMoreTokens() &&
                        (token.equals(" " ) || token.equals("\t") || token.equals("="))) {
                    token = tokenizer.nextToken();
                }
                alternate = token.substring(1, token.length() - 1).equals("yes");
            }

        }

        if ((null != type)
                && (type.equals("text/xsl") || type.equals("text/xml") || type.equals("application/xml+xslt"))
                && (null != href)) {
            if (null != m_media) {
                if (null != media) {
                    if (!media.equals(m_media)) {
                        return;
                    }
                } else {
                    return;
                }
            }
            if (null != m_charset) {
                if (null != charset) {
                    if (!charset.equals(m_charset)) {
                        return;
                    }
                } else {
                    return;
                }
            }
            if (null != m_title) {
                if (null != title) {
                    if (!title.equals(m_title)) {
                        return;
                    }
                } else {
                    return;
                }
            }
            m_stylesheets.addElement(source);
        }
        */
    }



    /***************/

    // MEMBER CLASSES
    private final class Entry {
        private long lastModified;
        private Templates templates;

        Entry(Templates templates) {
            this(templates, 0);
        }
        Entry(Templates templates, long lastModified) {
            this.lastModified = lastModified;
            this.templates = templates;
        }
    }

    private final class Cache {
        private HashMap<String, Entry> map = new HashMap<String, Entry>();
        Cache() {
            // super();
        }
        void put(String systemId, Entry entry) {
            if (cacheEnabled && systemId != null && ! "".equals(systemId)) {
                map.put(systemId, entry);
            }
        }
        Entry get(String systemId) {
            if (cacheEnabled && systemId != null && ! "".equals(systemId)) {
                return map.get(systemId);
            } else {
                return null;
            }
        }
        void clear() {
            map.clear();
        }
        int size() {
            return map.size();
        }
    }

    private Source newSource(InputStream is, String systemId) throws TransformerConfigurationException {
        // It would be much easier to do:
        //      return new StreamSource(is, systemId);
        // but, we would like to specify the ErrorHandler...

        SAXParser sp;
        XMLReader reader;
        try {
            sp = saxParserFactory.newSAXParser();
            reader = sp.getXMLReader();
        } catch (ParserConfigurationException e) {
            throw new TransformerConfigurationException(e);
        } catch (SAXException e) {
            throw new TransformerConfigurationException(e);
        }
        if (null != errorHandler) {
            reader.setErrorHandler(errorHandler);
        }
        reader.setEntityResolver(resolver);
        SAXSource src = new SAXSource(new InputSource(is));
        src.setSystemId(systemId);
        src.setXMLReader(reader);
        return src;
    }
}
