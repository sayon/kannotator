package org.jetbrains.kannotator.classHierarchy

import java.util.HashMap
import kotlinlib.flags
import org.jetbrains.kannotator.declarations.ClassName
import org.jetbrains.kannotator.declarations.Method
import org.jetbrains.kannotator.index.ClassSource
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassReader.*
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

fun buildClassHierarchyGraph(classSource: ClassSource): HierarchyGraph<ClassData> {
    val nodesByName = HashMap<ClassName, ClassNodeImpl>()

    fun getNodeByName(name: ClassName) = nodesByName.getOrPut(name) { ClassNodeImpl(name) }

    fun addEdge(base: ClassNodeImpl, derived: ClassNodeImpl) {
        val edge = ClassHierarchyEdgeImpl(base, derived)
        base.children.add(edge)
        derived.parents.add(edge)
    }

    classSource forEach {
        reader ->
        val className = ClassName.fromInternalName(reader.getClassName())
        val node = getNodeByName(className)
        val (methods, superClasses) = processClass(reader)
        for (superClass in superClasses) {
            val superClassNode = getNodeByName(superClass)
            addEdge(base = superClassNode, derived = node)
        }
        for (methodName in methods) {
            node.methods.add(methodName)
        }
    }

    return object : HierarchyGraph<ClassData> {
        override val nodes: Collection<HierarchyNode<ClassData>> = nodesByName.values()
    }
}

private data class MethodsAndSuperClasses(
        val methods: List<Method>,
        val superClasses: List<ClassName>
)

private fun processClass(reader: ClassReader): MethodsAndSuperClasses {
    val methods = arrayList<Method>()
    val superClasses = arrayList<ClassName>()
    val thisClassName = ClassName.fromInternalName(reader.getClassName())

    reader.accept(object : ClassVisitor(Opcodes.ASM4) {

        public override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
            if (interfaces != null) {
                for (interface in interfaces) {
                    superClasses.add(ClassName.fromInternalName(interface))
                }
            }
            if (superName != null) {
                superClasses.add(ClassName.fromInternalName(superName))
            }
        }

        public override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
            val method = Method(thisClassName, access, name, desc)
            methods.add(method)
            return null
        }
    },
            flags(SKIP_CODE, SKIP_DEBUG, SKIP_FRAMES))

    return MethodsAndSuperClasses(methods, superClasses)
}