package br.com.baozistore.controller;

import br.com.baozistore.dto.ClienteRequest;
import br.com.baozistore.model.Cliente;
import br.com.baozistore.repository.ClienteRepository;
import br.com.baozistore.repository.PedidoRepository;
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
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteRepository clientes;
    private final PedidoRepository pedidos;

    public ClienteController(ClienteRepository clientes, PedidoRepository pedidos) {
        this.clientes = clientes;
        this.pedidos = pedidos;
    }

    @PostMapping
    public ResponseEntity<Cliente> criar(@Valid @RequestBody ClienteRequest entrada) {
        Cliente cliente = clientes.save(new Cliente(entrada.nome(), entrada.clienteDesde()));
        return ResponseEntity.created(URI.create("/api/clientes/" + cliente.getId())).body(cliente);
    }

    @GetMapping
    public List<Cliente> listar() {
        return clientes.findAll(Sort.by("id"));
    }

    @GetMapping("/{id}")
    public Cliente consultar(@PathVariable Long id) {
        return buscar(id);
    }

    @PutMapping("/{id}")
    public Cliente atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest entrada) {
        Cliente cliente = buscar(id);
        cliente.setNome(entrada.nome());
        cliente.setClienteDesde(entrada.clienteDesde());
        return clientes.save(cliente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        Cliente cliente = buscar(id);
        if (pedidos.existsByCliente_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exclua os pedidos deste cliente antes de exclui-lo.");
        }
        clientes.delete(cliente);
        return ResponseEntity.noContent().build();
    }

    private Cliente buscar(Long id) {
        return clientes.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado."));
    }
}
