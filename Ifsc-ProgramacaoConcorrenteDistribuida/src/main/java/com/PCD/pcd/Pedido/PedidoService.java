package com.PCD.pcd.Pedido;

import com.PCD.pcd.Config.SocketIOService;
import com.PCD.pcd.GrpcCozinha.CozinhaGrpcClient;
import com.PCD.pcd.grpc.CozinhaResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class PedidoService {

    private final PedidoRepository repository;
    private final SocketIOService socketService;
    private final CozinhaGrpcClient cozinhaGrpcClient;

    private final BlockingQueue<Pedido> filaPedidos = new LinkedBlockingQueue<>();

    public PedidoService(PedidoRepository repository, SocketIOService socketService, CozinhaGrpcClient cozinhaGrpcClient) {
        this.repository = repository;
        this.socketService = socketService;
        this.cozinhaGrpcClient = cozinhaGrpcClient;
    }

    public Pedido criarPedido(Pedido pedido) {
        pedido.setStatus("RECEBIDO");
        Pedido pedidoSalvo = repository.save(pedido);

        filaPedidos.offer(pedidoSalvo);
        System.out.println("Pedido ID " + pedidoSalvo.getId() + " entrou na fila da cozinha.");

        return pedidoSalvo;
    }

    public List<Pedido> listarTodos() {
        return repository.findAll();
    }

    @PostConstruct
    public void iniciarWorkerCozinha() {
        Thread workerThread = new Thread(() -> {
            while (true) {
                try {
                    Pedido pedido = filaPedidos.take();

                    pedido.setStatus("AGUARDANDO");
                    repository.save(pedido);

                    socketService.emitirAtualizacao("novoPedido: ", pedido);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("[Worker] Erro: " + e.getMessage());
                }
            }
        });
        workerThread.setName("WorkerCozinha-Thread");
        workerThread.start();
    }

    public com.PCD.pcd.grpc.CozinhaResponse iniciarPedido(Long pedidoId) {
        com.PCD.pcd.grpc.CozinhaResponse resposta = cozinhaGrpcClient.iniciarPedido(pedidoId);
        if (resposta.getSucesso()) {
            repository.findById(pedidoId).ifPresent(p ->
                    socketService.emitirAtualizacao("atualizacaoPedido", p)
            );
        }
        return resposta;
    }


    public CozinhaResponse finalizarPedido(Long pedidoId) {
        com.PCD.pcd.grpc.CozinhaResponse resposta = cozinhaGrpcClient.finalizarPedido(pedidoId);
        if (resposta.getSucesso()) {
            repository.findById(pedidoId).ifPresent(p ->
                    socketService.emitirAtualizacao("atualizacaoPedido", p)
            );
        }
        return resposta;
    }


    public CozinhaResponse entregarPedido(Long pedidoId) {
        CozinhaResponse resposta = cozinhaGrpcClient.entregarPedido(pedidoId);
        if (resposta.getSucesso()) {
            repository.findById(pedidoId).ifPresent(p ->
                    socketService.emitirAtualizacao("pedidoEntregue", p)
            );
        }
        return resposta;
    }
}