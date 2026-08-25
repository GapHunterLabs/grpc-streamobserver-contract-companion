package dev.gaphunter.grpcstreamobservercontractcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KotlinStreamObserverContractFinderTest : BasePlatformTestCase() {

    fun `test onNext after onCompleted on the same receiver is flagged`() {
        val file = myFixture.configureByText(
            "OrderService.kt",
            """
            class OrderService {
                fun streamOrders(responseObserver: StreamObserver<Order>) {
                    responseObserver.onNext(order1)
                    responseObserver.onCompleted()
                    responseObserver.onNext(order2)
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinStreamObserverContractFinder.findAll(file).size)
    }

    fun `test correct order is not flagged`() {
        val file = myFixture.configureByText(
            "OrderService.kt",
            """
            class OrderService {
                fun streamOrders(responseObserver: StreamObserver<Order>) {
                    responseObserver.onNext(order1)
                    responseObserver.onCompleted()
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinStreamObserverContractFinder.findAll(file).isEmpty())
    }
}
