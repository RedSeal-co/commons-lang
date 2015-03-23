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
package org.apache.commons.lang3.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.ClassUtils;

/**
 * Contains common code for working with {@link java.lang.reflect.Method Methods}/{@link java.lang.reflect.Constructor Constructors},
 * extracted and refactored from {@link MethodUtils} when it was imported from Commons BeanUtils.
 *
 * @since 2.5
 * @version $Id$
 */
abstract class MemberUtils {
    // TODO extract an interface to implement compareParameterSets(...)?

    private static final int ACCESS_TEST = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;

    /** Array of primitive number types ordered by "promotability" */
    private static final Class<?>[] ORDERED_PRIMITIVE_TYPES = { Byte.TYPE, Short.TYPE,
            Character.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE };

    /**
     * XXX Default access superclass workaround.
     *
     * When a {@code public} class has a default access superclass with {@code public} members,
     * these members are accessible. Calling them from compiled code works fine.
     * Unfortunately, on some JVMs, using reflection to invoke these members
     * seems to (wrongly) prevent access even when the modifier is {@code public}.
     * Calling {@code setAccessible(true)} solves the problem but will only work from
     * sufficiently privileged code. Better workarounds would be gratefully
     * accepted.
     * @param o the AccessibleObject to set as accessible
     * @return a boolean indicating whether the accessibility of the object was set to true.
     */
    static boolean setAccessibleWorkaround(final AccessibleObject o) {
        if (o == null || o.isAccessible()) {
            return false;
        }
        final Member m = (Member) o;
        if (!o.isAccessible() && Modifier.isPublic(m.getModifiers()) && isPackageAccess(m.getDeclaringClass().getModifiers())) {
            try {
                o.setAccessible(true);
                return true;
            } catch (final SecurityException e) { // NOPMD
                // ignore in favor of subsequent IllegalAccessException
            }
        }
        return false;
    }

    /**
     * Returns whether a given set of modifiers implies package access.
     * @param modifiers to test
     * @return {@code true} unless {@code package}/{@code protected}/{@code private} modifier detected
     */
    static boolean isPackageAccess(final int modifiers) {
        return (modifiers & ACCESS_TEST) == 0;
    }

    /**
     * Returns whether a {@link Member} is accessible.
     * @param m Member to check
     * @return {@code true} if <code>m</code> is accessible
     */
    static boolean isAccessible(final Member m) {
        return m != null && Modifier.isPublic(m.getModifiers()) && !m.isSynthetic();
    }

    /**
     * Compares the relative fitness of two sets of parameter types in terms of
     * matching a third set of runtime parameter types, such that a list ordered
     * by the results of the comparison would return the best match first
     * (least).
     *
     * @param left the "left" parameter set
     * @param right the "right" parameter set
     * @param actual the runtime parameter types to match against
     * {@code left}/{@code right}
     * @return int consistent with {@code compare} semantics
     */
    static int compareParameterTypes(final Class<?>[] left, final Class<?>[] right, final Class<?>[] actual, boolean leftIsVarArgs, boolean rightIsVarArgs) {
        final float leftCost = getTotalTransformationCost(actual, left, leftIsVarArgs);
        final float rightCost = getTotalTransformationCost(actual, right, rightIsVarArgs);
        return leftCost < rightCost ? -1 : rightCost < leftCost ? 1 : 0;
    }

    /**
     * Returns the sum of the object transformation cost for each class in the
     * source argument list.
     * @param srcArgs The source arguments
     * @param destArgs The destination arguments
     * @return The total transformation cost
     */
    private static float getTotalTransformationCost(final Class<?>[] srcArgs, final Class<?>[] destArgs, boolean isVarArgs) {
        // "source" and "destination" may seem ambiguous. If so, then think of them instead as
        // "actual arguments" and "declared arguments", i.e actual is arguments being presented to
        // a method, and "declared arguments" are the arguments of the method's declared signature.
        // So "source"=="actual" and "destination"=="declared".
        // When isVarArgs is false, both arrays should have the same length.
        // When isVarArgs is true, the lengths may differ, but there are different cases to consider:
        // src.length+1 == dest.length:
        //    This can happen if no arguments were passed for the varargs parameter.
        // src.length > dest.length:
        //    A typical use of varargs where more than one argument is passed for the varargs parameter.
        // src.length == dest.length && src is an array type whose base type matches the varargs array type
        //    This is a case where an explicit array is passed for the varargs parameter
        // src.length == dest.length && src is not an array type.
        //    This may be a case where a single argument is passed for the varargs parameter
        float totalCost = 0.0f;
        long normalArgsLen = isVarArgs ? destArgs.length-1 : destArgs.length;
        if (srcArgs.length < normalArgsLen)
            return Float.MAX_VALUE;   // this is actually logic error!
        for (int i = 0; i < normalArgsLen; i++) {
            totalCost += getObjectTransformationCost(srcArgs[i], destArgs[i]);
        }
        if (isVarArgs) {
            Class<?> destClass = destArgs[destArgs.length-1].getComponentType();
            if (destClass == null) {
                return Float.MAX_VALUE;   // this is actually logic error!
            }
            if (srcArgs.length > destArgs.length) {
                for (int i = destArgs.length-1; i < srcArgs.length; i++) {
                    Class<?> srcClass = srcArgs[i];
                    totalCost += getObjectTransformationCost(srcClass, destClass) + 0.01;
                }
            }
            else if (srcArgs.length==destArgs.length) {
                if (srcArgs[srcArgs.length-1].isArray()) {
                    Class<?> sourceClass = srcArgs[srcArgs.length-1].getComponentType();
                    totalCost += getObjectTransformationCost(sourceClass, destClass) + 0.01;
                }
                else {
                    totalCost += getObjectTransformationCost(srcArgs[srcArgs.length-1], destClass) + 0.01;
                }
            }
            else {
              // No source arguments were provided for the vararg parameter.
              // This means we want the most generic matching type, not the most specific.
              totalCost += getObjectTransformationCost(destClass, Object.class) + 0.01;
            }
        }
        return totalCost;
    }

    /**
     * Gets the number of steps required needed to turn the source class into
     * the destination class. This represents the number of steps in the object
     * hierarchy graph.
     * @param srcClass The source class
     * @param destClass The destination class
     * @return The cost of transforming an object
     */
    private static float getObjectTransformationCost(Class<?> srcClass, final Class<?> destClass) {
        if (destClass.isPrimitive()) {
            return getPrimitivePromotionCost(srcClass, destClass);
        }
        float cost = 0.0f;
        while (srcClass != null && !destClass.equals(srcClass)) {
            if (destClass.isInterface() && ClassUtils.isAssignable(srcClass, destClass)) {
                // slight penalty for interface match.
                // we still want an exact match to override an interface match,
                // but
                // an interface match should override anything where we have to
                // get a superclass.
                cost += 0.25f;
                break;
            }
            cost++;
            srcClass = srcClass.getSuperclass();
        }
        /*
         * If the destination class is null, we've travelled all the way up to
         * an Object match. We'll penalize this by adding 1.5 to the cost.
         */
        if (srcClass == null) {
            cost += 1.5f;
        }
        return cost;
    }

    /**
     * Gets the number of steps required to promote a primitive number to another
     * type.
     * @param srcClass the (primitive) source class
     * @param destClass the (primitive) destination class
     * @return The cost of promoting the primitive
     */
    private static float getPrimitivePromotionCost(final Class<?> srcClass, final Class<?> destClass) {
        float cost = 0.0f;
        Class<?> cls = srcClass;
        if (!cls.isPrimitive()) {
            // slight unwrapping penalty
            cost += 0.1f;
            cls = ClassUtils.wrapperToPrimitive(cls);
        }
        for (int i = 0; cls != destClass && i < ORDERED_PRIMITIVE_TYPES.length; i++) {
            if (cls == ORDERED_PRIMITIVE_TYPES[i]) {
                cost += 0.1f;
                if (i < ORDERED_PRIMITIVE_TYPES.length - 1) {
                    cls = ORDERED_PRIMITIVE_TYPES[i + 1];
                }
            }
        }
        return cost;
    }

}
