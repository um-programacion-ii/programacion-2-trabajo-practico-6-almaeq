package com.example.sistemaMicroservicios.businessService.service;

import com.example.sistemaMicroservicios.businessService.client.DataServiceClient;
import com.example.sistemaMicroservicios.businessService.dto.ProductoDTO;
import com.example.sistemaMicroservicios.businessService.exception.ProductoNoEncontradoException;
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
}
