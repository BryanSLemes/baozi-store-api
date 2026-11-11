package br.com.baozistore.controller;

import br.com.baozistore.dto.ProdutoRequest;
import br.com.baozistore.model.Produto;
import br.com.baozistore.repository.PedidoRepository;
import br.com.baozistore.repository.ProdutoRepository;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoRepository produtos;
    private final PedidoRepository pedidos;

    public ProdutoController(ProdutoRepository produtos, PedidoRepository pedidos) {
        this.produtos = produtos;
        this.pedidos = pedidos;
    }

    @PostMapping
    public ResponseEntity<Produto> criar(@Valid @RequestBody ProdutoRequest entrada) {
        Produto produto = produtos.save(new Produto(entrada.nome(), entrada.preco(), entrada.estoque()));
        return ResponseEntity.created(URI.create("/api/produtos/" + produto.getId())).body(produto);
    }

    @GetMapping
    public List<Produto> listar() {
        return produtos.findAll(Sort.by("id"));
    }

    @GetMapping("/{id}")
    public Produto consultar(@PathVariable Long id) {
        return buscar(id);
    }

    @PutMapping("/{id}")
    public Produto atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest entrada) {
        Produto produto = buscar(id);
        produto.setNome(entrada.nome());
        produto.setPreco(entrada.preco());
        produto.setEstoque(entrada.estoque());
        return produtos.save(produto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        Produto produto = buscar(id);
        if (pedidos.existsByProduto_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exclua os pedidos desse produto antes de exclui-lo.");
        }
        produtos.delete(produto);
        return ResponseEntity.noContent().build();
    }

    private Produto buscar(Long id) {
        return produtos.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado."));
    }
}
