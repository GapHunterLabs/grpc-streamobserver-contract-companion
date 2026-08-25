// Demo data for gRPC StreamObserver Contract Companion -- used with
// `./gradlew runIde` to capture the real Marketplace screenshot. Open
// this file, the warning should appear on the second onNext() call.

class OrderService {

    void streamOrdersUnsafely(StreamObserver<Order> responseObserver) {
        responseObserver.onNext(firstOrder);
        responseObserver.onCompleted();
        // Called after onCompleted() -- FLAGGED. Violates the
        // StreamObserver contract: onCompleted() must be the last
        // method called.
        responseObserver.onNext(strayOrder);
    }

    void streamOrdersSafely(StreamObserver<Order> responseObserver) {
        responseObserver.onNext(firstOrder);
        responseObserver.onNext(secondOrder);
        // Correct order -- NOT flagged.
        responseObserver.onCompleted();
    }
}
