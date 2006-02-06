/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.util;

import java.util.Enumeration;

import javax.xml.XMLConstants;

import org.apache.xerces.xni.NamespaceContext;

/**
 * <p>A read-only XNI wrapper around a JAXP NamespaceContext.</p>
 * 
 * @author Michael Glavassevich, IBM
 * 
 * @version $Id: $
 */
public class JAXPNamespaceContextWrapper implements NamespaceContext {
    
    private javax.xml.namespace.NamespaceContext fNamespaceContext;
    private SymbolTable fSymbolTable;

    public JAXPNamespaceContextWrapper(SymbolTable symbolTable) {
        setSymbolTable(symbolTable);
    }
    
    public void setNamespaceContext(javax.xml.namespace.NamespaceContext context) {
        fNamespaceContext = context;
    }
    
    public javax.xml.namespace.NamespaceContext getNamespaceContext() {
        return fNamespaceContext;
    }
    
    public void setSymbolTable(SymbolTable symbolTable) {
        fSymbolTable = symbolTable;
    }
    
    public SymbolTable getSymbolTable() {
        return fSymbolTable;
    }
    
    /*
     * NamespaceContext methods
     */
    
    public String getURI(String prefix) {
        if (fNamespaceContext != null) {
            String uri = fNamespaceContext.getNamespaceURI(prefix);
            if (uri != null && !XMLConstants.NULL_NS_URI.equals(uri)) {
                return (fSymbolTable != null) ? fSymbolTable.addSymbol(uri) : uri.intern();
            }
        }
        return null;
    }

    public String getPrefix(String uri) {
        if (fNamespaceContext != null) {
            if (uri == null) {
                uri = XMLConstants.NULL_NS_URI;
            }
            String prefix = fNamespaceContext.getPrefix(uri);
            if (prefix == null) {
                prefix = XMLConstants.DEFAULT_NS_PREFIX;
            }
            return (fSymbolTable != null) ? fSymbolTable.addSymbol(prefix) : prefix.intern();
        }
        return null;
    }
    
    public Enumeration getAllPrefixes() {
        // It's not possible to get the list of all prefixes from the NamespaceContext
        // so the best we can do is return an empty enumeration.
        return new Enumeration () {
            public boolean hasMoreElements() {
                return false;
            }
            public Object nextElement() {
                return null;
            }
        };
    }

    public void pushContext() {}

    public void popContext() {}

    public boolean declarePrefix(String prefix, String uri) {
        return true;
    }

    public int getDeclaredPrefixCount() {
        return 0;
    }

    public String getDeclaredPrefixAt(int index) {
        return null;
    }

    public void reset() {}

} // JAXPNamespaceContextWrapper
