package com.github.sgtsilvio.gradle.android.retrofix

import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap
import org.objectweb.asm.commons.Remapper

/**
 * @author Silvio Giebl
 */
class RetroFixRemapper(private val typeMap: TypeMap) : Remapper() {

    override fun map(typeName: String) = typeMap[typeName] ?: typeName
}