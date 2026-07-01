package com.PCD.pcd.GrpcCozinha;

import com.PCD.pcd.Pedido.Pedido;
import com.PCD.pcd.Pedido.PedidoRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class CozinhaServiceImpl extends com.PCD.pcd.grpc.CozinhaServiceGrpc.CozinhaServiceImplBase{

    private final PedidoRepository pedidoRepository;

    private final ReentrantLock mutex = new ReentrantLock();

    public CozinhaServiceImpl(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public void iniciarPedido(com.PCD.pcd.grpc.CozinhaRequest request, StreamObserver<com.PCD.pcd.grpc.CozinhaResponse> responseObserver) {
        Long pedidoId = request.getPedidoId();

        mutex.lock();
        try {
            Optional<Pedido> optional = pedidoRepository.findById(pedidoId);
            if (optional.isEmpty()) {
                responseObserver.onNext(com.PCD.pcd.grpc.CozinhaResponse.newBuilder()
                        .setPedidoId(pedidoId).setSucesso(false)
                        .setMensagem("Pedido não encontrado.").build());
                responseObserver.onCompleted();
                return;
            }

            Pedido pedido = optional.get();
            if (!"AGUARDANDO".equals(pedido.getStatus())) {
                responseObserver.onNext(com.PCD.pcd.grpc.CozinhaResponse.newBuilder()
                        .setPedidoId(pedidoId).setSucesso(false)
                        .setNovoStatus(pedido.getStatus())
                        .setMensagem("Pedido já está " + pedido.getStatus() + ".").build());
                responseObserver.onCompleted();
                return;
            }

            pedido.setStatus("EM_PREPARO");
            pedidoRepository.save(pedido);

            responseObserver.onNext(com.PCD.pcd.grpc.CozinhaResponse.newBuilder()
                    .setPedidoId(pedidoId).setSucesso(true)
                    .setNovoStatus("EM_PREPARO")
                    .setMensagem("Pedido iniciado com sucesso.").build());
            responseObserver.onCompleted();
        } finally {
            mutex.unlock();
        }
    }


    @Override
    public void finalizarPedido(com.PCD.pcd.grpc.CozinhaRequest request, StreamObserver<com.PCD.pcd.grpc.CozinhaResponse> responseObserver) {
        Long pedidoId = request.getPedidoId();

        mutex.lock();
        try {
            Optional<Pedido> optional = pedidoRepository.findById(pedidoId);
            if (optional.isEmpty()) {
                responseObserver.onNext(com.PCD.pcd.grpc.CozinhaResponse.newBuilder()
                        .setPedidoId(pedidoId).setSucesso(false)
                        .setMensagem("Pedido não encontrado.").build());
                responseObserver.onCompleted();
                return;
            }

            Pedido pedido = optional.get();
            if (!"EM_PREPARO".equals(pedido.getStatus())) {
                responseObserver.onNext(com.PCD.pcd.grpc.CozinhaResponse.newBuilder()
                        .setPedidoId(pedidoId).setSucesso(false)
                        .setNovoStatus(pedido.getStatus())
                        .setMensagem("Pedido já está " + pedido.getStatus() + ".").build());
                responseObserver.onCompleted();
                return;
            }

            pedido.setStatus("PRONTO");
            pedidoRepository.save(pedido);

            responseObserver.onNext(com.PCD.pcd.grpc.CozinhaResponse.newBuilder()
                    .setPedidoId(pedidoId).setSucesso(true)
                    .setNovoStatus("PRONTO")
                    .setMensagem("Pedido finalizado com sucesso.").build());
            responseObserver.onCompleted();
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public void entregarPedido(com.PCD.pcd.grpc.CozinhaRequest request, StreamObserver<com.PCD.pcd.grpc.CozinhaResponse> responseObserver) {
        Long pedidoId = request.getPedidoId();

        mutex.lock();
        try {
            Optional<Pedido> optional = pedidoRepository.findById(pedidoId);
            if (optional.isEmpty()) {
                responseObserver.onNext(com.PCD.pcd.grpc.CozinhaResponse.newBuilder()
                        .setPedidoId(pedidoId).setSucesso(false)
                        .setMensagem("Pedido não encontrado.").build());
                responseObserver.onCompleted();
                return;
            }

            Pedido pedido = optional.get();
            if (!"PRONTO".equals(pedido.getStatus())) {
                responseObserver.onNext(com.PCD.pcd.grpc.CozinhaResponse.newBuilder()
                        .setPedidoId(pedidoId).setSucesso(false)
                        .setNovoStatus(pedido.getStatus())
                        .setMensagem("Pedido já está " + pedido.getStatus() + ".").build());
                responseObserver.onCompleted();
                return;
            }

            pedido.setStatus("ENTREGUE");
            pedidoRepository.save(pedido);

            responseObserver.onNext(com.PCD.pcd.grpc.CozinhaResponse.newBuilder()
                    .setPedidoId(pedidoId).setSucesso(true)
                    .setNovoStatus("ENTREGUE")
                    .setMensagem("Pedido entregue ao cliente.").build());
            responseObserver.onCompleted();
        } finally {
            mutex.unlock();
        }
    }
}
