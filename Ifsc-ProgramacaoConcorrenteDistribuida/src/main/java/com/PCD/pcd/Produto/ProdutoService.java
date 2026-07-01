package com.PCD.pcd.Produto;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService (ProdutoRepository repository) {
        this.repository = repository;
    }

    public Produto salvar (Produto pt){return  repository.save(pt);}

    public Produto listarUm (Long id){
        return repository.findById(id).orElseThrow(()->new RuntimeException("Produto não encontrado"));
    }

    public List<Produto> listarProdutos (){return repository.findAll();}
}
