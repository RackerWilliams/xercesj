/*
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000-2002 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Sun Microsystems, Inc., 
 * http://www.sun.com.  For more information on the Apache Software 
 * Foundation, please see <http://www.apache.org/>.
 */

package org.apache.xerces.jaxp;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.HandlerBase;
import org.xml.sax.Parser;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderAdapter;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import org.apache.xerces.impl.Constants;

import java.util.*;

/**
 * @author Rajiv Mordani
 * @author Edwin Goei
 * @version $Id$
 */

/**
 * This is the implementation specific class for the
 * <code>javax.xml.parsers.SAXParser</code>. 
 */
public class SAXParserImpl extends javax.xml.parsers.SAXParser
        implements JAXPConstants {
    private XMLReader xmlReader;
    private String schemaLanguage = null;     // null means DTD
    
    /**
     * Create a SAX parser with the associated features
     * @param features Hashtable of SAX features, may be null
     */
    SAXParserImpl(SAXParserFactory spf, Hashtable features)
        throws SAXException
    {
        // Instantiate a SAXParser directly and not through SAX so that we
        // use the right ClassLoader
        xmlReader = new org.apache.xerces.parsers.SAXParser();

        // If validating, provide a default ErrorHandler that prints
        // validation errors with a warning telling the user to set an
        // ErrorHandler.
        if (spf.isValidating()) {
            xmlReader.setErrorHandler(new DefaultValidationErrorHandler());
        }

        xmlReader.setFeature(Constants.SAX_FEATURE_PREFIX +
                             Constants.VALIDATION_FEATURE, spf.isValidating());

        // JAXP "namespaceAware" == SAX Namespaces feature
        // Note: there is a compatibility problem here with default values:
        // JAXP default is false while SAX 2 default is true!
        xmlReader.setFeature(Constants.SAX_FEATURE_PREFIX +
                             Constants.NAMESPACES_FEATURE,
                             spf.isNamespaceAware());

        // SAX "namespaces" and "namespace-prefixes" features should not
        // both be false.  We make them opposite for backward compatibility
        // since JAXP 1.0 apps may want to receive xmlns* attributes.
        xmlReader.setFeature(Constants.SAX_FEATURE_PREFIX +
                             Constants.NAMESPACE_PREFIXES_FEATURE,
                             !spf.isNamespaceAware());

        setFeatures(features);
    }

    /**
     * Set any features of our XMLReader based on any features set on the
     * SAXParserFactory.
     *
     * XXX Does not handle possible conflicts between SAX feature names and
     * JAXP specific feature names, eg. SAXParserFactory.isValidating()
     */
    private void setFeatures(Hashtable features)
        throws SAXNotSupportedException, SAXNotRecognizedException
    {
        if (features != null) {
            for (Enumeration e = features.keys(); e.hasMoreElements();) {
                String feature = (String)e.nextElement();
                boolean value = ((Boolean)features.get(feature)).booleanValue();
                xmlReader.setFeature(feature, value);
            }
        }
    }

    public Parser getParser() throws SAXException {
        // Xerces2 AbstractSAXParser implements SAX1 Parser
        // assert(xmlReader instanceof Parser);
        return (Parser) xmlReader;
    }

    /**
     * Returns the XMLReader that is encapsulated by the implementation of
     * this class.
     */
    public XMLReader getXMLReader() {
        return xmlReader;
    }

    public boolean isNamespaceAware() {
        try {
            return xmlReader.getFeature(Constants.SAX_FEATURE_PREFIX +
                                        Constants.NAMESPACES_FEATURE);
        } catch (SAXException x) {
            throw new IllegalStateException(x.getMessage());
        }
    }

    public boolean isValidating() {
        try {
            return xmlReader.getFeature(Constants.SAX_FEATURE_PREFIX +
                                        Constants.VALIDATION_FEATURE);
        } catch (SAXException x) {
            throw new IllegalStateException(x.getMessage());
        }
    }

    /**
     * Sets the particular property in the underlying implementation of 
     * org.xml.sax.XMLReader.
     */
    public void setProperty(String name, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if (JAXP_SCHEMA_LANGUAGE.equals(name)) {
            // JAXP 1.2 support
            if (W3C_XML_SCHEMA.equals(value)) {
                schemaLanguage = W3C_XML_SCHEMA;
                xmlReader.setFeature(Constants.XERCES_FEATURE_PREFIX +
                                     Constants.SCHEMA_VALIDATION_FEATURE,
                                     true);
                // this will allow the parser not to emit DTD-related
                // errors, as the spec demands
                xmlReader.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            } else if (value == null) {
                schemaLanguage = null;
                xmlReader.setFeature(Constants.XERCES_FEATURE_PREFIX +
                                     Constants.SCHEMA_VALIDATION_FEATURE,
                                     false);
            } else {
                throw new SAXNotSupportedException(
                    "Unsupported schema language");
            }
        } else {
            xmlReader.setProperty(name, value);
        }
    }

    /**
     * returns the particular property requested for in the underlying 
     * implementation of org.xml.sax.XMLReader.
     */
    public Object getProperty(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if (JAXP_SCHEMA_LANGUAGE.equals(name)) {
            // JAXP 1.2 support
            return schemaLanguage;
        } else {
            return xmlReader.getProperty(name);
        }
    }
}
