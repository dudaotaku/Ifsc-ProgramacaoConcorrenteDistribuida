package com.PCD.pcd.Config;

import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class SocketIOService {

    private final SocketIOServer server;

    public SocketIOService(SocketIOServer server) {
        this.server = server;
    }

    @PostConstruct
    public void start() {
        server.start();
    }

    @PreDestroy
    public void stop() {
        server.stop();
    }

    public void emitirAtualizacao(String evento, Object dados) {

        server.getBroadcastOperations().sendEvent(evento, dados);
    }
}