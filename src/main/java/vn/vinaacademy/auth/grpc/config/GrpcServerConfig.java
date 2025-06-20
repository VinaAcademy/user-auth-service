package vn.vinaacademy.auth.grpc.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import vn.vinaacademy.auth.grpc.JwtServiceGrpcImpl;
import vn.vinaacademy.common.config.GrpcServerProperties;

import java.io.IOException;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class GrpcServerConfig {
    private final JwtServiceGrpcImpl jwtServiceGrpc;
    private final GrpcServerProperties serverProperties;
    private Server server;

    @EventListener(ContextRefreshedEvent.class)
    public void startGrpcServer() throws IOException {
        server = ServerBuilder.forPort(serverProperties.getPort())
                .addService(jwtServiceGrpc)
                .build();
        server.start();

        log.info("gRPC server started on port {}", serverProperties.getPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server...");
            if (server != null) {
                server.shutdown();
                log.info("gRPC server shut down successfully.");
            }
        }));
    }
}
