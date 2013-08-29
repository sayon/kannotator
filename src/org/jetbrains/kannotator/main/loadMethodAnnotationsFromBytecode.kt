package org.jetbrains.kannotator.main

import java.util.HashMap
import org.jetbrains.kannotator.declarations.*
import org.objectweb.asm.tree.MethodNode
import java.util.Collections
import org.jetbrains.kannotator.controlFlow.builder.analysis.*
import org.jetbrains.kannotator.annotations.io.AnnotationData
import org.jetbrains.kannotator.runtime.annotations.AnalysisType

public fun <K : AnalysisType> loadMethodAnnotationsFromByteCode(
        methodNodes: Map<Method, MethodNode>,
        inferrers: Map<K, AnnotationInferrer<Any, Qualifier>>
): Map<K, Annotations<Any>> {
    val internalAnnotationsMap = inferrers.mapValues { entry -> AnnotationsImpl<Any>() }

    for ((method, methodNode) in methodNodes) {

        PositionsForMethod(method).forEachValidPosition {
            position ->
            val declPos = position.relativePosition
            val annotationsMap =
                    when (declPos) {
                        RETURN_TYPE -> {
                            val annotationsMap = HashMap<String, AnnotationData>()
                            methodNode.visibleAnnotations?.extractAnnotationDataMapTo(annotationsMap)
                            methodNode.invisibleAnnotations?.extractAnnotationDataMapTo(annotationsMap)
                            annotationsMap
                        }
                        is ParameterPosition -> {
                            val annotationsMap = HashMap<String, AnnotationData>()
                            val index = if (method.isStatic()) declPos.index else declPos.index - 1
                            if (methodNode.visibleParameterAnnotations != null && index < methodNode.visibleParameterAnnotations!!.size) {
                                methodNode.visibleParameterAnnotations!![index]?.filterNotNull()?.extractAnnotationDataMapTo(annotationsMap)
                            }

                            if (methodNode.invisibleParameterAnnotations != null && index < methodNode.invisibleParameterAnnotations!!.size) {
                                methodNode.invisibleParameterAnnotations!![index]?.filterNotNull()?.extractAnnotationDataMapTo(annotationsMap)
                            }
                            annotationsMap
                        }
                        else -> Collections.emptyMap<String, AnnotationData>()
                    }

            if (!annotationsMap.empty) {
                for ((inferrerKey, inferrer) in inferrers) {
                    val internalAnnotations = internalAnnotationsMap[inferrerKey]!!
                    val annotation = inferrer.resolveAnnotation(annotationsMap)
                    if (annotation != null) {
                        internalAnnotations[position] = annotation
                    }
                }
            }
        }

    }

    return internalAnnotationsMap
}