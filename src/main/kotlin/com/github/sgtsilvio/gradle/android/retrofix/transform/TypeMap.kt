package com.github.sgtsilvio.gradle.android.retrofix.transform

/**
 * @author Silvio Giebl
 */
class TypeMap : HashMap<String, String>() {

    private val prefixMap = HashMap<String, String>()

    override fun get(key: String): String? {
        for ((oldPrefix, newPrefix) in prefixMap) {
            if (key.startsWith(oldPrefix)) {
                return newPrefix + key.substring(oldPrefix.length)
            }
        }
        return super.get(key)
    }

    fun putPrefix(oldPrefix: String, newPrefix: String) = prefixMap.put(oldPrefix, newPrefix)
}
