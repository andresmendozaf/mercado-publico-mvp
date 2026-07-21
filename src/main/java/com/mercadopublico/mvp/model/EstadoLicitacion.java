package com.mercadopublico.mvp.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EstadoLicitacion {
    PUBLICADA(5),
    CERRADA(6),
    DESIERTA(7),
    ADJUDICADA(8),
    REVOCADA(9),
    ADJUDICADA_ART3(15),
    SUSPENDIDA(18),
    OTRO(-1);

    private final int codigoApi;

    public static EstadoLicitacion desdeCodigoApi(Integer codigo) {
        if (codigo == null) return OTRO;
        return Arrays.stream(values())
                .filter(e -> e.codigoApi == codigo)
                .findFirst()
                .orElse(OTRO);
    }
}