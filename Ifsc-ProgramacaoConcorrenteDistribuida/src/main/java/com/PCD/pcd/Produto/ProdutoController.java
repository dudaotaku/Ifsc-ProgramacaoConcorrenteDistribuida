package com.PCD.pcd.Produto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/produto")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController (ProdutoService service){
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Object> salvarProduto (@RequestBody Produto pt){
        try{
            return ResponseEntity.status(HttpStatus.OK).body(service.salvar(pt));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> listarUm (@PathVariable Long id){
        try{
            return ResponseEntity.status(HttpStatus.OK).body(service.listarUm(id));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listarProdutos(){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(service.listarProdutos());
        }catch (Exception e ){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
