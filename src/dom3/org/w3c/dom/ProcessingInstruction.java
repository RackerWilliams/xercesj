/*
 * Copyright (c) 2002 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */

package org.w3c.dom;

/**
 * The <code>ProcessingInstruction</code> interface represents a "processing 
 * instruction", used in XML as a way to keep processor-specific information 
 * in the text of the document. The property [notation] defined in  is not 
 * accessible from DOM Level 3 Core. 
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20020409'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface ProcessingInstruction extends Node {
    /**
     * The target of this processing instruction. XML defines this as being 
     * the first token following the markup that begins the processing 
     * instruction.
     * <br> This attribute represents the property [target] defined in . 
     */
    public String getTarget();

    /**
     * The content of this processing instruction. This is from the first non 
     * white space character after the target to the character immediately 
     * preceding the <code>?&gt;</code>.
     * <br> This attribute represents the property [content] defined by the 
     * Processing Instruction Information Item in . 
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     */
    public String getData();
    /**
     * The content of this processing instruction. This is from the first non 
     * white space character after the target to the character immediately 
     * preceding the <code>?&gt;</code>.
     * <br> This attribute represents the property [content] defined by the 
     * Processing Instruction Information Item in . 
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     */
    public void setData(String data)
                               throws DOMException;

}
