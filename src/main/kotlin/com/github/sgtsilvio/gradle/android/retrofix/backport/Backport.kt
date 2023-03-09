package com.github.sgtsilvio.gradle.android.retrofix.backport

import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap

/**
 * @author Silvio Giebl
 */
interface Backport {

    val indicatorClass: String

    fun isInstrumentable(className: String): Boolean

    fun apply(typeMap: TypeMap, methodMap: MethodMap)
}
