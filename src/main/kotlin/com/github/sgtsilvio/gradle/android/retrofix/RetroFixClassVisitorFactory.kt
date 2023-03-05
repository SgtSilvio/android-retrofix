package com.github.sgtsilvio.gradle.android.retrofix

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap
import org.objectweb.asm.ClassVisitor

/**
 * @author Silvio Giebl
 */
abstract class RetroFixClassVisitorFactory : AsmClassVisitorFactory<RetroFixClassVisitorFactory.Parameters> {

    interface Parameters : InstrumentationParameters {

    }

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
//        print("cCV ")
//        System.out.flush()
//        println(classContext.loadClassData("java.util.HashMap"))
        println(classContext)
        if (classContext.currentClassData.className == "com.hivemq.client.hivemq_mqtt_client_app.MainActivity") {
//            println("HEY")
            return RetroFixClassVisitor(classContext, TypeMap(), instrumentationContext.apiVersion.get(), nextClassVisitor)
        }
        return nextClassVisitor
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
//        print("iI ")
//        System.out.flush()
        return true
    }
}
