/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.psu.ist.vaccine.geotxt.utils;

import gate.Annotation;
import java.util.Vector;

/**
 *
 * @author ajaiswal
 */
 public class SortedAnnotationList extends Vector {
    public SortedAnnotationList() {
      super();
    } // SortedAnnotationList

    public boolean addSortedExclusive(Annotation annot) {
      Annotation currAnot = null;

      // overlapping check
      for (int i=0; i<size(); ++i) {
        currAnot = (Annotation) get(i);
        if(annot.overlaps(currAnot)) {
          return false;
        } // if
      } // for

      long annotStart = annot.getStartNode().getOffset().longValue();
      long currStart;
      // insert
      for (int i=0; i < size(); ++i) {
        currAnot = (Annotation) get(i);
        currStart = currAnot.getStartNode().getOffset().longValue();
        if(annotStart < currStart) {
          insertElementAt(annot, i);
          /*
           Out.prln("Insert start: "+annotStart+" at position: "+i+" size="+size());
           Out.prln("Current start: "+currStart);
           */
          return true;
        } // if
      } // for

      int size = size();
      insertElementAt(annot, size);
//Out.prln("Insert start: "+annotStart+" at size position: "+size);
      return true;
    } // addSorted
  } // SortedAnnotationList