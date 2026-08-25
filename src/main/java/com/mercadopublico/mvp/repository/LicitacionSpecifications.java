package com.mercadopublico.mvp.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.mercadopublico.mvp.model.EstadoLicitacion;
import com.mercadopublico.mvp.model.Licitacion;

public final class LicitacionSpecifications {

    private LicitacionSpecifications() {
    }

    public static Specification<Licitacion> conTexto(String texto) {
        if (!StringUtils.hasText(texto)) {
            return null;
        }
        String patron = "%" + texto.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), patron),
                cb.like(cb.lower(root.get("descripcion")), patron));
    }

    public static Specification<Licitacion> conEstado(EstadoLicitacion estado) {
        if (estado == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    public static Specification<Licitacion> conOrganismo(String organismo) {
        if (!StringUtils.hasText(organismo)) {
            return null;
        }
        String patron = "%" + organismo.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("organismoComprador")), patron);
    }
}
