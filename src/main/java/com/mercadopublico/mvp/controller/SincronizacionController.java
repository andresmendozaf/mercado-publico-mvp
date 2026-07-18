package com.mercadopublico.mvp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.service.SincronizacionService;

@RestController
@RequestMapping("/api/sincronizacion")
public class SincronizacionController {

private final SincronizacionService sincronizacionService;

    public SincronizacionController(SincronizacionService sincronizacionService) {
        this.sincronizacionService = sincronizacionService;
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Licitacion>> buscarYGuardarNecesidades(
            @RequestParam String fecha,
            @RequestParam String ticket) {
        
        List<Licitacion> resultados = sincronizacionService.buscarYSincronizar(fecha, ticket);
        return ResponseEntity.ok(resultados);
    }
}
