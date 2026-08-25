package dev.gaphunter.grpcstreamobservercontractcompanion.model

import com.intellij.psi.PsiElement

/** One StreamObserver call (onNext/onError) found textually after an onCompleted() call on the same receiver, in the same method body. */
data class ContractViolationHit(val callElement: PsiElement)
