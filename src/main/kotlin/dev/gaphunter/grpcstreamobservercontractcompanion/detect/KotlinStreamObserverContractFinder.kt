package dev.gaphunter.grpcstreamobservercontractcompanion.detect

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.gaphunter.grpcstreamobservercontractcompanion.model.ContractViolationHit
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/** Kotlin counterpart of [JavaStreamObserverContractFinder]. */
object KotlinStreamObserverContractFinder {

    private val COMPLETION_METHODS = setOf("onCompleted")
    private val POST_COMPLETION_METHODS = setOf("onNext", "onError")

    fun findAll(file: PsiFile): List<ContractViolationHit> {
        if (file !is KtFile) return emptyList()
        val hits = mutableListOf<ContractViolationHit>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                super.visitNamedFunction(function)
                hits += hitsInFunction(function)
            }
        })
        return hits
    }

    private fun hitsInFunction(function: KtNamedFunction): List<ContractViolationHit> {
        val body = function.bodyBlockExpression ?: function.bodyExpression ?: return emptyList()
        val calls = mutableListOf<KtDotQualifiedExpression>()
        body.accept(object : KtTreeVisitorVoid() {
            override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                super.visitDotQualifiedExpression(expression)
                if (expression.selectorExpression is KtCallExpression) calls += expression
            }
        })
        calls.sortBy { it.textRange.startOffset }

        val completedReceivers = mutableSetOf<String>()
        val hits = mutableListOf<ContractViolationHit>()
        for (expression in calls) {
            val call = expression.selectorExpression as? KtCallExpression ?: continue
            val methodName = call.calleeExpression?.text ?: continue
            val receiverName = expression.receiverExpression.text

            if (methodName in POST_COMPLETION_METHODS && receiverName in completedReceivers) {
                hits += ContractViolationHit(leafOf(expression))
            }
            if (methodName in COMPLETION_METHODS) {
                completedReceivers += receiverName
            }
        }
        return hits
    }

    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
