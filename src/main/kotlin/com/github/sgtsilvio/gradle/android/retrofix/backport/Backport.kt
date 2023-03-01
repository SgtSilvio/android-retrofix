package com.github.sgtsilvio.gradle.android.retrofix.backport

import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap
import javassist.ClassPool

/**
 * @author Silvio Giebl
 */
interface Backport {

    fun isPresent(classPool: ClassPool): Boolean

    fun apply(typeMap: TypeMap, methodMap: MethodMap)
}
