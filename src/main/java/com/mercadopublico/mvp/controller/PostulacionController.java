package com.mercadopublico.mvp.controller;

import com.mercadopublico.mvp.dto.PostulacionDTO;
import com.mercadopublico.mvp.model.EstadoPostulacion;
import com.mercadopublico.mvp.service.PostulacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/postulaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class PostulacionController {

    private final PostulacionService postulacionService;

    /**
     * POST /api/postulaciones
     */
    @PostMapping
    public ResponseEntity<PostulacionDTO> crearPostulacion(@RequestBody PostulacionDTO postulacionDTO) {
        // Cambiado de guardarPostulacion a crearPostulacion
        PostulacionDTO nuevaPostulacion = postulacionService.crearPostulacion(postulacionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPostulacion);
    }

    /**
     * GET /api/postulaciones/usuario/{proveedorId}
     */
    @GetMapping("/usuario/{proveedorId}")
    public ResponseEntity<List<PostulacionDTO>> obtenerPorProveedor(@PathVariable Long proveedorId) {
        // Cambiado a obtenerPorProveedor
        List<PostulacionDTO> postulaciones = postulacionService.obtenerPorProveedor(proveedorId);
        return ResponseEntity.ok(postulaciones);
    }

    /**
     * Opcional: GET para filtrar por Proveedor y Estado (útil para columnas del Kanban)
     * GET /api/postulaciones/usuario/{proveedorId}/estado?estado=POR_ESTUDIAR
     */
    @GetMapping("/usuario/{proveedorId}/estado")
    public ResponseEntity<List<PostulacionDTO>> obtenerPorProveedorYEstado(
            @PathVariable Long proveedorId,
            @RequestParam EstadoPostulacion estado) {
        List<PostulacionDTO> postulaciones = postulacionService.obtenerPorProveedorYEstado(proveedorId, estado);
        return ResponseEntity.ok(postulaciones);
    }

    /**
     * PATCH /api/postulaciones/{id}/estado?nuevoEstado=POSTULADA
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PostulacionDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPostulacion nuevoEstado) {
        PostulacionDTO postulacionActualizada = postulacionService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(postulacionActualizada);
    }

    /**
     * DELETE /api/postulaciones/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPostulacion(@PathVariable Long id) {
        postulacionService.eliminarPostulacion(id);
        return ResponseEntity.noContent().build();
    }
}