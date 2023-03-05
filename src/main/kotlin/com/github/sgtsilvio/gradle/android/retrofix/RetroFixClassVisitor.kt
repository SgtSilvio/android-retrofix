package com.github.sgtsilvio.gradle.android.retrofix

import com.android.build.api.instrumentation.ClassContext
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap
import org.objectweb.asm.*

/**
 * @author Silvio Giebl
 */
class RetroFixClassVisitor(
    private val classContext: ClassContext,
    private val typeMap: TypeMap,
    api: Int,
    classVisitor: ClassVisitor,
) : ClassVisitor(api, classVisitor) {

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?,
    ) {
        // TODO replace types in signature (example <K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/util/HashMap<TK;TV;>)
        // replace types in superName (without L;)
        // replace types in interfaces (without L;)
        println("visit $version $access $name $signature $superName ${interfaces.contentToString()}")

        val fixedSuperName = superName?.let { typeMap.fixClassName(it) }
        val fixedInterfaces = interfaces?.let { Array(it.size) { i -> typeMap.fixClassName(it[i]) } }
        super.visit(version, access, name, signature, fixedSuperName, fixedInterfaces)
    }

    override fun visitSource(source: String?, debug: String?) {
        println("visitSource $source $debug")
        super.visitSource(source, debug)
    }

    override fun visitModule(name: String, access: Int, version: String?): ModuleVisitor {
        println("visitModule $name $access $version")
        return super.visitModule(name, access, version)
    }

    override fun visitNestHost(nestHost: String) {
        println("visitNestHost $nestHost")
        super.visitNestHost(nestHost)
    }

    override fun visitOuterClass(owner: String, name: String?, descriptor: String?) {
        println("visitOuterClass $owner $name $descriptor")
        super.visitOuterClass(owner, name, descriptor)
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        // replace types in descriptor (e.g. FunctionalInterface) (L;)
        // TODO replace types in AnnotationVisitor.visitEnum descriptor
        // TODO replace types in AnnotationVisitor.visitAnnotation descriptor
        println("visitAnnotation $descriptor $visible")

        val fixedDescriptor = typeMap.fixDescriptor(descriptor)
        return super.visitAnnotation(fixedDescriptor, visible)
    }

    override fun visitTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String,
        visible: Boolean,
    ): AnnotationVisitor {
        // replace types in descriptor
        // TODO replace types in AnnotationVisitor.visitEnum descriptor
        // TODO replace types in AnnotationVisitor.visitAnnotation descriptor
        println("visitTypeAnnotation $typeRef $typePath $descriptor $visible")

        val fixedDescriptor = typeMap.fixDescriptor(descriptor)
        return super.visitTypeAnnotation(typeRef, typePath, fixedDescriptor, visible)
    }

    override fun visitAttribute(attribute: Attribute) {
        println("visitAttribute $attribute")
        super.visitAttribute(attribute)
    }

    override fun visitNestMember(nestMember: String) {
        println("visitNestMember $nestMember")
        super.visitNestMember(nestMember)
    }

    override fun visitPermittedSubclass(permittedSubclass: String) {
        println("visitPermittedSubclass $permittedSubclass")
        super.visitPermittedSubclass(permittedSubclass)
    }

    override fun visitInnerClass(name: String, outerName: String?, innerName: String?, access: Int) {
        println("visitInnerClass $name $outerName $innerName $access")
        super.visitInnerClass(name, outerName, innerName, access)
    }

    override fun visitRecordComponent(name: String, descriptor: String, signature: String?): RecordComponentVisitor {
        // replace types in descriptor
        // TODO replace types in signature
        // TODO replace types in RecordComponentVisitor.visitAnnotation descriptor
        // TODO replace types in RecordComponentVisitor.visitTypeAnnotation descriptor
        println("visitRecordComponent $name $descriptor $signature")

        val fixedDescriptor = typeMap.fixDescriptor(descriptor)
        return super.visitRecordComponent(name, fixedDescriptor, signature)
    }

    override fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        value: Any?,
    ): FieldVisitor {
        // replace types in descriptor
        // TODO replace types in signature
        // TODO replace types in FieldVisitor.visitAnnotation descriptor
        // TODO replace types in FieldVisitor.visitTypeAnnotation descriptor
        println("visitField $access $name $descriptor $signature $value")

        val fixedDescriptor = typeMap.fixDescriptor(descriptor)
        return super.visitField(access, name, fixedDescriptor, signature, value)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor {
        // replace types in descriptor (L;)
        // TODO replace types in signature
        // replace types in exceptions
        println("visitMethod $access $name $descriptor $signature ${exceptions.contentToString()}")

        val fixedDescriptor = typeMap.fixDescriptor(descriptor)
        val fixedExceptions = exceptions?.let { Array(it.size) { i -> typeMap.fixClassName(it[i]) } }
        return RetroFixMethodVisitor(
            classContext,
            typeMap,
            api,
            super.visitMethod(access, name, fixedDescriptor, signature, fixedExceptions),
        )
    }

    override fun visitEnd() {
        println("visitEnd")
        super.visitEnd()
    }
}

internal fun TypeMap.fixClassName(internalName: String) = this[internalName] ?: internalName

internal fun TypeMap.fixDescriptor(descriptor: String): String {
    var searchIndex = 0
    var appendIndex = 0
    var stringBuilder: StringBuilder? = null
    while (true) {
        val startIndex = descriptor.indexOf('L', searchIndex)
        if (startIndex == -1) break
        val endIndex = descriptor.indexOf(';', startIndex + 1)
        if (endIndex == -1) break
        val className = descriptor.substring(startIndex + 1, endIndex)
        val replacedClassName = this[className]
        if (replacedClassName != null) {
            if (stringBuilder == null) {
                stringBuilder = StringBuilder(descriptor.length + replacedClassName.length - className.length)
            }
            stringBuilder.append(descriptor, appendIndex, startIndex + 1)
            stringBuilder.append(replacedClassName)
            appendIndex = endIndex
        }
        searchIndex = endIndex + 1
    }
    return stringBuilder?.append(descriptor, appendIndex, descriptor.length)?.toString() ?: descriptor
}

internal fun TypeMap.fixType(type: Type) = Type.getType(fixDescriptor(type.descriptor))

internal fun TypeMap.fixHandle(handle: Handle) =
    Handle(handle.tag, fixClassName(handle.owner), handle.name, fixDescriptor(handle.desc), handle.isInterface)
