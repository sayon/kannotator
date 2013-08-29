package org.jetbrains.kannotator.main

import java.util.HashMap
import org.jetbrains.kannotator.declarations.*
import org.objectweb.asm.tree.FieldNode
import org.jetbrains.kannotator.controlFlow.builder.analysis.*
import org.jetbrains.kannotator.annotations.io.AnnotationData
import org.jetbrains.kannotator.runtime.annotations.AnalysisType

public fun <K : AnalysisType> loadFieldAnnotationsFromByteCode(
        fieldNodes: Map<Field, FieldNode>,
        inferrers: Map<K, AnnotationInferrer<Any, Qualifier>>
): Map<K, Annotations<Any>> {
    val internalAnnotationsMap = inferrers.mapValues { entry -> AnnotationsImpl<Any>() }

    for ((field, node) in fieldNodes) {
        val position = getFieldTypePosition(field)

        val annotationsMap = HashMap<String, AnnotationData>()
        node.visibleAnnotations?.extractAnnotationDataMapTo(annotationsMap)
        node.invisibleAnnotations?.extractAnnotationDataMapTo(annotationsMap)

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

    return internalAnnotationsMap
}