/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.xerces.dom;

import org.w3c.dom.DOMImplementation;

/**
 * The DOMImplementation class is description of a particular
 * implementation of the Document Object Model. As such its data is
 * static, shared by all instances of this implementation.
 * <P>
 * This implementation simply extends DOMImplementationImpl to differentiate
 * between the Deferred DOM Implementations and Non-Deferred DOM Implementations.
 *
 * @version 
 * @since  
 */
public class DeferredDOMImplementationImpl extends DOMImplementationImpl {
	//
	// Data
	//

	// static

	/** Dom implementation singleton. */
	static DeferredDOMImplementationImpl singleton = new DeferredDOMImplementationImpl();


	//
	// Public methods
	//

	/** NON-DOM: Obtain and return the single shared object */
	public static DOMImplementation getDOMImplementation() {
		return singleton;
	}
}
