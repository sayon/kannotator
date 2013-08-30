package org.jetbrains.kannotator.main

import org.jetbrains.kannotator.declarations.*
import org.jetbrains.kannotator.index.DeclarationIndex
import org.jetbrains.kannotator.index.FieldDependencyInfo
import org.objectweb.asm.tree.MethodNode
import org.jetbrains.kannotator.annotationsInference.propagation.*
import org.jetbrains.kannotator.controlFlow.builder.analysis.*
import org.jetbrains.kannotator.annotationsInference.engine.*
import org.jetbrains.kannotator.annotations.io.AnnotationData

trait AnnotationInferrer<A: Any, I: Qualifier> {
    fun resolveAnnotation(classNames: Map<String, AnnotationData>): A?

    fun inferAnnotationsFromFieldValue(field: Field): Annotations<A>

    fun <Q: Qualifier> inferAnnotationsFromMethod(
            method: Method,
            methodNode: MethodNode,
            analysisResult: AnalysisResult<QualifiedValueSet<Q>>,
            fieldDependencyInfoProvider: (Field) -> FieldDependencyInfo,
            declarationIndex: DeclarationIndex,
            annotations: Annotations<A>): Annotations<A>

    val lattice: AnnotationLattice<A>
    val qualifierSet: QualifierSet<I>

    fun getFrameTransformer(annotations: Annotations<A>, declarationIndex: DeclarationIndex): FrameTransformer<QualifiedValueSet<*>>
    fun getQualifierEvaluator(positions: PositionsForMethod, annotations: Annotations<A>, declarationIndex: DeclarationIndex): QualifierEvaluator<I>
}