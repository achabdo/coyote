/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 * Copyright (C) 2018 Achrouf Abdenour <achroufabdenour@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package org.robovm.compiler.optimisation;

import soot.*;

/** Methods for checking Java scope and visibiliity requirements. */
public class AccessManager
{
	public static boolean isAccessLegal(SootMethod container, ClassMember target)
	{
		SootClass targetClass = target.getDeclaringClass();
		SootClass containerClass = container.getDeclaringClass();

		if (!isAccessLegal(container, targetClass))
			return false;

		// Condition 1 above.
		if (target.isPrivate() && 
				!targetClass.getName().equals(containerClass.getName()))
			return false;

		// Condition 2. Check the package names.
		if (!target.isPrivate() && !target.isProtected() 
				&& !target.isPublic())
		{
			if (!targetClass.getPackageName().equals
					(containerClass.getPackageName()))
				return false;
		}

		// Condition 3.  
		if (target.isProtected())
		{
			Hierarchy h = Scene.v().getActiveHierarchy();

			// protected means that you can be accessed by your children.
			// i.e. container must be in a child of target.
			if (h.isClassSuperclassOfIncluding(targetClass, containerClass))
				return true;

			return false;
		}

		return true;        
	}

	/** Returns true if an access to <code>target</code> is legal from code in <code>container</code>. */
	public static boolean isAccessLegal(SootMethod container, SootClass target)
	{
		return target.isPublic() || 
				container.getDeclaringClass().getPackageName().equals(target.getPackageName());
	}


	/**
	 * Turns a field access or method call into a call to an accessor method.
	 * Reuses existing accessors based on name mangling (see createAccessorName) 
	 * @param container
	 * @param stmt
	 */

	public static boolean ensureAccess(SootMethod container, ClassMember target, String options)
	{
		boolean accessors=options.equals("accessors");
		boolean allowChanges = !(options.equals("none"));
		boolean safeChangesOnly = !(options.equals("unsafe"));

		SootClass targetClass = target.getDeclaringClass();
		if (!ensureAccess(container, targetClass, options))
			return false;

		if (isAccessLegal(container, target))
			return true;

		if (!allowChanges && !accessors)
			return false;

		//throw new RuntimeException("Not implemented yet!");

		if (target.getDeclaringClass().isApplicationClass())
		{
			if (accessors)
				return true;

			if (safeChangesOnly)
				throw new RuntimeException("Not implemented yet!");

			if(target.isPrivate())
				target.setModifiers((target.getModifiers() | Modifier.PUBLIC) &  ~Modifier.PRIVATE);
			else if(target.isProtected())
				target.setModifiers((target.getModifiers() | Modifier.PUBLIC) &  ~Modifier.PROTECTED);
			else
				target.setModifiers(target.getModifiers() | Modifier.PUBLIC);

			return true;
		}
		else
			return false;
	}

	/** Modifies code so that an access to <code>target</code> is legal from code in <code>container</code>. */
	public static boolean ensureAccess(SootMethod container, SootClass target, String options)
	{
		boolean accessors=options.equals("accessors");
		boolean allowChanges = !(options.equals("none"));
		boolean safeChangesOnly = !(options.equals("unsafe"));

		if (isAccessLegal(container, target))
			return true;

		if (!allowChanges && !accessors)
			return false;

		if (safeChangesOnly && !accessors)
			throw new RuntimeException("Not implemented yet!");

		if (accessors)
			return false;

		if (target.isApplicationClass())
		{
			if(target.isPrivate())
				target.setModifiers((target.getModifiers() | Modifier.PUBLIC) &  ~Modifier.PRIVATE);
			else if(target.isProtected())
				target.setModifiers((target.getModifiers() | Modifier.PUBLIC) &  ~Modifier.PROTECTED);
			else
				target.setModifiers(target.getModifiers() | Modifier.PUBLIC);

			return true;
		}
		else
			return false;
	}
}
