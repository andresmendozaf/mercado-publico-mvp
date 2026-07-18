package com.mercadopublico.mvp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mercadopublico.mvp.dto.LicitacionDTO;
import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.service.LicitacionService;

@RestController
@RequestMapping("/api/licitaciones")
public class LicitacionController {

private final LicitacionService licitacionService;

    public LicitacionController(LicitacionService licitacionService) {
        this.licitacionService = licitacionService;
    }

    /**
     * Endpoint para registrar una licitación de forma manual si lo requieres.
     */
    @PostMapping
    public ResponseEntity<Licitacion> crearLicitacion(@RequestBody LicitacionDTO dto) {
        Licitacion licitacion = new Licitacion();
        licitacion.setTitulo(dto.titulo());
        licitacion.setDescripcion(dto.descripcion());
        licitacion.setPresupuestoEstimado(dto.presupuestoEstimado());
        licitacion.setFechaCierre(dto.fechaCierre());
        
        Licitacion nuevaLicitacion = licitacionService.guardarLicitacionDirecta(licitacion);
        return new ResponseEntity<>(nuevaLicitacion, HttpStatus.CREATED);
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
