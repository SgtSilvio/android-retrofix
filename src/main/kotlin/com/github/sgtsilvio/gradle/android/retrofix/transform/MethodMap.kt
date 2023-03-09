package com.github.sgtsilvio.gradle.android.retrofix.transform

/**
 * @author Silvio Giebl
 */
class MethodMap : HashMap<String, MethodMap.Entry>() {

    fun forType(type: String) = ForType(type)

    class Entry(
        val owner: String,
        val isStatic: Boolean,
        val name: String,
        val descriptor: String,

        val newOwner: String,
        // no newIsStatic as always static
        val newName: String,
        // newDescriptor is computed

        val next: Entry?,
    ) {
        val newDescriptor get() = if (isStatic) descriptor else "(L" + owner + ";" + descriptor.substring(1)
    }

    inner class ForType(private val owner: String) {

        fun redirect(name: String, descriptor: String, newOwner: String) =
            redirect(false, name, descriptor, newOwner, name)

        fun redirectStatic(name: String, descriptor: String, newOwner: String) =
            redirect(true, name, descriptor, newOwner, name)

        fun redirectStatic(method: String, descriptor: String, newOwner: String, newName: String) =
            redirect(true, method, descriptor, newOwner, newName)

        private fun redirect(
            isStatic: Boolean,
            name: String,
            descriptor: String,
            newOwner: String,
            newName: String,
        ): ForType {
            val key = "$name$descriptor"
            put(key, Entry(owner, isStatic, name, descriptor, newOwner, newName, get(key)))
            return this
        }
    }
}
