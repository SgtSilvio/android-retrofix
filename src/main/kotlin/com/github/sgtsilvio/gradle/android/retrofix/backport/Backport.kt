package com.github.sgtsilvio.gradle.android.retrofix.backport

import com.github.sgtsilvio.gradle.android.retrofix.transform.ClassMap
import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap

/**
 * @author Silvio Giebl
 */
interface Backport {

    val indicatorClass: String

    fun isInstrumentable(className: String): Boolean

    fun apply(classMap: ClassMap, methodMap: MethodMap)
}
