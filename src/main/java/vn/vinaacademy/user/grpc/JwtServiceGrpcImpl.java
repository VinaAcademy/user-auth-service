package vn.vinaacademy.user.grpc;


import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.vinaacademy.user.service.JwtService;
import vn.vinaacademy.grpc.JwtServiceGrpc;
import vn.vinaacademy.grpc.JwtServiceProto;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtServiceGrpcImpl extends JwtServiceGrpc.JwtServiceImplBase {
    private final JwtService jwtService;

    @Override
    public void validate(JwtServiceProto.Token request, StreamObserver<JwtServiceProto.ValidateTokenResponse> responseObserver) {
        log.info("Received validate request with token: {}", request.getToken());
        try {
            boolean isValid = jwtService.isValidToken(request.getToken());

            JwtServiceProto.ValidateTokenResponse response = JwtServiceProto.ValidateTokenResponse.newBuilder()
                    .setIsValid(isValid)
                    .setMessage(isValid ? "Token is valid" : "Token is invalid")
                    .setUserId(jwtService.extractUserId(request.getToken()))
                    .setEmail(jwtService.extractEmail(request.getToken()))
                    .setRoles(jwtService.extractRoles(request.getToken()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            responseObserver.onError(
                    io.grpc.Status.INTERNAL.withDescription("Internal server error").withCause(e).asRuntimeException()
            );
        }
    }
}
