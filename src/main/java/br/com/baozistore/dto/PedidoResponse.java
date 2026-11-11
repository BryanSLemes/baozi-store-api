package br.com.baozistore.dto;

public record PedidoResponse(
    Long id,
    Long clienteId,
    Long produtoId,
    Integer quantidade
) { }
