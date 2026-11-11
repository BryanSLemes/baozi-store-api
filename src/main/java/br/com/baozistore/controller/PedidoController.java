package br.com.baozistore.controller;

import br.com.baozistore.dto.PedidoRequest;
import br.com.baozistore.dto.PedidoResponse;
import br.com.baozistore.model.Cliente;
import br.com.baozistore.model.Pedido;
import br.com.baozistore.model.Produto;
import br.com.baozistore.repository.ClienteRepository;
import br.com.baozistore.repository.PedidoRepository;
import br.com.baozistore.repository.ProdutoRepository;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoRepository pedidos;
    private final ClienteRepository clientes;
    private final ProdutoRepository produtos;

    public PedidoController(PedidoRepository pedidos, ClienteRepository clientes,
                            ProdutoRepository produtos) {
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.produtos = produtos;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest entrada) {
        Pedido pedido = pedidos.save(novoPedido(entrada));
        return ResponseEntity.created(URI.create("/api/pedidos/" + pedido.getId()))
            .body(converter(pedido));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidos.findAll(Sort.by("id")).stream().map(this::converter).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PedidoResponse consultar(@PathVariable Long id) {
        return converter(buscar(id));
    }

    @PutMapping("/{id}")
    @Transactional
    public PedidoResponse atualizar(@PathVariable Long id, @Valid @RequestBody PedidoRequest entrada) {
        Pedido pedido = buscar(id);
        Pedido novo = novoPedido(entrada);
        pedido.setCliente(novo.getCliente());
        pedido.setProduto(novo.getProduto());
        pedido.setQuantidade(novo.getQuantidade());
        return converter(pedidos.save(pedido));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        Pedido pedido = buscar(id);
        pedidos.delete(pedido);
        return ResponseEntity.noContent().build();
    }

    private Pedido novoPedido(PedidoRequest entrada) {
        Cliente cliente = clientes.findById(entrada.clienteId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado."));
        Produto produto = produtos.findById(entrada.produtoId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado."));
        if (!Boolean.TRUE.equals(produto.getEstoque())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Produto indisponivel em estoque.");
        }
        return new Pedido(cliente, produto, entrada.quantidade());
    }

    private Pedido buscar(Long id) {
        return pedidos.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado."));
    }

    private PedidoResponse converter(Pedido pedido) {
        return new PedidoResponse(pedido.getId(), pedido.getCliente().getId(),
            pedido.getProduto().getId(), pedido.getQuantidade());
    }
}
