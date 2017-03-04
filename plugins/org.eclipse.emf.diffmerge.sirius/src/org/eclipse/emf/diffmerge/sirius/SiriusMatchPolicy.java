/**
 * <copyright>
 * 
 * Copyright (c) 2006-2017  Thales Global Services S.A.S.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thales Global Services S.A.S. - initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.emf.diffmerge.sirius;

import org.eclipse.emf.diffmerge.api.scopes.IModelScope;
import org.eclipse.emf.diffmerge.gmf.GMFMatchPolicy;
import org.eclipse.emf.diffmerge.util.structures.comparable.ComparableTreeMap;
import org.eclipse.emf.diffmerge.util.structures.comparable.IComparableStructure;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.diagram.DDiagram;
import org.eclipse.sirius.diagram.DDiagramElement;
import org.eclipse.sirius.diagram.DiagramPackage;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.sirius.viewpoint.DRepresentationDescriptor;
import org.eclipse.sirius.viewpoint.DView;
import org.eclipse.sirius.viewpoint.description.AnnotationEntry;
import org.eclipse.sirius.viewpoint.description.Viewpoint;


/**
 * A match policy for Sirius elements.
 */
public class SiriusMatchPolicy extends GMFMatchPolicy {
  
  /**
   * Return a semantic ID for the given diagram element
   * @param diagramElement_p a non-null diagram element
   * @param scope_p a non-null scope that covers element
   * @param inScopeOnly_p whether only the scope may be considered, or the underlying EMF model
   * @return a potentially null 
   */
  protected IComparableStructure<?> getDDiagramElementSemanticID(DDiagramElement diagramElement_p,
      IModelScope scope_p, boolean inScopeOnly_p) {
    // The semantic ID is defined from the diagram and the represented element,
    // the assumption being that an element cannot be represented more than once
    // in the same diagram.
    ComparableTreeMap<String, IComparableStructure<String>> result = null;
    DDiagram diagram = diagramElement_p.getParentDiagram();
    EObject represented = diagramElement_p.getTarget();
    if (diagram != null && represented != null) {
      IComparableStructure<String> typeID = getEncapsulateOrNull(diagramElement_p.eClass().getName());
      @SuppressWarnings("unchecked")
      IComparableStructure<String> diagramID =
      (IComparableStructure<String>)getMatchID(diagram, scope_p);
      if (diagramID != null) {
        @SuppressWarnings("unchecked")
        IComparableStructure<String> representedID =
        (IComparableStructure<String>)getMatchID(represented, scope_p);
        if (representedID != null) {
          result = new ComparableTreeMap<String, IComparableStructure<String>>();
          result.put("SEMANTIC_ID_TYPE", typeID); //$NON-NLS-1$
          result.put("SEMANTIC_ID_DIAGRAM", diagramID); //$NON-NLS-1$
          result.put("SEMANTIC_ID_ELEMENT", representedID); //$NON-NLS-1$
        }
      }
    }
    return result;
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.gmf.GMFMatchPolicy#getSemanticID(org.eclipse.emf.ecore.EObject, org.eclipse.emf.diffmerge.api.scopes.IModelScope, boolean)
   */
  @Override
  protected IComparableStructure<?> getSemanticID(EObject element_p, IModelScope scope_p,
      boolean inScopeOnly_p) {
    // Intended return types: ComparableLinkedList<String>,
    //  ComparableTreeMap<String, ComparableLinkedList<String>>
    IComparableStructure<?> result = null;
    if (element_p instanceof DDiagramElement)
      result = getDDiagramElementSemanticID((DDiagramElement)element_p, scope_p, inScopeOnly_p);
    if (result == null)
      result = super.getSemanticID(element_p, scope_p, inScopeOnly_p);
    return result;
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.gmf.GMFMatchPolicy#getUniqueName(org.eclipse.emf.ecore.EObject, org.eclipse.emf.diffmerge.api.scopes.IModelScope, boolean)
   */
  @Override
  protected String getUniqueName(EObject element_p, IModelScope scope_p, boolean inScopeOnly_p) {
    String result = null;
    if (element_p instanceof DView) {
      Viewpoint viewpoint = ((DView) element_p).getViewpoint();
      if (viewpoint != null)
        result = viewpoint.getName();
    } else if (element_p instanceof DRepresentationDescriptor) {
      result = ((DRepresentationDescriptor) element_p).getName();
    } else if (element_p instanceof DRepresentation) {
      result = ((DRepresentation) element_p).getName();
    } else if (element_p instanceof AnnotationEntry) {
      AnnotationEntry annotation = (AnnotationEntry)element_p;
      if (getContainer(element_p, scope_p, inScopeOnly_p) instanceof DDiagram &&
          annotation.getSource() != null) {
        // AnnotationEntry in a DDiagram
        result = "ANNOTATION_" + annotation.getSource(); //$NON-NLS-1$
      }
    }
    if (result == null)
      result = super.getUniqueName(element_p, scope_p, inScopeOnly_p);
    return result;
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.gmf.GMFMatchPolicy#isDiscriminatingContainment(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)
   */
  @Override
  protected boolean isDiscriminatingContainment(EObject element_p, EReference containment_p) {
    return super.isDiscriminatingContainment(element_p, containment_p) ||
        containment_p == DiagramPackage.eINSTANCE.getDDiagramElement_GraphicalFilters();
  }
  
}
