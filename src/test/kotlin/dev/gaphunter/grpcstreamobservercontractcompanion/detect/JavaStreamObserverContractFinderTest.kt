package dev.gaphunter.grpcstreamobservercontractcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JavaStreamObserverContractFinderTest : BasePlatformTestCase() {

    fun `test onNext after onCompleted on the same receiver is flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                void streamOrders(StreamObserver<Order> responseObserver) {
                    responseObserver.onNext(order1);
                    responseObserver.onCompleted();
                    responseObserver.onNext(order2);
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaStreamObserverContractFinder.findAll(file).size)
    }

    fun `test onError after onCompleted on the same receiver is flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                void streamOrders(StreamObserver<Order> responseObserver) {
                    responseObserver.onCompleted();
                    responseObserver.onError(new RuntimeException());
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaStreamObserverContractFinder.findAll(file).size)
    }

    fun `test correct order is not flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                void streamOrders(StreamObserver<Order> responseObserver) {
                    responseObserver.onNext(order1);
                    responseObserver.onNext(order2);
                    responseObserver.onCompleted();
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaStreamObserverContractFinder.findAll(file).isEmpty())
    }

    fun `test onCompleted on a different receiver does not affect this one`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                void streamOrders(StreamObserver<Order> a, StreamObserver<Order> b) {
                    a.onCompleted();
                    b.onNext(order1);
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaStreamObserverContractFinder.findAll(file).isEmpty())
    }
}
