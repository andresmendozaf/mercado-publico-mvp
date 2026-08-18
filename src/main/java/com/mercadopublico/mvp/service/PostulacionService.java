package com.mercadopublico.mvp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercadopublico.mvp.dto.PostulacionDTO;
import com.mercadopublico.mvp.exception.RecursoNoEncontradoException;
import com.mercadopublico.mvp.mapper.PostulacionMapper;
import com.mercadopublico.mvp.model.EstadoPostulacion;
import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.model.Postulacion;
import com.mercadopublico.mvp.model.Usuario;
import com.mercadopublico.mvp.repository.LicitacionRepository;
import com.mercadopublico.mvp.repository.PostulacionRepository;
import com.mercadopublico.mvp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostulacionService {

    private final PostulacionRepository postulacionRepository;
    private final LicitacionRepository licitacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PostulacionMapper postulacionMapper;

    @Transactional
    public PostulacionDTO crearPostulacion(PostulacionDTO dto) {
        log.info("Creando postulación. Lic ID: {}, Proveedor ID: {}", dto.licitacionId(), dto.proveedorId());

        Licitacion licitacion = licitacionRepository.findById(dto.licitacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Licitación no encontrada: " + dto.licitacionId()));

        Usuario proveedor = usuarioRepository.findById(dto.proveedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + dto.proveedorId()));

        Postulacion nuevaPostulacion = postulacionMapper.toEntity(dto, licitacion, proveedor);
        Postulacion guardada = postulacionRepository.save(nuevaPostulacion);

        return postulacionMapper.toDTO(guardada);
    }

    public List<PostulacionDTO> obtenerPorProveedor(Long proveedorId) {
        return postulacionRepository.findByProveedorId(proveedorId)
                .stream()
                .map(postulacionMapper::toDTO)
                .toList();
    }

    public List<PostulacionDTO> obtenerPorProveedorYEstado(Long proveedorId, EstadoPostulacion estado) {
        return postulacionRepository.findByProveedorIdAndEstado(proveedorId, estado)
                .stream()
                .map(postulacionMapper::toDTO)
                .toList();
    }

    @Transactional
    public PostulacionDTO cambiarEstado(Long postulacionId, EstadoPostulacion nuevoEstado) {
        Postulacion postulacion = postulacionRepository.findById(postulacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Postulación no encontrada: " + postulacionId));

        postulacion.setEstado(nuevoEstado);
        log.info("Postulación ID {} actualizada a estado: {}", postulacionId, nuevoEstado);

        return postulacionMapper.toDTO(postulacion);
    }

    @Transactional
    public void eliminarPostulacion(Long id) {
        if (!postulacionRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Postulación no encontrada: " + id);
        }
        postulacionRepository.deleteById(id);
    }
}