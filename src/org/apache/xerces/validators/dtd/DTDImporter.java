/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.validators.dtd;

import org.apache.xerces.framework.XMLContentSpec;
import org.apache.xerces.framework.XMLDocumentHandler; // for DTDHandler
import org.apache.xerces.framework.XMLDTDScanner;
import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.readers.DefaultEntityHandler;
import org.apache.xerces.readers.XMLEntityHandler;
import org.apache.xerces.utils.ChunkyCharArray;
import org.apache.xerces.utils.QName;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.utils.XMLCharacterProperties;
import org.apache.xerces.utils.XMLMessages;
import org.apache.xerces.validators.common.XMLValidator;

public final class DTDImporter 
    implements XMLDTDScanner.EventHandler {

    // REVISIT: DTDs don't have a namespace so we must bind the
    //          empty namespace, "", so that validation works when
    //          namespace processing is turned on.

    //
    // Data
    //

    private StringPool fStringPool = null;
    private boolean fValidating = false;
    private int fStandaloneReader = -1;
    private XMLDTDScanner fDTDScanner = null;
    private XMLErrorReporter fErrorReporter = null;
    private DefaultEntityHandler fEntityHandler = null;
    private boolean fNamespacesEnabled = false;
    private XMLDocumentHandler.DTDHandler fDTDHandler = null;
    private int fCDATASymbol = -1;
    private int fIDSymbol = -1;
    private boolean fDeclsAreExternal = false;
    private XMLValidator fValidator = null;

    //
    // Constructors
    //

    /** Constructs a DTD importer. */
    public DTDImporter(StringPool stringPool,
                       XMLErrorReporter errorReporter,
                       DefaultEntityHandler entityHandler,
                       XMLValidator validator) {

        fStringPool = stringPool;
        fErrorReporter = errorReporter;
        fEntityHandler = entityHandler;
        fValidator = validator;
        fDTDScanner = new XMLDTDScanner(fStringPool, fErrorReporter, fEntityHandler, new ChunkyCharArray(fStringPool));
        fDTDScanner.setEventHandler(this);
        init();

    } // <init>(StringPool,XMLErrorReporter,DefaultEntityHandler,XMLValidator)

    /** Initialize handlers. */
    public void initHandlers(XMLDocumentHandler.DTDHandler dtdHandler) {
        fDTDHandler = dtdHandler;
    }

    public void setValidating(boolean validating) {
        fValidating = validating;
    }
    public void setNamespacesEnabled(boolean namespacesEnabled) {
        fNamespacesEnabled = namespacesEnabled;
    }
    //
    //
    //
    public void reset(StringPool stringPool) throws Exception {
        fStringPool = stringPool;
        fDTDScanner.reset(stringPool, new ChunkyCharArray(fStringPool));
        fStandaloneReader = -1;
        init();
    }
    //
    //
    //
    public void sendEndOfInputNotifications(int entityName, boolean moreToFollow) throws Exception {
        if (fValidating) {
            int readerDepth = fEntityHandler.getReaderDepth();
            if (fDTDScanner.getReadingContentSpec()) {
                int parenDepth = fDTDScanner.parenDepth();
                if (readerDepth != parenDepth) {
                    reportRecoverableXMLError(XMLMessages.MSG_IMPROPER_GROUP_NESTING,
                                                XMLMessages.VC_PROPER_GROUP_PE_NESTING,
                                                entityName);
                }
            } else {
                int markupDepth = fDTDScanner.markupDepth();
                if (readerDepth != markupDepth) {
                    reportRecoverableXMLError(XMLMessages.MSG_IMPROPER_DECLARATION_NESTING,
                                                XMLMessages.VC_PROPER_DECLARATION_PE_NESTING,
                                                entityName);
                }
            }
        }
        fDTDScanner.endOfInput(entityName, moreToFollow);
    }
    public void sendReaderChangeNotifications(XMLEntityHandler.EntityReader reader, int readerId) throws Exception {
        fDTDScanner.readerChange(reader, readerId);
        fDeclsAreExternal = fStandaloneReader != -1 && readerId != fStandaloneReader;
    }

    //
    // XMLDTDScanner.EventHandler interface
    //
    //    public boolean validVersionNum(String version) throws Exception;
    //    public boolean validEncName(String encoding) throws Exception;
    //    public int validPublicId(String publicId) throws Exception;
    //    public void callTextDecl(int version, int encoding) throws Exception;
    //    public void doctypeDecl(int rootElementType, int publicId, int systemId) throws Exception;
    //    public void startReadingFromExternalSubset(int publicId, int systemId) throws Exception;
    //    public void stopReadingFromExternalSubset() throws Exception;
    //    public int addElementDecl(int elementType) throws Exception;
    //    public int addElementDecl(int elementType, int contentSpecType, int contentSpec) throws Exception;
    //    public int addAttDef(int elementIndex, int attName, int attType, int enumeration, int attDefaultType, int attDefaultValue) throws Exception;
    //    public int addUniqueLeafNode(int nameIndex) throws Exception;
    //    public int addContentSpecNode(int nodeType, int nodeValue) throws Exception;
    //    public int addContentSpecNode(int nodeType, int leftNodeIndex, int rightNodeIndex) throws Exception;
    //    public String getContentSpecNodeAsString(int nodeIndex) throws Exception;
    //    public boolean startEntityDecl(boolean isPE, int entityName) throws Exception;
    //    public void endEntityDecl() throws Exception;
    //    public int addInternalPEDecl(int name, int value) throws Exception;
    //    public int addExternalPEDecl(int name, int publicId, int systemId) throws Exception;
    //    public int addInternalEntityDecl(int name, int value) throws Exception;
    //    public int addExternalEntityDecl(int name, int publicId, int systemId) throws Exception;
    //    public int addUnparsedEntityDecl(int name, int publicId, int systemId, int notationName) throws Exception;
    //    public int startEnumeration() throws Exception;
    //    public void addNameToEnumeration(int enumIndex, int elementType, int attrName, int nameIndex, boolean isNotationType) throws Exception;
    //    public void endEnumeration(int enumIndex) throws Exception;
    //    public int addNotationDecl(int notationName, int publicId, int systemId) throws Exception;
    //    public void callComment(int data) throws Exception;
    //    public void callProcessingInstruction(int piTarget, int piData) throws Exception;
    //    public int scanElementType(XMLEntityHandler.EntityReader entityReader, char fastchar) throws Exception;
    //    public int checkForElementTypeWithPEReference(XMLEntityHandler.EntityReader entityReader, char fastchar) throws Exception;
    //    public int checkForAttributeNameWithPEReference(XMLEntityHandler.EntityReader entityReader, char fastcheck) throws Exception;
    //    public int checkForNameWithPEReference(XMLEntityHandler.EntityReader entityReader, char fastcheck) throws Exception;
    //    public int checkForNmtokenWithPEReference(XMLEntityHandler.EntityReader entityReader, char fastcheck) throws Exception;
    //    public int scanDefaultAttValue(int elementType, int attrName, int attType, int enumeration) throws Exception;
    //    public void internalSubset(int internalSubset) throws Exception;
    //
    public void callTextDecl(int version, int encoding) throws Exception {
        fDTDHandler.textDecl(version, encoding);
    }
    public void doctypeDecl(QName rootElement, int publicId, int systemId) throws Exception {
        rootElement.uri = StringPool.EMPTY_STRING; // REVISIT: DTD + Namespaces
        fValidator.setRootElementType(rootElement);
        fDTDHandler.startDTD(rootElement, publicId, systemId);
    }

    /** Adds an element declaration. */ 
    public int addElementDecl(QName elementDecl) throws Exception {
        elementDecl.uri = StringPool.EMPTY_STRING; // REVISIT: DTD + Namespaces
        int elementIndex = fValidator.addElement(elementDecl);
        return elementIndex;
    }

    /** Adds an element declaration. */
    public int addElementDecl(QName elementDecl, 
                              int contentSpecType, int contentSpec) 
        throws Exception {

        elementDecl.uri = StringPool.EMPTY_STRING; // REVISIT: DTD + Namespaces
        int elementIndex = fValidator.addElementDecl(elementDecl, contentSpecType, contentSpec, fDeclsAreExternal);
        if (elementIndex == -1) {
            if (fValidating) {
                reportRecoverableXMLError(XMLMessages.MSG_ELEMENT_ALREADY_DECLARED,
                                          XMLMessages.VC_UNIQUE_ELEMENT_TYPE_DECLARATION,
                                          elementDecl.rawname);
            }
        } 
        else {
            fDTDHandler.elementDecl(elementDecl, fValidator.getContentSpec(elementIndex));
        }
        return elementIndex;

    } // addElementDecl(QName,int,int):int

    public int addAttDef(QName elementDecl, QName attributeDecl, 
                         int attType, int enumeration, 
                         int attDefaultType, int attDefaultValue) throws Exception {

        elementDecl.uri = StringPool.EMPTY_STRING; // REVISIT: DTD + Namespaces
        attributeDecl.uri = StringPool.EMPTY_STRING; // REVISIT: DTD + Namespaces
        int attDefIndex = fValidator.addAttDef(elementDecl, attributeDecl, 
                                               attType, enumeration, 
                                               attDefaultType, attDefaultValue, 
                                               fDeclsAreExternal);
        if (attDefIndex != -1) {
            String enumString = (enumeration == -1) ? null : fStringPool.stringListAsString(enumeration);
            fDTDHandler.attlistDecl(elementDecl, attributeDecl, 
                                    attType, enumString, 
                                    attDefaultType, attDefaultValue);
        }
        return attDefIndex;

    } // addAttDef(QName,QName,int,int,int,int):int

    public int addUniqueLeafNode(int nodeValue) throws Exception {
        int csn = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF, nodeValue, -1, true);
        if (csn == -1 && fValidating) {
            reportRecoverableXMLError(XMLMessages.MSG_DUPLICATE_TYPE_IN_MIXED_CONTENT,
                                      XMLMessages.VC_NO_DUPLICATE_TYPES,
                                      nodeValue);
        }
        return csn;
    }
    public int addContentSpecNode(int nodeType, int nodeValue) throws Exception {
        return fValidator.addContentSpecNode(nodeType, nodeValue, -1, false);
    }
    public int addContentSpecNode(int nodeType, int nodeValue, int otherNodeValue) throws Exception {
        return fValidator.addContentSpecNode(nodeType, nodeValue, otherNodeValue, false);
    }
    public String getContentSpecNodeAsString(int nodeIndex) {
        return fValidator.contentSpecNodeAsString(nodeIndex);
    }
    public boolean startEntityDecl(boolean isPE, int entityName) throws Exception {
        return fEntityHandler.startEntityDecl(isPE, entityName);
    }
    public void endEntityDecl() throws Exception {
        fEntityHandler.endEntityDecl();
    }
    public int addInternalPEDecl(int name, int value) throws Exception {
        int entityHandle = fEntityHandler.addInternalPEDecl(name, value, fDeclsAreExternal);
        fDTDHandler.internalPEDecl(name, value);
        return entityHandle;
    }
    public int addExternalPEDecl(int name, int publicId, int systemId) throws Exception {
        int entityHandle = fEntityHandler.addExternalPEDecl(name, publicId, systemId, fDeclsAreExternal);
        fDTDHandler.externalPEDecl(name, publicId, systemId);
        return entityHandle;
    }
    public int addInternalEntityDecl(int name, int value) throws Exception {
        int entityHandle = fEntityHandler.addInternalEntityDecl(name, value, fDeclsAreExternal);
        fDTDHandler.internalEntityDecl(name, value);
        return entityHandle;
    }
    public int addExternalEntityDecl(int name, int publicId, int systemId) throws Exception {
        int entityHandle = fEntityHandler.addExternalEntityDecl(name, publicId, systemId, fDeclsAreExternal);
        fDTDHandler.externalEntityDecl(name, publicId, systemId);
        return entityHandle;
    }
    public int addUnparsedEntityDecl(int name, int publicId, int systemId, int notationName) throws Exception {
        int entityHandle = fEntityHandler.addUnparsedEntityDecl(name, publicId, systemId, notationName, fDeclsAreExternal);
        fDTDHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
        return entityHandle;
    }
    public int startEnumeration() {
        return fStringPool.startStringList();
    }
    public void addNameToEnumeration(int enumHandle, int elementType, int attrName, int nameIndex, boolean isNotationType) {
        fStringPool.addStringToList(enumHandle, nameIndex);
        if (isNotationType && !fEntityHandler.isNotationDeclared(nameIndex)) {
            Object[] args = { fStringPool.toString(elementType),
                              fStringPool.toString(attrName),
                              fStringPool.toString(nameIndex) };
            fEntityHandler.addRequiredNotation(nameIndex,
                                               fErrorReporter.getLocator(),
                                               XMLMessages.MSG_NOTATION_NOT_DECLARED_FOR_NOTATIONTYPE_ATTRIBUTE,
                                               XMLMessages.VC_NOTATION_DECLARED,
                                               args);
        }
    }
    public void endEnumeration(int enumHandle) {
        fStringPool.finishStringList(enumHandle);
    }
    public int addNotationDecl(int notationName, int publicId, int systemId) throws Exception {
        int notationHandle = fEntityHandler.addNotationDecl(notationName, publicId, systemId, fDeclsAreExternal);
        //
        // REVISIT - I am making an arbitrary decision that is not covered by the
        //  spec (or I missed it).  If we see a second NotationDecl for the same name,
        //  we will ignore the latter declaration.  This seems consistent with the
        //  usual behavior of such things in the spec, but it might also be good to
        //  generate a warning that this has occurred.  The addNotation() method will
        //  return -1 if we already have a declaration for this name.  This will slow
        //  down the parser in direct relation to the number of NotationDecl names.
        //
        if (notationHandle != -1)
            fDTDHandler.notationDecl(notationName, publicId, systemId);
        return notationHandle;
    }
    public void callComment(int comment) throws Exception {
        fDTDHandler.comment(comment);
    }
    public void callProcessingInstruction(int target, int data) throws Exception {
        fDTDHandler.processingInstruction(target, data);
    }

    public void internalSubset(int internalSubset) throws Exception {
        fDTDHandler.internalSubset(internalSubset);
    }

    //
    // DTDImporter implementation
    //
    private void init() {
        fCDATASymbol = fStringPool.addSymbol("CDATA");
        fIDSymbol = fStringPool.addSymbol("ID");
    }
    //
    //
    //
    protected void reportRecoverableXMLError(int majorCode, int minorCode) throws Exception {
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   null,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }
    protected void reportRecoverableXMLError(int majorCode, int minorCode, int stringIndex1) throws Exception {
        Object[] args = { fStringPool.toString(stringIndex1) };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }
    protected void reportRecoverableXMLError(int majorCode, int minorCode, String string1) throws Exception {
        Object[] args = { string1 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }
    protected void reportRecoverableXMLError(int majorCode, int minorCode, int stringIndex1, int stringIndex2) throws Exception {
        Object[] args = { fStringPool.toString(stringIndex1), fStringPool.toString(stringIndex2) };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }
    protected void reportRecoverableXMLError(int majorCode, int minorCode, String string1, String string2) throws Exception {
        Object[] args = { string1, string2 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }
    protected void reportRecoverableXMLError(int majorCode, int minorCode, String string1, String string2, String string3) throws Exception {
        Object[] args = { string1, string2, string3 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }
}
