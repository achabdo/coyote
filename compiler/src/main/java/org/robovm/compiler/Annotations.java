/*
 * Copyright (C) 2012 RoboVM AB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-2.0.html>.
 */
package org.robovm.compiler;

import static soot.tagkit.AnnotationConstants.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.SootClass;
import soot.SootMethod;
import soot.SootResolver;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationIntElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Host;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.tagkit.VisibilityParameterAnnotationTag;

/**
 * @author niklas
 *
 */
public class Annotations {
    
    public enum Visibility { RuntimeVisible, RuntimeInvisible, SourceVisible, Any }
    
    public static final String WEAKLY_LINKED = "Lorg/robovm/rt/annotation/WeaklyLinked;";
    public static final String STRONGLY_LINKED = "Lorg/robovm/rt/annotation/StronglyLinked;";

    public static boolean hasAnnotation(Host host, String annotationType) {
        return getAnnotation(host, annotationType) != null;
    }
    
    public static boolean hasParameterAnnotation(SootMethod method, int paramIndex, String annotationType) {
        return getParameterAnnotation(method, paramIndex, annotationType) != null;
    }

    public static List<AnnotationTag> getAnnotations(Host host, Visibility visibility) {
        if (host instanceof SootClass) {
            SootResolver.v().bringToHierarchy((SootClass) host);
        }
        List<AnnotationTag> result = new ArrayList<>();
        for (Tag tag : host.getTags()) {
            if (tag instanceof VisibilityAnnotationTag) {
                if (visibility == Visibility.Any
                        || ((VisibilityAnnotationTag) tag).getVisibility() == visibility.ordinal()) {
                    result.addAll(((VisibilityAnnotationTag) tag).getAnnotations());
                }
            }
        }
        return result;
    }
    
    public static AnnotationTag getAnnotation(Host host, String annotationType) {
        for (AnnotationTag tag : getAnnotations(host, Visibility.Any)) {
            if (annotationType.equals(tag.getType())) {
                return tag;
            }                    
        }
        return null;
    }
    
    public static List<AnnotationTag> getParameterAnnotations(SootMethod method, int paramIndex, Visibility visibility) {
        List<AnnotationTag> result = new ArrayList<>();
        for (Tag tag : method.getTags()) {
            if (tag instanceof VisibilityParameterAnnotationTag) { 
                if (visibility == Visibility.Any 
                        || ((VisibilityParameterAnnotationTag) tag).getKind() == visibility.ordinal()) {
                    
                    ArrayList<VisibilityAnnotationTag> l = 
                            ((VisibilityParameterAnnotationTag) tag).getVisibilityAnnotations();
                    if (l != null && paramIndex < l.size()) {
                        ArrayList<AnnotationTag> annotations = l.get(paramIndex).getAnnotations();
                        if (annotations != null) {
                            result.addAll(annotations);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<AnnotationTag>[] getParameterAnnotations(SootMethod method, Visibility visibility) {
        @SuppressWarnings("unchecked")
        ArrayList<AnnotationTag>[] result = new ArrayList[method.getParameterCount()];
        for (int i = 0; i < result.length; i++) {
            result[i] = new ArrayList<>();
        }
        for (Tag tag : method.getTags()) {
            if (tag instanceof VisibilityParameterAnnotationTag) {
                if (visibility == Visibility.Any 
                        || ((VisibilityParameterAnnotationTag) tag).getKind() == visibility.ordinal()) {
                    
                    ArrayList<VisibilityAnnotationTag> l = 
                            ((VisibilityParameterAnnotationTag) tag).getVisibilityAnnotations();
                    if (l != null) {
                        int i = 0;
                        for (VisibilityAnnotationTag t : l) {
                            ArrayList<AnnotationTag> annotations = t.getAnnotations();
                            if (annotations != null) {
                                result[i].addAll(annotations);
                            }
                            i++;
                        }
                    }
                }
            }
        }
        return result;
    }
    
    public static AnnotationTag getParameterAnnotation(SootMethod method, int paramIndex, String annotationType) {
        for (AnnotationTag tag : getParameterAnnotations(method, paramIndex, Visibility.Any)) {
            if (annotationType.equals(tag.getType())) {
                return tag;
            }                    
        }
        return null;
    }

    public static boolean hasWeaklyLinkedAnnotation(Host host) {
        return hasAnnotation(host, WEAKLY_LINKED);
    }

    public static boolean hasStronglyLinkedAnnotation(Host host) {
        return hasAnnotation(host, STRONGLY_LINKED);
    }
    
    public static AnnotationElem getElemByName(AnnotationTag annotation, String name) {
        for (int i = 0; i < annotation.getNumElems(); i++) {
            AnnotationElem elem = annotation.getElemAt(i);
            if (name.equals(elem.getName())) {
                return elem;
            }
        }
        return null;
    }
    
    public static String readStringElem(AnnotationTag annotation, String name, String def) {
        AnnotationStringElem elem = (AnnotationStringElem) getElemByName(annotation, name);
        return elem != null ? elem.getValue() : def;
    }

    public static boolean readBooleanElem(AnnotationTag annotation, String name, boolean def) {
        AnnotationIntElem elem = (AnnotationIntElem) getElemByName(annotation, name);
        return elem != null ? elem.getValue() == 1 : def;
    }

    public static int readIntElem(AnnotationTag annotation, String name, int def) {
        AnnotationIntElem elem = (AnnotationIntElem) getElemByName(annotation, name);
        return elem != null ? elem.getValue() : def;
    }

    public static VisibilityAnnotationTag getOrCreateRuntimeVisibilityAnnotationTag(Host host) {
        for (Tag tag : host.getTags()) {
            if (tag instanceof VisibilityAnnotationTag) {
                if (((VisibilityAnnotationTag) tag).getVisibility() == RUNTIME_VISIBLE) {
                    return (VisibilityAnnotationTag) tag;
                }
            }
        }
        VisibilityAnnotationTag tag = new VisibilityAnnotationTag(RUNTIME_VISIBLE);
        host.addTag(tag);
        return tag;
    }
    
    public static void addRuntimeVisibleAnnotation(Host host, String annotationType) {
        if (!hasAnnotation(host, annotationType)) {
            VisibilityAnnotationTag tag = getOrCreateRuntimeVisibilityAnnotationTag(host);
            tag.addAnnotation(new AnnotationTag(annotationType, 0));
        }
    }

    public static void addRuntimeVisibleAnnotation(Host host, AnnotationTag annoTag) {
        removeAnnotation(host, annoTag.getType());
        VisibilityAnnotationTag tag = getOrCreateRuntimeVisibilityAnnotationTag(host);
        tag.addAnnotation(annoTag);
    }

    public static void removeAnnotation(Host host, String annotationType) {
        for (Tag tag : host.getTags()) {
            if (tag instanceof VisibilityAnnotationTag) {
                ArrayList<AnnotationTag> l = ((VisibilityAnnotationTag) tag).getAnnotations();
                for (Iterator<AnnotationTag> it = l.iterator(); it.hasNext();) {
                    AnnotationTag annoTag = it.next();
                    if (annoTag.getType().equals(annotationType)) {
                        it.remove();
                    }
                }
            }
        }
    }

    public static void removeParameterAnnotation(SootMethod method, int paramIndex, String annotationType) {
        for (Tag tag : method.getTags()) {
            if (tag instanceof VisibilityParameterAnnotationTag) {
                ArrayList<VisibilityAnnotationTag> l = 
                        ((VisibilityParameterAnnotationTag) tag).getVisibilityAnnotations();
                if (l != null && paramIndex < l.size()) {
                    ArrayList<AnnotationTag> annotations = l.get(paramIndex).getAnnotations();
                    if (annotations != null) {
                        for (Iterator<AnnotationTag> it = annotations.iterator(); it.hasNext();) {
                            AnnotationTag annoTag = it.next();
                            if (annoTag.getType().equals(annotationType)) {
                                it.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    private static void copyAnnotations(SootMethod fromMethod, SootMethod toMethod, int visibility) {
        for (Tag tag : fromMethod.getTags()) {
            if (tag instanceof VisibilityAnnotationTag) {
                if (((VisibilityAnnotationTag) tag).getVisibility() == visibility) {
                    VisibilityAnnotationTag copy = new VisibilityAnnotationTag(visibility);
                    for (AnnotationTag annoTag : ((VisibilityAnnotationTag) tag).getAnnotations()) {
                        copy.addAnnotation(annoTag);
                    }
                    toMethod.addTag(copy);
                }
            }
        }
    }
    
    public static void copyAnnotations(SootMethod fromMethod, SootMethod toMethod, Visibility visibility) {
        if (visibility == Visibility.Any) {
            copyAnnotations(fromMethod, toMethod, RUNTIME_VISIBLE);
            copyAnnotations(fromMethod, toMethod, RUNTIME_INVISIBLE);
            copyAnnotations(fromMethod, toMethod, SOURCE_VISIBLE);
        } else {
            copyAnnotations(fromMethod, toMethod, visibility.ordinal());
        }
    }
    
    private static void copyParameterAnnotations(SootMethod fromMethod, SootMethod toMethod, int start, int end, int shift, int visibility) {
        List<AnnotationTag>[] fromAnnos = getParameterAnnotations(fromMethod, Visibility.values()[visibility]);
        List<AnnotationTag>[] toAnnos = getParameterAnnotations(toMethod, Visibility.values()[visibility]);

        for (int i = start; i < end; i++) {
            toAnnos[i + shift].addAll(fromAnnos[i]);
        }
        
        for (Iterator<Tag> it = toMethod.getTags().iterator(); it.hasNext();) {
            Tag tag = it.next();
            if (tag instanceof VisibilityParameterAnnotationTag) {
                if (((VisibilityParameterAnnotationTag) tag).getKind() == visibility) {
                    it.remove();
                }
            }
        }
        
        VisibilityParameterAnnotationTag vpaTag = new VisibilityParameterAnnotationTag(toAnnos.length, visibility);
        for (List<AnnotationTag> annos : toAnnos) {
            VisibilityAnnotationTag vaTag = new VisibilityAnnotationTag(visibility);
            for (AnnotationTag anno : annos) {
                vaTag.addAnnotation(anno);
            }
            vpaTag.addVisibilityAnnotation(vaTag);
        }
        toMethod.addTag(vpaTag);
    }
    
    public static void copyParameterAnnotations(SootMethod fromMethod, SootMethod toMethod, int start, int end, int shift, Visibility visibility) {
        if (visibility == Visibility.Any) {
            copyParameterAnnotations(fromMethod, toMethod, start, end, shift, RUNTIME_VISIBLE);
            copyParameterAnnotations(fromMethod, toMethod, start, end, shift, RUNTIME_INVISIBLE);
            copyParameterAnnotations(fromMethod, toMethod, start, end, shift, SOURCE_VISIBLE);
        } else {
            copyParameterAnnotations(fromMethod, toMethod, start, end, shift, visibility.ordinal());
        }
    }
    
    public static VisibilityAnnotationTag getOrCreateRuntimeVisibilityAnnotationTag(SootMethod method, int paramIndex) {
        for (Tag tag : method.getTags()) {
            if (tag instanceof VisibilityParameterAnnotationTag) {
                ArrayList<VisibilityAnnotationTag> l = 
                        ((VisibilityParameterAnnotationTag) tag).getVisibilityAnnotations();
                if (l != null && paramIndex < l.size()) {
                    if ((l.get(paramIndex)).getVisibility() == RUNTIME_VISIBLE) {
                        return l.get(paramIndex);
                    }
                }
            }
        }
        VisibilityParameterAnnotationTag ptag = 
                new VisibilityParameterAnnotationTag(method.getParameterCount(), RUNTIME_VISIBLE);
        ArrayList<VisibilityAnnotationTag> l = new ArrayList<VisibilityAnnotationTag>();
        for (int i = 0; i < method.getParameterCount(); i++) {
            l.add(new VisibilityAnnotationTag(RUNTIME_VISIBLE));
        }
        method.addTag(ptag);
        return l.get(paramIndex);
    }
    
    public static void addRuntimeVisibleParameterAnnotation(SootMethod method, int paramIndex, String annotationType) {
        if (!hasParameterAnnotation(method, paramIndex, annotationType)) {
            VisibilityAnnotationTag tag = getOrCreateRuntimeVisibilityAnnotationTag(method, paramIndex);
            tag.addAnnotation(new AnnotationTag(annotationType, 0));
        }
    }

    public static void addRuntimeVisibleParameterAnnotation(SootMethod method, int paramIndex, AnnotationTag annoTag) {
        removeParameterAnnotation(method, paramIndex, annoTag.getType());
        VisibilityAnnotationTag tag = getOrCreateRuntimeVisibilityAnnotationTag(method, paramIndex);
        tag.addAnnotation(annoTag);
    }

}