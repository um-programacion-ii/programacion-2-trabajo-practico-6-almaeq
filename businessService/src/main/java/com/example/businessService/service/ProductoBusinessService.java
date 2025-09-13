package com.example.businessService.service;


import com.example.businessService.client.DataServiceClient;
import com.example.businessService.dto.ProductoDTO;
import com.example.businessService.dto.ProductoRequest;
import com.example.businessService.exception.MicroserviceCommunicationException;
import com.example.businessService.exception.ProductoNoEncontradoException;
import com.example.businessService.exception.ValidacionNegocioException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class ProductoBusinessService {
    private final DataServiceClient dataServiceClient;

    public ProductoBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    public List<ProductoDTO> obtenerTodosLosProductos() {
        try {
            return dataServiceClient.obtenerTodosLosProductos();
        } catch (FeignException e) {
            log.error("Error al obtener productos del microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    public ProductoDTO obtenerProductoPorId(Long id) {
        try {
            return dataServiceClient.obtenerProductoPorId(id);
        } catch (FeignException.NotFound e) {
            throw new ProductoNoEncontradoException("Producto no encontrado con ID: " + id);
        } catch (FeignException e) {
            log.error("Error al obtener producto del microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    public ProductoDTO crearProducto(ProductoRequest request) {
        validarProducto(request);

        try {
            return dataServiceClient.crearProducto(request);
        } catch (FeignException e) {
            log.error("Error al crear producto en el microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    private void validarProducto(ProductoRequest request) {
        if (request.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionNegocioException("El precio debe ser mayor a cero");
        }

        if (request.getStock() < 0) {
            throw new ValidacionNegocioException("El stock no puede ser negativo");
        }
    }

    public BigDecimal calcularValorTotalInventario() {
        log.info("Iniciando cálculo del valor total del inventario.");
        try {
            // Obtiene todos los productos desde el data-service.
            List<ProductoDTO> todosLosProductos = dataServiceClient.obtenerTodosLosProductos();

            // Usa un Stream para calcular el valor total.
            BigDecimal valorTotal = todosLosProductos.stream()
                    // Para cada producto, multiplica su precio por su stock.
                    .map(producto -> producto.getPrecio().multiply(new BigDecimal(producto.getStock())))
                    // Suma todos los subtotales.
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("Cálculo finalizado. El valor total del inventario es: {}", valorTotal);
            return valorTotal;

        } catch (FeignException e) {
            log.error("Error de comunicación al intentar obtener todos los productos para el cálculo de inventario.", e);
            throw new MicroserviceCommunicationException("No se pudo calcular el valor del inventario debido a un error de comunicación.");
        }
    }
}
