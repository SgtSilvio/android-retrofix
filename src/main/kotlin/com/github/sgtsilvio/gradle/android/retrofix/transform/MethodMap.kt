package com.github.sgtsilvio.gradle.android.retrofix.transform

/**
 * @author Silvio Giebl
 */
class MethodMap {

    private val map = HashMap<String, Entry>()

    fun get(name: String, descriptor: String) = map["$name$descriptor"]

    private fun redirect(
        owner: String,
        isStatic: Boolean,
        name: String,
        descriptor: String,
        newOwner: String,
        newName: String,
    ) {
        val key = "$name$descriptor"
        map[key] = Entry(owner, isStatic, name, descriptor, newOwner, newName, map[key])
    }

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

        fun redirect(name: String, descriptor: String, newOwner: String): ForType {
            redirect(owner, false, name, descriptor, newOwner, name)
            return this
        }

        fun redirectStatic(name: String, descriptor: String, newOwner: String): ForType {
            redirect(owner, true, name, descriptor, newOwner, name)
            return this
        }

        fun redirectStatic(method: String, descriptor: String, newOwner: String, newName: String): ForType {
            redirect(owner, true, method, descriptor, newOwner, newName)
            return this
        }
    }
}
