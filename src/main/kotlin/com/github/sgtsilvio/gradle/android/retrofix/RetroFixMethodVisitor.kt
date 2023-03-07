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
    methodVisitor: MethodVisitor,
) : MethodVisitor(api, methodVisitor) {

//    override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String) {
//        super.visitMethodInsn(opcode, owner, name, descriptor)
//    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        // TODO replace method (before type replacement)
        // opcode gets always INVOKESTATIC
        // owner is replaced
        // name is replaced
        // descriptor is replaced
        // isInterface gets always false
        val entry = mapMethod(owner, opcode == Opcodes.INVOKESTATIC, name, descriptor)
        if (entry == null) {
//            if (name=="from"&&owner=="java/util/Date") {
//                println("AAAAAAAAAAAA")
//                println("method $opcode $owner $name $descriptor $isInterface")
//            }
//            if (name=="from"&&owner=="java/sql/Date") {
//                println("BBBBBBBBBBBB")
//                println("method $opcode $owner $name $descriptor $isInterface")
//            }
//            if (name=="a"&&owner.endsWith("A")) {
//                println("AAAAAAAAAAA")
//            }
//            if (name=="a"&&owner.endsWith("B")) {
//                println("BBBBBBBBBBB")
//            }
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
        vararg bootstrapMethodArguments: Any
    ) {
        // name stays as should be implemented
        // descriptor stays as should be implemented
        // handler stays as only LambdaMetaFactory in java
        // Type in bootstrapMethodArguments stays as should be implemented
        // TODO replace method in bootstrapMethodArguments if is Handle (2nd param) (before type replacement)
//        if (mapMethod(bootstrapMethodHandle) != null) { // TODO wrong
//            println(classContext.currentClassData.className)
//            println("invokeDynamic handle $name $descriptor $bootstrapMethodHandle ${bootstrapMethodArguments.contentToString()}")
//        }
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
        val key = "$name$descriptor"
        var entry = methodMap[key]
        while (entry != null) {
            if (isStatic == entry.isStatic) {
                if (owner == entry.owner) {
                    return entry
                }
//                if (!isStatic) {
                    val ownerClassData = classContext.loadClassData(owner.replace('/', '.'))
                    if (ownerClassData != null) {
                        val entryOwner = entry.owner.replace('/', '.')
                        if ((entryOwner in ownerClassData.superClasses) || (entryOwner in ownerClassData.interfaces)) {
                            return entry
                        }
                    }
//                }
            }
            entry = entry.next
        }
        return null
    }

    private fun mapMethod(handle: Handle): MethodMap.Entry? {
        return mapMethod(handle.owner, handle.tag == Opcodes.H_INVOKESTATIC, handle.name, handle.desc)
    }
}