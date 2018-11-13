/*
 * Copyright (C) 2018 Achrouf Abdenour <achroufabdenour@gmail.com>
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

package org.robovm.compiler.optimisation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.robovm.compiler.Types;
import org.robovm.compiler.clazz.Clazz;

import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.Modifier;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.queue.QueueReader;

public class DeadSymbolRemover {
	private List<SootMethod> entryPoints        ;
	private Set<SootField>   reachableFields    ;

	public DeadSymbolRemover() {
		entryPoints      = new ArrayList<SootMethod>();
		reachableFields  = new HashSet<SootField>();
	}

	private void findReachableFields(SootMethod src) {
		UnitGraph graph = new ExceptionalUnitGraph(src.getActiveBody());

		for (Unit unit : graph) {
			Stmt s = (Stmt) unit;
			if(s.containsFieldRef()) {
				reachableFields.add(s.getFieldRef().getField());						
			}
		} 
	}

	public void addCallGraphEntryPoints(Set<Clazz> rootClazzes) {
		for(Clazz clazz : rootClazzes) {
			for(SootMethod method : clazz.sootClass.getMethods()) {
				if(method.isConcrete())
					entryPoints.add(method); 			
			}

			for(SootField field : clazz.sootClass.getFields())
				reachableFields.add(field);
		}
		
		Scene.v().setEntryPoints(entryPoints);
	}

	public Set<SootField> getReachableFields(){
		return reachableFields;
	}

	public void removeUnreachableFields(Set<Clazz> clazzes) {
		Iterator<Clazz> clazzesIt = clazzes.iterator();

		while(clazzesIt.hasNext()){	
			Clazz clazz = clazzesIt.next();

			Iterator<SootField> fieldIt = clazz.sootClass.fieldIterator();

			while(fieldIt.hasNext()){
				SootField field = fieldIt.next();
				if(!reachableFields.contains(field))
					fieldIt.remove();		
			}
		}
	}

	private Set<Triple<String, String, String>> getCallGraphReachableMethods(){
		Set<Triple<String, String, String>> result = new HashSet<Triple<String, String, String>>();

		ReachableMethods rm = Scene.v().getReachableMethods();
		QueueReader<MethodOrMethodContext> methods = rm.listener();

		while(methods.hasNext()) {
			SootMethod method = methods.next().method();
			Triple<String, String, String> methodNode = new ImmutableTriple<String, String, String>(
					Types.getInternalName(method.getDeclaringClass()), method.getName() , Types.getDescriptor(method));
		
			result.add(methodNode);
		}
		return result;
	}

	public Set<Triple<String, String, String>> findReachableMethodes(Set<Triple<String, String, String>> methods){
		Set<Triple<String, String, String>> rm     =     getCallGraphReachableMethods();
		Set<Triple<String, String, String>> result =     new HashSet<Triple<String, String, String>>();
		
		for (Triple<String, String, String> method : methods) {
			String classInternalName = method.getLeft();
			String name              = method.getMiddle();
			String desc              = method.getRight();

			if(rm.contains(method) ||
					("<clinit>".equals(name) && "()V".equals(desc)) ||
					(name.equals("<init>")) ||
					("values".equals(name) && desc.equals("()[L" + classInternalName + ";"))) {
				result.add(method);
			}
		}
		return result;
	}
}
