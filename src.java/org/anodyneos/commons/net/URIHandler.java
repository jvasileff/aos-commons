package org.anodyneos.commons.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public interface URIHandler {

    URLConnection openConnection(URI uri) throws IOException;
    InputStream openStream(URI uri) throws IOException;
    URL toURL(URI uri);

}
