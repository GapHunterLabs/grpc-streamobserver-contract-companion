package dev.gaphunter.grpcstreamobservercontractcompanion.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import dev.gaphunter.grpcstreamobservercontractcompanion.detect.JavaStreamObserverContractFinder
import dev.gaphunter.grpcstreamobservercontractcompanion.detect.KotlinStreamObserverContractFinder
import dev.gaphunter.grpcstreamobservercontractcompanion.model.ContractViolationHit
import dev.gaphunter.grpcstreamobservercontractcompanion.review.ReviewPrompt

class ContractViolationLineMarkerProvider : LineMarkerProviderDescriptor(), DumbAware {

    override fun getName(): String = "StreamObserver call after onCompleted()"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        val file = elements.firstOrNull()?.containingFile ?: return
        val hits = when (file.language.id) {
            "JAVA" -> JavaStreamObserverContractFinder.findAll(file)
            "kotlin" -> KotlinStreamObserverContractFinder.findAll(file)
            else -> emptyList()
        }
        if (hits.isEmpty()) return

        val hitsByElement = hits.associateBy { it.callElement }
        for (element in elements) {
            val hit = hitsByElement[element] ?: continue
            result.add(buildMarker(hit))

            val path = file.virtualFile?.path ?: continue
            val lineNumber = file.viewProvider.document?.getLineNumber(element.textRange.startOffset) ?: -1
            ReviewPrompt.recordHit(file.project, "$path:$lineNumber")
        }
    }

    private fun buildMarker(hit: ContractViolationHit): LineMarkerInfo<PsiElement> {
        val tooltip = "This call happens after onCompleted() was already called on the same StreamObserver -- " +
            "gRPC's own javadoc states the contract explicitly: onNext/onError are never called after " +
            "onCompleted(), and onCompleted() must be the last method called"
        return LineMarkerInfo(
            hit.callElement,
            hit.callElement.textRange,
            ContractViolationIcons.RISK,
            { _: PsiElement -> tooltip },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { tooltip },
        )
    }
}
