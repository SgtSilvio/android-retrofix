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
        val newDescriptor = if (isStatic) descriptor else "(L" + owner + ";" + descriptor.substring(1)
        map[key] = Entry(owner, isStatic, newOwner, newName, newDescriptor, map[key])
    }

    fun forOwner(oldOwner: String, newOwner: String) = ForOwner(oldOwner, newOwner)

    class Entry(
        val owner: String,
        val isStatic: Boolean,
        val newOwner: String,
        val newName: String,
        val newDescriptor: String,
        val next: Entry?,
    )

    inner class ForOwner(private val oldOwner: String, private val newOwner: String) {

        fun redirect(name: String, descriptor: String): ForOwner {
            redirect(oldOwner, false, name, descriptor, newOwner, name)
            return this
        }

        fun redirectStatic(name: String, descriptor: String): ForOwner {
            redirect(oldOwner, true, name, descriptor, newOwner, name)
            return this
        }

        fun redirectStatic(method: String, descriptor: String, newName: String): ForOwner {
            redirect(oldOwner, true, method, descriptor, newOwner, newName)
            return this
        }
    }
}
