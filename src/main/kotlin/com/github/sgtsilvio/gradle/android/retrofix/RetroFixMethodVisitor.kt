package com.github.sgtsilvio.gradle.android.retrofix

import com.android.build.api.instrumentation.ClassContext
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap
import org.objectweb.asm.*

/**
 * @author Silvio Giebl
 */
class RetroFixMethodVisitor(
    private val classContext: ClassContext,
    private val typeMap: TypeMap,
    api: Int,
    methodVisitor: MethodVisitor,
) : MethodVisitor(api, methodVisitor) {

    override fun visitParameter(name: String?, access: Int) {
        println("visitParameter $name $access")
        super.visitParameter(name, access)
    }

    override fun visitAnnotationDefault(): AnnotationVisitor {
        // TODO replace types in AnnotationVisitor.visitEnum descriptor
        // TODO replace types in AnnotationVisitor.visitAnnotation descriptor
        println("visitAnnotationDefault")
        return super.visitAnnotationDefault()
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        // replace types in descriptor
        // TODO replace types in AnnotationVisitor.visitEnum descriptor
        // TODO replace types in AnnotationVisitor.visitAnnotation descriptor
        println("visitAnnotation $descriptor $visible")

        val fixedDescriptor = typeMap.fixClassDescriptor(descriptor)
        return super.visitAnnotation(fixedDescriptor, visible)
    }

    override fun visitTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String,
        visible: Boolean
    ): AnnotationVisitor {
        // replace types in descriptor
        // TODO replace types in AnnotationVisitor.visitEnum descriptor
        // TODO replace types in AnnotationVisitor.visitAnnotation descriptor
        println("visitTypeAnnotation $typeRef $typePath $descriptor $visible")

        val fixedDescriptor = typeMap.fixClassDescriptor(descriptor)
        return super.visitTypeAnnotation(typeRef, typePath, fixedDescriptor, visible)
    }

    override fun visitAnnotableParameterCount(parameterCount: Int, visible: Boolean) {
        println("visitAnnotableParameterCount $parameterCount $visible")
        super.visitAnnotableParameterCount(parameterCount, visible)
    }

    override fun visitParameterAnnotation(parameter: Int, descriptor: String, visible: Boolean): AnnotationVisitor {
        // replace types in descriptor
        // TODO replace types in AnnotationVisitor.visitEnum descriptor
        // TODO replace types in AnnotationVisitor.visitAnnotation descriptor
        println("visitParameterAnnotation $parameter $descriptor $visible")

        val fixedDescriptor = typeMap.fixClassDescriptor(descriptor)
        return super.visitParameterAnnotation(parameter, fixedDescriptor, visible)
    }

    override fun visitAttribute(attribute: Attribute) {
        println("visitAttribute $attribute")
        super.visitAttribute(attribute)
    }

    override fun visitCode() {
        println("visitCode")
        super.visitCode()
    }

    override fun visitFrame(type: Int, numLocal: Int, local: Array<out Any>, numStack: Int, stack: Array<out Any>) {
        // TODO replace types in local if is String (without L;)
        // TODO replace types in stack if is String (without L;)
        println("visitFrame $type $numLocal ${local.contentToString()} $numStack ${stack.contentToString()}")
        super.visitFrame(type, numLocal, local, numStack, stack)
    }

    override fun visitInsn(opcode: Int) {
        println("visitInsn $opcode")
        super.visitInsn(opcode)
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
        println("visitIntInsn $opcode $operand")
        super.visitIntInsn(opcode, operand)
    }

    override fun visitVarInsn(opcode: Int, `var`: Int) {
        println("visitVarInsn $opcode $`var`")
        super.visitVarInsn(opcode, `var`)
    }

    override fun visitTypeInsn(opcode: Int, type: String) {
        // replace types in type (without L;)
        println("visitTypeInsn $opcode $type")

//        val fixedType = typeMap.fixClass(type) // TODO array
//        super.visitTypeInsn(opcode, fixedType)
        super.visitTypeInsn(opcode, type)
    }

    override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
        // replace types in owner (without L;)
        // replace types in descriptor (L;)
        println("visitFieldInsn $opcode $owner $name $descriptor")

        val fixedOwner = typeMap.fixClassName(owner)
        val fixedDescriptor = typeMap.fixClassDescriptor(descriptor)
        super.visitFieldInsn(opcode, fixedOwner, name, fixedDescriptor)
    }

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
        // replace types in owner (without L;)
        // replace types in descriptor (L;)
        println("visitMethodInsn $opcode $owner $name $descriptor $isInterface")

        val fixedOwner = typeMap.fixClassName(owner)
        val fixedDescriptor = typeMap.fixMethodDescriptor(descriptor)
        super.visitMethodInsn(opcode, fixedOwner, name, fixedDescriptor, isInterface)
    }

    override fun visitInvokeDynamicInsn(
        name: String,
        descriptor: String,
        bootstrapMethodHandle: Handle,
        vararg bootstrapMethodArguments: Any
    ) {
        // replace types in descriptor (L;)
        // TODO replace method in bootstrapMethodArguments if is Handle (2nd param) (before type replacement)
        // replace types in bootstrapMethodArguments if Type (L;) or Handle (owner without L;, descriptor L;)
        println("visitInvokeDynamicInsn $name $descriptor $bootstrapMethodHandle ${bootstrapMethodArguments.contentToString()}")

        val fixedDescriptor = typeMap.fixMethodDescriptor(descriptor)
        val fixedBootstrapMethodArguments = Array(bootstrapMethodArguments.size) { i ->
            when (val argument = bootstrapMethodArguments[i]) {
                is Type -> typeMap.fixType(argument)
                is Handle -> typeMap.fixHandle(argument)
                else -> argument
            }
        }
        super.visitInvokeDynamicInsn(name, fixedDescriptor, bootstrapMethodHandle, *fixedBootstrapMethodArguments)
    }

    override fun visitJumpInsn(opcode: Int, label: Label) {
        println("visitJumpInsn $opcode $label")
        super.visitJumpInsn(opcode, label)
    }

    override fun visitLabel(label: Label) {
        println("visitLabel $label")
        super.visitLabel(label)
    }

    override fun visitLdcInsn(value: Any) {
        println("visitLdcInsn $value")
        super.visitLdcInsn(value)
    }

    override fun visitIincInsn(`var`: Int, increment: Int) {
        println("visitIincInsn $`var` $increment")
        super.visitIincInsn(`var`, increment)
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label, vararg labels: Label) {
        println("visitTableSwitchInsn $min $max $dflt ${labels.contentToString()}")
        super.visitTableSwitchInsn(min, max, dflt, *labels)
    }

    override fun visitLookupSwitchInsn(dflt: Label, keys: IntArray, labels: Array<out Label>) {
        println("visitLookupSwitchInsn $dflt ${keys.contentToString()} ${labels.contentToString()}")
        super.visitLookupSwitchInsn(dflt, keys, labels)
    }

    override fun visitMultiANewArrayInsn(descriptor: String, numDimensions: Int) {
        println("visitMultiANewArrayInsn $descriptor $numDimensions")
        super.visitMultiANewArrayInsn(descriptor, numDimensions)
    }

    override fun visitInsnAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String,
        visible: Boolean
    ): AnnotationVisitor {
        // replace types in descriptor
        // TODO replace types in AnnotationVisitor.visitEnum descriptor
        // TODO replace types in AnnotationVisitor.visitAnnotation descriptor
        println("visitInsnAnnotation $typeRef $typePath $descriptor $visible")

        val fixedDescriptor = typeMap.fixClassDescriptor(descriptor)
        return super.visitInsnAnnotation(typeRef, typePath, fixedDescriptor, visible)
    }

    override fun visitTryCatchBlock(start: Label, end: Label, handler: Label, type: String?) {
        // TODO replace types in type
        println("visitTryCatchBlock $start $end $handler $type")
        super.visitTryCatchBlock(start, end, handler, type)
    }

    override fun visitTryCatchAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String,
        visible: Boolean
    ): AnnotationVisitor {
        // replace types in descriptor
        // TODO replace types in AnnotationVisitor.visitEnum descriptor
        // TODO replace types in AnnotationVisitor.visitAnnotation descriptor
        println("visitTryCatchAnnotation $typeRef $typePath $descriptor $visible")

        val fixedDescriptor = typeMap.fixClassDescriptor(descriptor)
        return super.visitTryCatchAnnotation(typeRef, typePath, fixedDescriptor, visible)
    }

    override fun visitLocalVariable(
        name: String,
        descriptor: String,
        signature: String?,
        start: Label,
        end: Label,
        index: Int
    ) {
        // replace types in descriptor (L;)
        // TODO replace types signature (L<, L;)
        println("visitLocalVariable $name $descriptor $signature $start $end $index")

        val fixedDescriptor = typeMap.fixClassDescriptor(descriptor) // TODO array
        super.visitLocalVariable(name, fixedDescriptor, signature, start, end, index)
    }

    override fun visitLocalVariableAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        start: Array<out Label>,
        end: Array<out Label>,
        index: IntArray,
        descriptor: String,
        visible: Boolean
    ): AnnotationVisitor {
        // replace types in descriptor
        // TODO replace types in AnnotationVisitor.visitEnum descriptor
        // TODO replace types in AnnotationVisitor.visitAnnotation descriptor
        println("visitLocalVariableAnnotation $typeRef $typePath ${start.contentToString()} ${end.contentToString()} ${index.contentToString()} $descriptor $visible")

        val fixedDescriptor = typeMap.fixClassDescriptor(descriptor)
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, fixedDescriptor, visible)
    }

    override fun visitLineNumber(line: Int, start: Label) {
        println("visitLineNumber $line $start")
        super.visitLineNumber(line, start)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        println("visitMaxs $maxStack $maxLocals")
        super.visitMaxs(maxStack, maxLocals)
    }

    override fun visitEnd() {
        println("visitEnd")
        super.visitEnd()
    }
}