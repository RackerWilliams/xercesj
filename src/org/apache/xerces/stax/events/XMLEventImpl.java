/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.stax.events;

import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * 
 * @author Lucian Holland
 *
 * @version $Id$
 */
public abstract class XMLEventImpl implements XMLEvent {

    /**
     * Constant representing the type of this event. 
     * {@see javax.xml.stream.XMLStreamConstants}
     */
    private int fEventType;
    
    /**
     * Location object for this event.
     */
    private Location fLocation;

    /**
     * The QName of the schema type for this element.
     */
    private QName fSchemaType;

    /**
     * Constructor.
     */
    public XMLEventImpl(final int eventType, final Location location, final QName schemaType) {
        fEventType = eventType;
        fLocation = location;
        fSchemaType = schemaType;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#getEventType()
     */
    public int getEventType() {
        return fEventType;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#getLocation()
     */
    public Location getLocation() {
        return fLocation;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#isStartElement()
     */
    public boolean isStartElement() {
        return XMLStreamConstants.START_ELEMENT == fEventType;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#isAttribute()
     */
    public boolean isAttribute() {
        return XMLStreamConstants.ATTRIBUTE == fEventType;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#isNamespace()
     */
    public boolean isNamespace() {
        return XMLStreamConstants.NAMESPACE == fEventType;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#isEndElement()
     */
    public boolean isEndElement() {
        return XMLStreamConstants.END_ELEMENT == fEventType;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#isEntityReference()
     */
    public boolean isEntityReference() {
        return XMLStreamConstants.ENTITY_REFERENCE == fEventType;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#isProcessingInstruction()
     */
    public boolean isProcessingInstruction() {
        return XMLStreamConstants.PROCESSING_INSTRUCTION == fEventType;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#isCharacters()
     */
    public boolean isCharacters() {
        return XMLStreamConstants.CHARACTERS == fEventType;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#isStartDocument()
     */
    public boolean isStartDocument() {
        return XMLStreamConstants.START_DOCUMENT == fEventType;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#isEndDocument()
     */
    public boolean isEndDocument() {
        return XMLStreamConstants.END_DOCUMENT == fEventType;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#asStartElement()
     */
    public StartElement asStartElement() {
        return (StartElement) this;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#asEndElement()
     */
    public EndElement asEndElement() {
        return (EndElement) this;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#asCharacters()
     */
    public Characters asCharacters() {
        return (Characters) this;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#getSchemaType()
     */
    public QName getSchemaType() {
        return fSchemaType;
    }

    /**
     * @see javax.xml.stream.events.XMLEvent#writeAsEncodedUnicode(java.io.Writer)
     */
    public void writeAsEncodedUnicode(final Writer writer) throws XMLStreamException {
        // TODO Auto-generated method stub
    }
}
