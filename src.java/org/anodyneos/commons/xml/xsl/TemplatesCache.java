package org.anodyneos.commons.xml.xsl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.anodyneos.commons.net.URI;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

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

public class TemplatesCache {

    /** TODO: ErrorListener ? */
    private Cache cache = new Cache();
    private boolean cacheEnabled = true;
    private TransformerFactory tFactory = TransformerFactory.newInstance();
    private UnifiedResolver resolver;

    // CONSTRUCTORS
    public TemplatesCache() {
    }
    public TemplatesCache(UnifiedResolver resolver) {
        tFactory.setURIResolver(resolver);
        this.resolver = resolver;
    }

    // PROPERTIES
    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }
    public boolean getCacheEnabled() {
        return this.cacheEnabled;
    }
    public void clearCache() {
        cache.clear();
    }
    public int getCacheSize() {
        return cache.size();
    }

    // GET XML FILTER
    public XMLFilter getXMLFilter(Source source)
    throws TransformerConfigurationException, IOException {
        return ((SAXTransformerFactory) tFactory).newXMLFilter(getTemplates(source));
    }

    public XMLFilter getXMLFilter(URI uri)
    throws TransformerConfigurationException, IOException {
        return ((SAXTransformerFactory) tFactory).newXMLFilter(getTemplates(uri));
    }

    public XMLFilter getXMLFilter(URL url)
    throws TransformerConfigurationException, IOException {
        return ((SAXTransformerFactory) tFactory).newXMLFilter(getTemplates(url));
    }

    // GET XML FILTER
    public TransformerHandler getTransformerHandler()
    throws TransformerConfigurationException, IOException {
        return ((SAXTransformerFactory) tFactory).newTransformerHandler();
    }

    public TransformerHandler getTransformerHandler(Source source)
    throws TransformerConfigurationException, IOException {
        return ((SAXTransformerFactory) tFactory).newTransformerHandler(getTemplates(source));
    }

    public TransformerHandler getTransformerHandler(URI uri)
    throws TransformerConfigurationException, IOException {
        return ((SAXTransformerFactory) tFactory).newTransformerHandler(getTemplates(uri));
    }

    public TransformerHandler getTransformerHandler(URL url)
    throws TransformerConfigurationException, IOException {
        return ((SAXTransformerFactory) tFactory).newTransformerHandler(getTemplates(url));
    }

    // GET TRANSFORMER
    public Transformer getTransformer() throws TransformerConfigurationException {
        return tFactory.newTransformer();
    }

    public Transformer getTransformer(Source source)
    throws TransformerConfigurationException, IOException {
        return getTemplates(source).newTransformer();
    }

    /**
     *  @exception FileNotFoundException
     *  @exception IOException
     *  @exception TransformerConfigurationException
     */
    public Transformer getTransformer(URI uri)
    throws TransformerConfigurationException, IOException {
        return getTemplates(uri).newTransformer();
    }

    /**
     *  @exception FileNotFoundException
     *  @exception IOException
     *  @exception TransformerConfigurationException
     */
    public Transformer getTransformer(URL url)
    throws TransformerConfigurationException, IOException {
        return getTemplates(url).newTransformer();
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
                File resourceFile = new File(url.getFile());
                long lastModified = resourceFile.lastModified();
                if (null == oldEntry || (null != oldEntry && lastModified > oldEntry.lastModified)) {
                    is = new FileInputStream(resourceFile);
                    templates = tFactory.newTemplates(
                            new StreamSource(is, systemId));
                    cache.put(systemId, new Entry(templates, lastModified));
                } else {
                    templates = oldEntry.templates;
                }
            } else {
                URLConnection conn = url.openConnection();
                is = conn.getInputStream();
                long lastModified = conn.getLastModified();
                if (null == oldEntry || (null != oldEntry && lastModified > oldEntry.lastModified)) {
                    templates = tFactory.newTemplates(
                            new StreamSource(is, systemId));
                    cache.put(systemId, new Entry(templates, lastModified));
                } else {
                    templates = oldEntry.templates;
                }
            }
        } finally {
            try { if(is != null) is.close(); } catch (Exception e) { }
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
        } else if (null == resolver || resolver.isDefaultLookupEnabled()) {
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
        } catch (URI.MalformedURIException e) {
            // this should not happen
            return null;
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
     */
    public UnifiedResolver getResolver() {
        // @TODO: is this safe?  The resolver can be modified.
        return resolver;
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
        private HashMap map = new HashMap();
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
                return (Entry) map.get(systemId);
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
}
