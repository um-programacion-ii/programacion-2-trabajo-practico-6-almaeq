package com.example.sistemaMicroservicios.businessService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {

    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Long categoriaId;
    private Integer stock;
    private Integer stockMinimo; //Establecer cúal es el stock mínimo para futuras alertas
}