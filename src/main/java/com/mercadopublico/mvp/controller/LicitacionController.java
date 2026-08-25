package com.mercadopublico.mvp.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mercadopublico.mvp.dto.LicitacionResponseDTO;
import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.service.LicitacionService;

@RestController
@RequestMapping("/api/licitaciones")
public class LicitacionController {

    private final LicitacionService licitacionService;

    public LicitacionController(LicitacionService licitacionService) {
        this.licitacionService = licitacionService;
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<List<LicitacionResponseDTO>> sincronizar() {
        return ResponseEntity.ok(licitacionService.sincronizarLicitacionesDelDia());
    }

    @GetMapping
    public ResponseEntity<List<LicitacionResponseDTO>> obtenerTodas(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) EstadoLicitacion estado,
            @RequestParam(required = false) String organismo) {
        return ResponseEntity.ok(licitacionService.buscarConFiltros(texto, estado, organismo));
    }

    @GetMapping("/abiertas")
    public ResponseEntity<List<LicitacionResponseDTO>> obtenerAbiertas() {
        return ResponseEntity.ok(licitacionService.obtenerLicitacionesAbiertas());
    }
}