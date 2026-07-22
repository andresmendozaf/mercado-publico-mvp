package com.mercadopublico.mvp.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.service.LicitacionService;

@RestController
@RequestMapping("/api/licitaciones")
public class LicitacionController {

    private final LicitacionService licitacionService;

    public LicitacionController(LicitacionService licitacionService) {
        this.licitacionService = licitacionService;
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<List<Licitacion>> sincronizar() {
        return ResponseEntity.ok(licitacionService.sincronizarLicitacionesDelDia());
    }

    @GetMapping
    public ResponseEntity<List<Licitacion>> obtenerTodas() {
        return ResponseEntity.ok(licitacionService.obtenerTodas());
    }

    @GetMapping("/abiertas")
    public ResponseEntity<List<Licitacion>> obtenerAbiertas() {
        return ResponseEntity.ok(licitacionService.obtenerLicitacionesAbiertas());
    }
}