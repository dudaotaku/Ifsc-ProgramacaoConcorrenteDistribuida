package com.PCD.pcd.GrpcCozinha;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import com.PCD.pcd.grpc.CozinhaRequest;
import com.PCD.pcd.grpc.CozinhaResponse;
import com.PCD.pcd.grpc.CozinhaServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Component;

@Component
public class CozinhaGrpcClient {
    @Value("${grpc.server.host:localhost}")
    private String host;

    @Value("${grpc.server.port:9090}")
    private int port;

    private ManagedChannel channel;
    private CozinhaServiceGrpc.CozinhaServiceBlockingStub stub;

    private synchronized CozinhaServiceGrpc.CozinhaServiceBlockingStub getStub() {
        if (stub == null) {
            channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .build();
            stub = CozinhaServiceGrpc.newBlockingStub(channel);
        }
        return stub;
    }

    public CozinhaResponse iniciarPedido(Long pedidoId) {
        System.out.println("[gRPC-Client] Solicitando início do pedido ID " + pedidoId);
        return getStub().iniciarPedido(CozinhaRequest.newBuilder().setPedidoId(pedidoId).build());
    }

    public CozinhaResponse finalizarPedido(Long pedidoId) {
        System.out.println("[gRPC-Client] Solicitando finalização do pedido ID " + pedidoId);
        return getStub().finalizarPedido(CozinhaRequest.newBuilder().setPedidoId(pedidoId).build());
    }

    public CozinhaResponse entregarPedido(Long pedidoId) {
        System.out.println("[gRPC-Client] Solicitando entrega do pedido ID " + pedidoId);
        return getStub().entregarPedido(CozinhaRequest.newBuilder().setPedidoId(pedidoId).build());
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) channel.shutdown();
    }
}
