package com.mercadopublico.mvp.repository;

import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.EstadoPostulacion;
import com.mercadopublico.mvp.model.Licitacion;
import com.mercadopublico.mvp.model.Postulacion;
import com.mercadopublico.mvp.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
class PostulacionRepositoryTest {

    @Autowired
    private PostulacionRepository postulacionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Usuario nuevoProveedor(String runOId, String email) {
        Usuario usuario = new Usuario();
        usuario.setRunOId(runOId);
        usuario.setNombre("Proveedor de prueba");
        usuario.setEmail(email);
        usuario.setRol("PROVEEDOR");
        return entityManager.persistAndFlush(usuario);
    }

    private Licitacion nuevaLicitacion(String codigoExterno) {
        Licitacion licitacion = new Licitacion();
        licitacion.setCodigoExterno(codigoExterno);
        licitacion.setEstado(EstadoLicitacion.PUBLICADA);
        return entityManager.persistAndFlush(licitacion);
    }

    private Postulacion nuevaPostulacion(Usuario proveedor, Licitacion licitacion) {
        Postulacion postulacion = new Postulacion();
        postulacion.setProveedor(proveedor);
        postulacion.setLicitacion(licitacion);
        postulacion.setMontoPostulacion(1000000.0);
        postulacion.setPropuestaTecnica("Propuesta de prueba");
        return postulacion;
    }

    @Test
    void existsByProveedorIdAndLicitacionId_detectaSoloLaCombinacionPersistida() {
        Usuario proveedorA = nuevoProveedor("11111111-1", "proveedorA@test.cl");
        Usuario proveedorB = nuevoProveedor("22222222-2", "proveedorB@test.cl");
        Licitacion licitacionA = nuevaLicitacion("LIC-A");
        Licitacion licitacionB = nuevaLicitacion("LIC-B");

        entityManager.persistAndFlush(nuevaPostulacion(proveedorA, licitacionA));

        assertThat(postulacionRepository.existsByProveedorIdAndLicitacionId(proveedorA.getId(), licitacionA.getId()))
                .as("mismo proveedor + misma licitación")
                .isTrue();

        assertThat(postulacionRepository.existsByProveedorIdAndLicitacionId(proveedorA.getId(), licitacionB.getId()))
                .as("mismo proveedor + distinta licitación")
                .isFalse();

        assertThat(postulacionRepository.existsByProveedorIdAndLicitacionId(proveedorB.getId(), licitacionA.getId()))
                .as("distinto proveedor + misma licitación")
                .isFalse();

        assertThat(postulacionRepository.existsByProveedorIdAndLicitacionId(proveedorB.getId(), licitacionB.getId()))
                .as("combinación sin postulación registrada")
                .isFalse();
    }

    @Test
    void guardarPostulacionSinMontoNiPropuesta_debePersistirConCamposNulosYEstadoPorEstudiar() {
        Usuario proveedor = nuevoProveedor("44444444-4", "proveedorD@test.cl");
        Licitacion licitacion = nuevaLicitacion("LIC-D");

        Postulacion postulacion = new Postulacion();
        postulacion.setProveedor(proveedor);
        postulacion.setLicitacion(licitacion);
        // montoPostulacion y propuestaTecnica quedan intencionalmente en null

        Postulacion guardada = postulacionRepository.saveAndFlush(postulacion);

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getMontoPostulacion()).isNull();
        assertThat(guardada.getPropuestaTecnica()).isNull();
        assertThat(guardada.getEstado()).isEqualTo(EstadoPostulacion.POR_ESTUDIAR);
    }

    @Test
    void guardarPostulacionDuplicada_violaConstraintUnicoProveedorLicitacion() {
        Usuario proveedor = nuevoProveedor("33333333-3", "proveedorC@test.cl");
        Licitacion licitacion = nuevaLicitacion("LIC-C");

        postulacionRepository.saveAndFlush(nuevaPostulacion(proveedor, licitacion));

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> postulacionRepository.saveAndFlush(nuevaPostulacion(proveedor, licitacion)));
    }
}
