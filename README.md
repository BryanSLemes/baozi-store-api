# Baozi Store API — UNINTER

API REST para cadastro de clientes, produtos e pedidos simples, elaborada para a atividade prática de Desenvolvimento Web Back-End da UNINTER por Bryan Silva Lemes (RU 5262978).

## Tecnologias
Java 17+, Spring Boot 3.5.16, Spring Web, Spring Data JPA, Bean Validation e banco H2 persistente em arquivo.

## Requisitos
- JDK 17 ou superior.
- Apache Maven 3.6.3+ (ou uma IDE com integração Maven).
- Postman para executar e registrar as capturas solicitadas.

## Como executar

No terminal da pasta que contém o `pom.xml`, confirme que o Maven usa o JDK correto e execute:

```powershell
mvn -version
mvn -B clean verify
java -jar target/baozi-store-api-1.0.0.jar
```

O build gera um JAR executável. Também é possível iniciar com `mvn spring-boot:run` ou usar os scripts `testar_windows.bat` e `executar_windows.bat` no Windows com o Maven no PATH.

A API escuta em `http://localhost:8080`. O H2 cria `baozi-store-db.mv.db` no diretório de execução. O Hibernate mantém o esquema com `ddl-auto=update` e o H2 grava com `WRITE_DELAY=0`. Os testes usam um banco H2 em memória, separado do arquivo da aplicação.

## Coleção Postman

1. No aplicativo Postman, importe `postman/Baozi_Store.postman_collection.json`.
2. Confira `baseUrl` nas variáveis da coleção. O valor padrão é `http://localhost:8080`.
3. Envie as 12 requisições numeradas na ordem. Os POSTs guardam os IDs gerados e as operações seguintes reutilizam esses valores. O pedido é excluído antes do produto e do cliente.
4. Para o relatório acadêmico, capture método, URL e status em todas as operações. Nos POSTs e GETs, mostre o JSON aplicável.

Para uma execução automatizada com Node.js/npm:

```powershell
npx --yes newman@6.2.1 run postman/Baozi_Store.postman_collection.json --iteration-count 2
```

## Endpoints
| Entidade | POST | GET lista | GET por ID | PUT por ID | DELETE por ID |
|---|---|---|---|---|---|
| Clientes | `/api/clientes` | `/api/clientes` | `/api/clientes/{id}` | `/api/clientes/{id}` | `/api/clientes/{id}` |
| Produtos | `/api/produtos` | `/api/produtos` | `/api/produtos/{id}` | `/api/produtos/{id}` | `/api/produtos/{id}` |
| Pedidos | `/api/pedidos` | `/api/pedidos` | `/api/pedidos/{id}` | `/api/pedidos/{id}` | `/api/pedidos/{id}` |

Os corpos são JSON. **POST** retorna HTTP 201, **GET/PUT** retorna HTTP 200, **DELETE** retorna HTTP 204. Entradas inválidas retornam 400, inexistentes 404 e regras de integridade retornam 409.

## Exemplos de corpo
Cliente:
```json
{"nome":"Bryan Silva Lemes5262978","clienteDesde":"2026-11-09"}
```
Produto:
```json
{"nome":"Baozi tradicional","preco":10.00,"estoque":true}
```
Pedido no Postman (os POSTs de Cliente e Produto preenchem as variáveis):
```json
{"clienteId":{{clienteId}},"produtoId":{{produtoId}},"quantidade":3}
```

### Relacionamento
Cada pedido possui exatamente um cliente e um produto. Um mesmo cliente ou produto pode aparecer em vários pedidos. As relações são representadas por chaves estrangeiras JPA. A quantidade deve ser um inteiro positivo. O estoque é booleano e representa disponibilidade. Produtos indisponíveis não podem ser usados para criar ou atualizar pedidos. Exclua os pedidos vinculados antes de excluir seus clientes ou produtos.

Os nomes aceitam até 150 caracteres. O preço mínimo é 0,01, com até oito dígitos inteiros e duas casas decimais. Consulte o diagrama em [docs/casos_de_uso.svg](docs/casos_de_uso.svg).

## Entrega acadêmica
O PDF do relatório deve conter as 12 capturas do Postman com o cenário da coleção e um link acessível para o repositório publicado.

Para publicar no GitHub, crie um repositório e execute na pasta do projeto:
```bash
git init
git add .
git commit -m "Atividade pratica - Baozi Store API"
git branch -M main
git remote add origin https://github.com/SEU_USUARIO/SEU_REPOSITORIO.git
git push -u origin main
```
Substitua a URL pelo endereço de um repositório que você realmente criou.
