package com.PCD.pcd.Pedido;

import com.PCD.pcd.grpc.CozinhaResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/pedido")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Object> salvarPedido(@RequestBody Pedido pedido) {
        try {
            Pedido novoPedido = service.criarPedido(pedido);
            return ResponseEntity.status(HttpStatus.OK).body(novoPedido);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listarPedidos() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(service.listarTodos());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //Status aguardando -> em preparo
    @PutMapping("/{id}/iniciar")
    public ResponseEntity<Object> iniciarPedido(@PathVariable Long id) {
        try {
            CozinhaResponse resposta = service.iniciarPedido(id);
            if (resposta.getSucesso()) {
                return ResponseEntity.ok(Map.of(
                        "mensagem", resposta.getMensagem(),
                        "status", resposta.getNovoStatus(),
                        "pedidoId", resposta.getPedidoId()
                ));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "mensagem", resposta.getMensagem(),
                    "status", resposta.getNovoStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    //Status em preparo -> pronto
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<Object> finalizarPedido(@PathVariable Long id) {
        try {
            CozinhaResponse resposta = service.finalizarPedido(id);
            if (resposta.getSucesso()) {
                return ResponseEntity.ok(Map.of(
                        "mensagem", resposta.getMensagem(),
                        "status", resposta.getNovoStatus(),
                        "pedidoId", resposta.getPedidoId()
                ));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "mensagem", resposta.getMensagem(),
                    "status", resposta.getNovoStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //Status pronto -> entregue
    @PutMapping("/{id}/entregar")
    public ResponseEntity<Object> entregarPedido(@PathVariable Long id) {
        try {
            CozinhaResponse resposta = service.entregarPedido(id);
            if (resposta.getSucesso()) {
                return ResponseEntity.ok(Map.of(
                        "mensagem", resposta.getMensagem(),
                        "status", resposta.getNovoStatus(),
                        "pedidoId", resposta.getPedidoId()
                ));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "mensagem", resposta.getMensagem(),
                    "status", resposta.getNovoStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}