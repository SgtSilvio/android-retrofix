package com.github.sgtsilvio.gradle.android.retrofix.transform

/**
 * @author Silvio Giebl
 */
class ClassMap {

    private val prefixMap = HashMap<String, String>()
    private val map = HashMap<String, String>()

    operator fun get(className: String): String? {
        for ((oldPrefix, newPrefix) in prefixMap) {
            if (className.startsWith(oldPrefix)) {
                return newPrefix + className.substring(oldPrefix.length)
            }
        }
        return map[className]
    }

    operator fun set(oldClassName: String, newClassName: String) {
        map[oldClassName] = newClassName
    }

    fun putPrefix(oldPrefix: String, newPrefix: String) {
        prefixMap[oldPrefix] = newPrefix
    }
}
