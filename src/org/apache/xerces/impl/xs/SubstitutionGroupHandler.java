/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.xs;

import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xni.QName;
import java.util.Hashtable;
import java.util.Vector;

/**
 * To store and validate information about substitutionGroup
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class SubstitutionGroupHandler {

    // grammar resolver
    XSGrammarBucket fGrammarBucket;

    /**
     * Default constructor
     */
    public SubstitutionGroupHandler(XSGrammarBucket grammarBucket) {
        fGrammarBucket = grammarBucket;
    }

    // 3.9.4 Element Sequence Locally Valid (Particle) 2.3.3
    // check whether one element decl matches an element with the given qname
    public XSElementDecl getMatchingElemDecl(QName element, XSElementDecl exemplar) {
        if (element.localpart == exemplar.fName &&
            element.uri == exemplar.fTargetNamespace) {
            return exemplar;
        }

        // if the exemplar is not a global element decl, then it's not possible
        // to be substituted by another element.
        if (exemplar.fScope != XSConstants.SCOPE_GLOBAL)
            return null;

        // if the decl blocks substitution, return false
        if ((exemplar.fBlock & XSConstants.DERIVATION_SUBSTITUTION) != 0)
            return null;

        // get grammar of the element
        SchemaGrammar sGrammar = fGrammarBucket.getGrammar(element.uri);
        if (sGrammar == null)
            return null;

        // get the decl for the element
        XSElementDecl eDecl = sGrammar.getGlobalElementDecl(element.localpart);
        if (eDecl == null)
            return null;

        // and check by using substitutionGroup information
        if (substitutionGroupOK(eDecl, exemplar, exemplar.fBlock))
            return eDecl;

        return null;
    }

    // 3.3.6 Substitution Group OK (Transitive)
    // check whether element can substitute exemplar
    protected boolean substitutionGroupOK(XSElementDecl element, XSElementDecl exemplar, short blockingConstraint) {
        // For an element declaration (call it D) to be validly substitutable for another element declaration (call it C) subject to a blocking constraint (a subset of {substitution, extension, restriction}, the value of a {disallowed substitutions}) one of the following must be true:
        // 1. D and C are the same element declaration.
        if (element == exemplar)
            return true;
        
        // 2 All of the following must be true:
        // 2.1 The blocking constraint does not contain substitution.
        if ((blockingConstraint & XSConstants.DERIVATION_SUBSTITUTION) != 0)
            return false;

        // 2.2 There is a chain of {substitution group affiliation}s from D to C, that is, either D's {substitution group affiliation} is C, or D's {substitution group affiliation}'s {substitution group affiliation} is C, or . . .
        XSElementDecl subGroup = element.fSubGroup;
        while (subGroup != null && subGroup != exemplar) {
            subGroup = subGroup.fSubGroup;
        }

        if (subGroup == null)
            return false;

        // 2.3 The set of all {derivation method}s involved in the derivation of D's {type definition} from C's {type definition} does not intersect with the union of the blocking constraint, C's {prohibited substitutions} (if C is complex, otherwise the empty set) and the {prohibited substitutions} (respectively the empty set) of any intermediate {type definition}s in the derivation of D's {type definition} from C's {type definition}.
        // prepare the combination of {derivation method} and
        // {disallowed substitution}
        short devMethod = 0, blockConstraint = blockingConstraint;

        // element.fType should be derived from exemplar.fType
        // add derivation methods of derived types to devMethod;
        // add block of base types to blockConstraint.
        XSTypeDefinition type = element.fType;
        while (type != exemplar.fType && type != SchemaGrammar.fAnyType) {
            if (type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE)
                devMethod |= ((XSComplexTypeDecl)type).fDerivedBy;
            else
                devMethod |= XSConstants.DERIVATION_RESTRICTION;
            type = type.getBaseType();
            // type == null means the current type is anySimpleType,
            // whose base type should be anyType
            if (type == null)
                type = SchemaGrammar.fAnyType;
            if (type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE)
                blockConstraint |= ((XSComplexTypeDecl)type).fBlock;
        }
        if (type != exemplar.fType)
            return false;
        
        if ((devMethod & blockConstraint) != 0)
            return false;

        return true;
    }

    // check whether element is in exemplar's substitution group
    public boolean inSubstitutionGroup(XSElementDecl element, XSElementDecl exemplar) {
        // [Definition:]  Every element declaration (call this HEAD) in the {element declarations} of a schema defines a substitution group, a subset of those {element declarations}, as follows:
        // Define PSG, the potential substitution group for HEAD, as follows:
        // 1 The element declaration itself is in PSG;
        // 2 PSG is closed with respect to {substitution group affiliation}, that is, if any element declaration in the {element declarations} has a {substitution group affiliation} in PSG, then it is also in PSG itself.
        // HEAD's actual substitution group is then the set consisting of each member of PSG such that all of the following must be true:
        // 1 Its {abstract} is false.
        // 2 It is validly substitutable for HEAD subject to an empty blocking constraint, as defined in Substitution Group OK (Transitive) (3.3.6).
        return substitutionGroupOK(element, exemplar, exemplar.fBlock);
    }

    // to store substitution group information
    // the key to the hashtable is an element decl, and the value is
    // - a Vector, which contains all elements that has this element as their
    //   substitution group affilication
    // - an array of OneSubGroup, which contains its substitution group before block.
    Hashtable fSubGroupsB = new Hashtable();
    private static final OneSubGroup[] EMPTY_VECTOR = new OneSubGroup[0];
    // The real substitution groups (after "block")
    Hashtable fSubGroups = new Hashtable();

    /**
     * clear the internal registry of substitutionGroup information
     */
    public void reset() {
        fSubGroupsB.clear();
        fSubGroups.clear();
    }

    /**
     * add a list of substitution group information.
     */
    public void addSubstitutionGroup(XSElementDecl[] elements) {
        XSElementDecl subHead, element;
        Vector subGroup;
        // for all elements with substitution group affiliation
        for (int i = elements.length-1; i >= 0; i--) {
            element = elements[i];
            subHead = element.fSubGroup;
            // check whether this an entry for this element
            subGroup = (Vector)fSubGroupsB.get(subHead);
            if (subGroup == null) {
                // if not, create a new one
                subGroup = new Vector();
                fSubGroupsB.put(subHead, subGroup);
            }
            // add to the vactor
            subGroup.addElement(element);
        }
    }

    /**
     * get all elements that can substitute the given element,
     * according to the spec, we shouldn't consider the {block} constraints.
     *
     * from the spec, substitution group of a given element decl also contains
     * the element itself. but the array returned from this method doesn't
     * containt this element.
     */
    public XSElementDecl[] getSubstitutionGroup(XSElementDecl element) {
        // If we already have sub group for this element, just return it.
        Object subGroup = fSubGroups.get(element);
        if (subGroup != null)
            return (XSElementDecl[])subGroup;
        
        // Otherwise, get all potential sub group elements
        // (without considering "block" on this element
        OneSubGroup[] groupB = getSubGroupB(element, new OneSubGroup());
        int len = groupB.length, rlen = 0;
        XSElementDecl[] ret = new XSElementDecl[len];
        // For each of such elements, check whether the derivation methods
        // overlap with "block". If not, add it to the sub group
        for (int i = 0 ; i < len; i++) {
            if ((element.fBlock & groupB[i].dMethod) == 0)
                ret[rlen++] = groupB[i].sub;
        }
        // Resize the array if necessary
        if (rlen < len) {
            XSElementDecl[] ret1 = new XSElementDecl[rlen];
            System.arraycopy(ret, 0, ret1, 0, rlen);
            ret = ret1;
        }
        // Store the subgroup
        fSubGroups.put(element, ret);

        return ret;
    }

    // Get potential sub group element (without considering "block")
    private OneSubGroup[] getSubGroupB(XSElementDecl element, OneSubGroup methods) {
        Object subGroup = fSubGroupsB.get(element);

        // substitution group for this one is empty
        if (subGroup == null) {
            fSubGroupsB.put(element, EMPTY_VECTOR);
            return EMPTY_VECTOR;
        }
        
        // we've already calculated the element, just return.
        if (subGroup instanceof OneSubGroup[])
            return (OneSubGroup[])subGroup;
        
        // we only have the *direct* substitutions
        Vector group = (Vector)subGroup, newGroup = new Vector();
        OneSubGroup[] group1;
        // then for each of the direct substitutions, get its substitution
        // group, and combine the groups together.
        short dMethod, bMethod, dSubMethod, bSubMethod;
        for (int i = group.size()-1, j; i >= 0; i--) {
            // Check whether this element is blocked. If so, ignore it.
            XSElementDecl sub = (XSElementDecl)group.elementAt(i);
            if (!getDBMethods(sub.fType, element.fType, methods))
                continue;
            // Remember derivation methods and blocks from the types
            dMethod = methods.dMethod;
            bMethod = methods.bMethod;
            // Add this one to potential group
            newGroup.addElement(new OneSubGroup(sub, methods.dMethod, methods.bMethod));
            // Get potential group for this element
            group1 = getSubGroupB(sub, methods);
            for (j = group1.length-1; j >= 0; j--) {
                // For each of them, check whether it's blocked (by type)
                dSubMethod = (short)(dMethod | group1[j].dMethod);
                bSubMethod = (short)(bMethod | group1[j].bMethod);
                // Ignore it if it's blocked
                if ((dSubMethod & bSubMethod) != 0)
                    continue;
                newGroup.addElement(new OneSubGroup(group1[j].sub, dSubMethod, bSubMethod));
            }
        }
        // Convert to an array
        OneSubGroup[] ret = new OneSubGroup[newGroup.size()];
        for (int i = newGroup.size()-1; i >= 0; i--) {
            ret[i] = (OneSubGroup)newGroup.elementAt(i);
        }
        // Store the potential sub group
        fSubGroupsB.put(element, ret);
        
        return ret;
    }

    private boolean getDBMethods(XSTypeDefinition typed, XSTypeDefinition typeb,
                                 OneSubGroup methods) {
        short dMethod = 0, bMethod = 0;
        while (typed != typeb && typed != SchemaGrammar.fAnyType) {
            if (typed.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE)
                dMethod |= ((XSComplexTypeDecl)typed).fDerivedBy;
            else
                dMethod |= XSConstants.DERIVATION_RESTRICTION;
            typed = typed.getBaseType();
            // type == null means the current type is anySimpleType,
            // whose base type should be anyType
            if (typed == null)
                typed = SchemaGrammar.fAnyType;
            if (typed.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE)
                bMethod |= ((XSComplexTypeDecl)typed).fBlock;
        }
        // No derivation relation, or blocked, return false
        if (typed != typeb || (dMethod & bMethod) != 0)
            return false;
        
        // Remember the derivation methods and blocks, return true.
        methods.dMethod = dMethod;
        methods.bMethod = bMethod;
        return true;
    }

    // Record the information about how one element substitute another one
    private static final class OneSubGroup {
        OneSubGroup() {}
        OneSubGroup(XSElementDecl sub, short dMethod, short bMethod) {
            this.sub = sub;
            this.dMethod = dMethod;
            this.bMethod = bMethod;
        }
        // The element that substitutes another one
        XSElementDecl sub;
        // The combination of all derivation methods from sub's type to
        // the head's type
        short dMethod;
        // The combination of {block} of the types in the derivation chain
        // excluding sub's type
        short bMethod;
    }
} // class SubstitutionGroupHandler
