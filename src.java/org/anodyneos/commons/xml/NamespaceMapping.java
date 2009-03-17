package org.anodyneos.commons.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 * Can be used to track prefix to namespace mappings when implementing a SAX
 * content handler. As a content handler recieves <code>startPrefixMapping</code>
 * and <code>endPrefixMapping</code> calls, the content handler should call
 * <code>push</code> and <code>pop</code> on an instance of this class.
 * <code>peek</code> may be called at any time to see the current namespace
 * uri that is mapped to a prefix.
 *
 * This class uses a <code>Stack</code> to keep track of multiple uri
 * mappings for a prefix. Prefixes may be any string including the empty
 * string.
 *
 * TODO: make more compatible with javax.xml.namespaces.NamespaceContext with regards to well known URIs.
 *
 * @author jvas
 */
public class NamespaceMapping {

    /**
     * Keys are prefix names, values are a stack of URIs with the top of the
     * stack representing the current mapping.
     */
    private HashMap prefixMap = new HashMap();

    public NamespaceMapping() {
        super();
    }

    /**
     * Register a new prefix mapping. Normally called by a content handler in
     * its <code>startPrefixMapping</code> method.
     *
     * @param nnPrefix
     * @param nnUri
     */
    public void push(String prefix, String uri) {
        String nnPrefix = prefix;
        String nnUri = uri;
        if (null == nnPrefix) {
            nnPrefix = "";
        }
        if (null == nnUri) {
            nnUri = "";
        }
        Stack stack = (Stack) prefixMap.get(nnPrefix);
        if (null == stack) {
            stack = new Stack();
            prefixMap.put(nnPrefix, stack);
        }
        stack.push(nnUri);
    }

    /**
     * Remove a prefix mapping. Normally called by a content handler in its
     * <code>endPrefixMapping</code> method.
     *
     * @param prefix
     * @return the namespace uri being popped.
     */
    public String pop(String prefix) {
        Stack stack = (Stack) prefixMap.get(prefix);
        if (null == stack) {
            return "";
        } else {
            String uri = (String) stack.pop();
            if (stack.isEmpty()) {
                prefixMap.remove(prefix);
            }
            return uri;
        }
    }

    /**
     * Returns the current namespace uri for the given prefix.
     *
     * @param prefix
     * @return The namespace uri
     */
    public String getNamespaceURI(String prefix) {
        Stack stack = (Stack) prefixMap.get(prefix);
        if (null == stack) {
            return "";
        } else {
            return (String) stack.peek();
        }
    }

    /**
     * Can be used to determin wether a prefix is currently registered with
     * this class. This method will return false if the prefix has never been
     * registered or if it was registered but all namespace uris have been
     * popped.
     *
     * @param prefix
     * @return
     */
    public boolean prefixExists(String prefix) {
        return null != prefixMap.get(prefix);
    }

    /**
     * Provides reverse lookup; namespace uri -> prefix. The first prefix found
     * will be returned. Multiple calls to this method may return different
     * prefixes if more than one prefix is mapped to the given namespace uri.
     *
     * @param uri
     *            The uri to search for.
     * @return The prefix to use for the uri or null if none exists.
     */
    public String getPrefix(String namespaceURI) {
        Iterator it = prefixMap.keySet().iterator();
        while (it.hasNext()) {
            String prefix = (String) it.next();
            Stack stack = (Stack) prefixMap.get(prefix);
            if (namespaceURI.equals(stack.peek())) { return prefix; }
        }
        return null;
    }

    /**
     * Returns an unmodifiable Iterator for all currently bound prefixes.
     *
     * @return The iterator.
     */
    public Iterator getPrefixes() {
        return new Iterator() {
            Iterator keys = prefixMap.keySet().iterator();
            public boolean hasNext() {
                return keys.hasNext();
            }
            public Object next() {
                return keys.next();
            }
            public void remove() {
                throw new UnsupportedOperationException("This iterator is not modifiable.");
            }
        };
    }

    /**
     * Get all prefixes bound to a Namespace URI in the current scope.
     *
     * @param namespaceURI
     *            The uri to search for.
     * @return the Iterator
     */
    public Iterator getPrefixes(String namespaceURI) {
        final Set prefixes = new HashSet();

        Iterator it = prefixMap.keySet().iterator();
        while (it.hasNext()) {
            String prefix = (String) it.next();
            Stack stack = (Stack) prefixMap.get(prefix);
            if (namespaceURI.equals(stack.peek())) {
                prefixes.add(prefix);
            }
        }

        return new Iterator() {
            Iterator values = prefixes.iterator();
            public boolean hasNext() {
                return values.hasNext();
            }
            public Object next() {
                return values.next();
            }
            public void remove() {
                throw new UnsupportedOperationException("This iterator is not modifiable.");
            }
        };
    }

}
