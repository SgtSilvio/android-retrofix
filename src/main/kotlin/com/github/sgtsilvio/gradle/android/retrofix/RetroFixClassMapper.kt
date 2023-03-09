package com.github.sgtsilvio.gradle.android.retrofix

import com.github.sgtsilvio.gradle.android.retrofix.transform.ClassMap
import org.objectweb.asm.commons.Remapper

/**
 * @author Silvio Giebl
 */
class RetroFixClassMapper(private val classMap: ClassMap) : Remapper() {

    override fun map(typeName: String) = classMap[typeName] ?: typeName
}