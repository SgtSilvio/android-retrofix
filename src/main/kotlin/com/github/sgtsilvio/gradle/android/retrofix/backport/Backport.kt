package com.github.sgtsilvio.gradle.android.retrofix.backport

import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap
import javassist.ClassPool

/**
 * @author Silvio Giebl
 */
interface Backport {

    val indicatorClass: String

    fun isInstrumentable(className: String): Boolean

    fun isPresent(classPool: ClassPool) = classPool.find(indicatorClass) != null

    fun apply(typeMap: TypeMap, methodMap: MethodMap)
}
