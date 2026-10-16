package com.boccaprime.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    // Exemplos: /produtos   /produtos?categoriaId=1   /produtos?busca=burger
    @GetMapping
    public List<Produto> listar(@RequestParam(required = false) Integer categoriaId,
                                @RequestParam(required = false) String busca) {
        return service.listar(categoriaId, busca);
    }

    @GetMapping("/{id}")
    public Produto buscar(@PathVariable int id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Produto> cadastrar(@RequestBody Produto produto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(produto));
    }

    @PutMapping("/{id}")
    public Produto editar(@PathVariable int id, @RequestBody Produto produto) {
        return service.editar(id, produto);
    }

    // Ativar ou desativar sem excluir (RF09)
    @PatchMapping("/{id}/disponivel")
    public Produto alterarDisponibilidade(@PathVariable int id, @RequestParam boolean valor) {
        return service.alterarDisponibilidade(id, valor);
    }

    // Exclui de verdade ou, se já foi vendido, só marca como indisponível (RF08)
    @DeleteMapping("/{id}")
    public Map<String, Object> excluir(@PathVariable int id) {
        boolean excluido = service.excluir(id);
        String mensagem = excluido
                ? "Produto excluído com sucesso."
                : "Este produto já foi vendido, então foi apenas marcado como indisponível.";
        return Map.of("excluido", excluido, "mensagem", mensagem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> regraViolada(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> erroInesperado(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Ocorreu um erro inesperado. Tente novamente."));
    }
}