package dev.gaphunter.grpcstreamobservercontractcompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import dev.gaphunter.grpcstreamobservercontractcompanion.model.ContractViolationHit

/**
 * Finds a `.onNext(...)` or `.onError(...)` call on a `StreamObserver`-
 * shaped receiver that appears, in textual order, AFTER an
 * `.onCompleted()` call on the same receiver, within the same method
 * body -- gRPC's own `StreamObserver` javadoc states the contract
 * explicitly: "onNext" ... "is never called after onError(Throwable)
 * or onCompleted() are called", and "onCompleted may only be called
 * once and if called it must be the last method called". Calling
 * either after completion is a real, documented contract violation
 * (undefined behavior downstream, not just a style nit).
 *
 * **v0.1 scope, stated honestly:** matches by simple receiver
 * variable/parameter name and simple method name (`onNext`/`onError`/
 * `onCompleted`), not real type resolution or control-flow analysis
 * -- an unrelated type that happens to share these three method names
 * is a possible (rare) false positive. Only catches the
 * straight-line, same-method-body case (both calls as direct
 * statements in the same method, textual order = execution order) --
 * doesn't attempt to trace calls split across branches/loops/helper
 * methods, which would need real control-flow analysis out of scope
 * for a v0.1 static scanner. A call guarded by an early `return`
 * between the two (so they can never both execute) is a possible
 * false positive not traced here either.
 */
object JavaStreamObserverContractFinder {

    private val COMPLETION_METHODS = setOf("onCompleted")
    private val POST_COMPLETION_METHODS = setOf("onNext", "onError")

    fun findAll(file: PsiFile): List<ContractViolationHit> {
        val hits = mutableListOf<ContractViolationHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitMethod(method: PsiMethod) {
                super.visitMethod(method)
                hits += hitsInMethod(method)
            }
        })
        return hits
    }

    private fun hitsInMethod(method: PsiMethod): List<ContractViolationHit> {
        val body = method.body ?: return emptyList()
        val calls = mutableListOf<PsiMethodCallExpression>()
        body.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                calls += expression
            }
        })
        // Textual order within the same method body IS execution order
        // for straight-line code -- calls are already visited depth-first
        // in source order by JavaRecursiveElementWalkingVisitor, but sort
        // explicitly by offset for clarity and safety against any future
        // visitor-order assumption change.
        calls.sortBy { it.textRange.startOffset }

        val completedReceivers = mutableSetOf<String>()
        val hits = mutableListOf<ContractViolationHit>()
        for (call in calls) {
            val methodName = call.methodExpression.referenceName ?: continue
            val receiverName = call.methodExpression.qualifierExpression?.text ?: continue

            if (methodName in POST_COMPLETION_METHODS && receiverName in completedReceivers) {
                hits += ContractViolationHit(leafOf(call))
            }
            if (methodName in COMPLETION_METHODS) {
                completedReceivers += receiverName
            }
        }
        return hits
    }

    /** Descends to a real leaf PSI element -- LineMarkerInfo must never anchor on a composite node. */
    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
