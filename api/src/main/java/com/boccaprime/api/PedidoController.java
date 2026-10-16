package com.boccaprime.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    // Pedidos de hoje (RF10)
    @GetMapping
    public List<Pedido> listarDoDia() {
        return service.listarDoDia();
    }

    // Detalhe de um pedido com seus itens (RF11)
    @GetMapping("/{id}")
    public Pedido buscar(@PathVariable int id) {
        return service.buscar(id);
    }

    // Confirmar pedido no totem (RF20)
    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody Pedido pedido) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(pedido));
    }

    // Avançar o status (RF12)
    @PatchMapping("/{id}/avancar")
    public Pedido avancar(@PathVariable int id) {
        return service.avancarStatus(id);
    }

    // Cancelar (RF12)
    @PatchMapping("/{id}/cancelar")
    public Pedido cancelar(@PathVariable int id) {
        return service.cancelar(id);
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