package com.boccaprime.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Categoria> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Categoria buscar(@PathVariable int id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Categoria> cadastrar(@RequestBody Categoria categoria) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(categoria));
    }

    @PutMapping("/{id}")
    public Categoria editar(@PathVariable int id, @RequestBody Categoria categoria) {
        return service.editar(id, categoria);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable int id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // Regras de negócio violadas: devolve a mensagem amigável
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> regraViolada(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }

    // Qualquer outro erro: mensagem genérica, sem stack trace na tela
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> erroInesperado(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Ocorreu um erro inesperado. Tente novamente."));
    }
}