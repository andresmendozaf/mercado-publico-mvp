package com.mercadopublico.mvp.dto;

public record LicitacionDTO(

    String titulo,
    String descripcion,
    Double presupuestoEstimado,
    java.time.Instant fechaCierre
) 
{}
