package com.PCD.pcd.GrpcCozinha;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GrpcServerConfig {

    @Value("${grpc.server.port:9090}")
    private int port;

    private final CozinhaServiceImpl cozinhaService;
    private Server server;

    public GrpcServerConfig(CozinhaServiceImpl cozinhaService) {
        this.cozinhaService = cozinhaService;
    }

    @PostConstruct
    public void start() throws IOException {
        server = ServerBuilder.forPort(port).addService(cozinhaService).build().start();
        System.out.println("Serviço da Cozinha iniciado na porta " + port);
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}

