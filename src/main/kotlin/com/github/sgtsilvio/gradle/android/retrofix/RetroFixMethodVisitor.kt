package com.github.sgtsilvio.gradle.android.retrofix

import com.android.build.api.instrumentation.ClassContext
import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author Silvio Giebl
 */
class RetroFixMethodVisitor(
    private val classContext: ClassContext,
    private val methodMap: MethodMap,
    api: Int,
    nextMethodVisitor: MethodVisitor,
) : MethodVisitor(api, nextMethodVisitor) {

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean,
    ) {
        val entry = mapMethod(owner, opcode == Opcodes.INVOKESTATIC, name, descriptor)
        if (entry == null) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        } else {
            println(classContext.currentClassData.className)
            println("method $opcode $owner $name $descriptor $isInterface")
            super.visitMethodInsn(Opcodes.INVOKESTATIC, entry.newOwner, entry.newName, entry.newDescriptor, false)
        }
    }

    override fun visitInvokeDynamicInsn(
        name: String,
        descriptor: String,
        bootstrapMethodHandle: Handle,
        vararg bootstrapMethodArguments: Any,
    ) {
        val mappedBootstrapMethodArguments = Array(bootstrapMethodArguments.size) { i ->
            val argument = bootstrapMethodArguments[i]
            if (argument is Handle) {
                val entry = mapMethod(argument)
                if (entry == null) {
                    argument
                } else {
                    println(classContext.currentClassData.className)
                    println("invokeDynamic argument $name $descriptor $bootstrapMethodHandle ${bootstrapMethodArguments.contentToString()}")
                    Handle(Opcodes.H_INVOKESTATIC, entry.newOwner, entry.newName, entry.newDescriptor, false)
                }
            } else argument
        }

        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, *mappedBootstrapMethodArguments)
    }

    private fun mapMethod(owner: String, isStatic: Boolean, name: String, descriptor: String): MethodMap.Entry? {
        var entry = methodMap.get(name, descriptor)
        while (entry != null) {
            if (isStatic == entry.isStatic) {
                if (owner == entry.owner) {
                    return entry
                }
                val ownerClassData = classContext.loadClassData(owner.replace('/', '.'))
                if (ownerClassData != null) {
                    val entryOwner = entry.owner.replace('/', '.')
                    if ((entryOwner in ownerClassData.superClasses) || (!isStatic && (entryOwner in ownerClassData.interfaces))) {
                        return entry
                    }
                }
            }
            entry = entry.next
        }
        return null
    }

    private fun mapMethod(handle: Handle): MethodMap.Entry? {
        return mapMethod(handle.owner, handle.tag == Opcodes.H_INVOKESTATIC, handle.name, handle.desc)
    }
}
