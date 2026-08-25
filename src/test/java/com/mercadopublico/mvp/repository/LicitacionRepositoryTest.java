package com.mercadopublico.mvp.repository;

import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.Licitacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LicitacionRepositoryTest {

    @Autowired
    private LicitacionRepository licitacionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Licitacion nuevaLicitacion(String codigoExterno, String nombre, EstadoLicitacion estado,
            String organismoComprador) {
        Licitacion licitacion = new Licitacion();
        licitacion.setCodigoExterno(codigoExterno);
        licitacion.setNombre(nombre);
        licitacion.setEstado(estado);
        licitacion.setOrganismoComprador(organismoComprador);
        return entityManager.persistAndFlush(licitacion);
    }

    @Test
    void findAll_ConEstadoYOrganismoCombinados_SoloRetornaCoincidenciasDeAmbosFiltros() {
        nuevaLicitacion("LIC-1", "Adquisición de Servidores", EstadoLicitacion.PUBLICADA, "Ministerio de Defensa");
        nuevaLicitacion("LIC-2", "Adquisición de Notebooks", EstadoLicitacion.CERRADA, "Ministerio de Defensa");
        nuevaLicitacion("LIC-3", "Servicio de Aseo", EstadoLicitacion.PUBLICADA, "Municipalidad de Santiago");

        Specification<Licitacion> specification = Specification
                .where(LicitacionSpecifications.conEstado(EstadoLicitacion.PUBLICADA))
                .and(LicitacionSpecifications.conOrganismo("defensa"));

        List<Licitacion> resultado = licitacionRepository.findAll(specification);

        assertThat(resultado)
                .extracting(Licitacion::getCodigoExterno)
                .containsExactly("LIC-1");
    }
}
