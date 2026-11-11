package br.com.baozistore.repository;

import br.com.baozistore.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    boolean existsByCliente_Id(Long clienteId);
    boolean existsByProduto_Id(Long produtoId);
}
