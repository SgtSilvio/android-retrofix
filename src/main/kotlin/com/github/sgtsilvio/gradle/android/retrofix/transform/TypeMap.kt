package com.github.sgtsilvio.gradle.android.retrofix.transform

import javassist.ClassMap

/**
 * @author Silvio Giebl
 */
class TypeMap : ClassMap() {

    private val prefixMap = HashMap<String, String>()

    override fun get(key: String): String? {
        for (entry in prefixMap) {
            if (key.startsWith(entry.key)) {
                return entry.value + key.substring(entry.key.length)
            }
        }
        return super.get(key)
    }

    fun putPrefix(oldPrefix: String, newPrefix: String) = prefixMap.put(oldPrefix, newPrefix)
}
