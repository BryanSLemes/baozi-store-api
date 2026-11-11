package br.com.baozistore;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BaoziStoreApplicationTests {
    private static final String CLIENTE = """
        {"nome":"Bryan Silva Lemes5262978","clienteDesde":"2026-11-09"}
        """;
    private static final String PRODUTO = """
        {"nome":"Baozi tradicional","preco":10.00,"estoque":true}
        """;

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void fluxoCompletoDeClientesProdutosEPedidos() throws Exception {
        String respostaCliente = mvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CLIENTE))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Bryan Silva Lemes5262978"))
            .andExpect(jsonPath("$.clienteDesde").value("2026-11-09"))
            .andReturn().getResponse().getContentAsString();
        long clienteId = mapper.readTree(respostaCliente).get("id").asLong();

        String respostaProduto = mvc.perform(post("/api/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PRODUTO))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Baozi tradicional"))
            .andExpect(jsonPath("$.preco").value(10.00))
            .andExpect(jsonPath("$.estoque").value(true))
            .andReturn().getResponse().getContentAsString();
        long produtoId = mapper.readTree(respostaProduto).get("id").asLong();

        String pedidoJson = pedido(clienteId, produtoId, 3);
        String respostaPedido = mvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON).content(pedidoJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.clienteId").value(clienteId))
            .andExpect(jsonPath("$.produtoId").value(produtoId))
            .andExpect(jsonPath("$.quantidade").value(3))
            .andReturn().getResponse().getContentAsString();
        long pedidoId = mapper.readTree(respostaPedido).get("id").asLong();

        mvc.perform(get("/api/clientes")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(clienteId));
        mvc.perform(get("/api/produtos")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(produtoId));
        mvc.perform(get("/api/pedidos")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(pedidoId));
        mvc.perform(get("/api/clientes/{id}", clienteId)).andExpect(status().isOk());
        mvc.perform(get("/api/produtos/{id}", produtoId)).andExpect(status().isOk());
        mvc.perform(get("/api/pedidos/{id}", pedidoId)).andExpect(status().isOk());

        mvc.perform(put("/api/clientes/{id}", clienteId).contentType(MediaType.APPLICATION_JSON)
                .content(CLIENTE.replace("2026-11-09", "2026-11-08")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.clienteDesde").value("2026-11-08"));
        mvc.perform(put("/api/produtos/{id}", produtoId).contentType(MediaType.APPLICATION_JSON)
                .content(PRODUTO.replace("10.00", "12.50")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.preco").value(12.50));
        mvc.perform(put("/api/pedidos/{id}", pedidoId).contentType(MediaType.APPLICATION_JSON)
                .content(pedido(clienteId, produtoId, 4)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(4));
        mvc.perform(get("/api/pedidos/{id}", pedidoId)).andExpect(jsonPath("$.quantidade").value(4));

        // Um cliente ou produto referenciado por pedido não pode ser excluído.
        mvc.perform(delete("/api/clientes/{id}", clienteId)).andExpect(status().isConflict());
        mvc.perform(delete("/api/produtos/{id}", produtoId)).andExpect(status().isConflict());

        mvc.perform(delete("/api/pedidos/{id}", pedidoId)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/produtos/{id}", produtoId)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/clientes/{id}", clienteId)).andExpect(status().isNoContent());
        mvc.perform(get("/api/pedidos/{id}", pedidoId)).andExpect(status().isNotFound());
        mvc.perform(get("/api/produtos/{id}", produtoId)).andExpect(status().isNotFound());
        mvc.perform(get("/api/clientes/{id}", clienteId)).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "3.5", "null"})
    void rejeitaPedidoComQuantidadeInvalida(String quantidade) throws Exception {
        long clienteId = criar("/api/clientes", CLIENTE);
        long produtoId = criar("/api/produtos", PRODUTO);
        mvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedido(clienteId, produtoId, 3).replace("\"quantidade\":3", "\"quantidade\":" + quantidade)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaNomesInvalidosEDataAusente() throws Exception {
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Bryan Silva Lemes5262978\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                .content(CLIENTE.replace("Bryan Silva Lemes5262978", " ")))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                .content(CLIENTE.replace("Bryan Silva Lemes5262978", "A".repeat(151))))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON)
                .content(PRODUTO.replace("Baozi tradicional", "A".repeat(151))))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "10.001", "100000000.00"})
    void rejeitaPrecosForaDosLimites(String preco) throws Exception {
        mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON)
                .content(PRODUTO.replace("10.00", preco)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaEstoqueAusente() throws Exception {
        mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON)
                .content(PRODUTO.replace("true", "null")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaPedidoComClienteOuProdutoInexistente() throws Exception {
        long clienteId = criar("/api/clientes", CLIENTE);
        long produtoId = criar("/api/produtos", PRODUTO);
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content(pedido(Long.MAX_VALUE, produtoId, 3)))
            .andExpect(status().isNotFound());
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content(pedido(clienteId, Long.MAX_VALUE, 3)))
            .andExpect(status().isNotFound());
    }

    @Test
    void rejeitaPedidoDeProdutoIndisponivel() throws Exception {
        long clienteId = criar("/api/clientes", CLIENTE);
        long produtoId = criar("/api/produtos", PRODUTO.replace("true", "false"));
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content(pedido(clienteId, produtoId, 3)))
            .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/clientes", "/api/produtos", "/api/pedidos"})
    void retorna404AoConsultarOuExcluirRegistroInexistente(String rota) throws Exception {
        mvc.perform(get(rota + "/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
        mvc.perform(delete(rota + "/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    private long criar(String rota, String json) throws Exception {
        String resposta = mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(resposta).get("id").asLong();
    }

    private String pedido(long clienteId, long produtoId, int quantidade) {
        return "{\"clienteId\":%d,\"produtoId\":%d,\"quantidade\":%d}"
            .formatted(clienteId, produtoId, quantidade);
    }
}
