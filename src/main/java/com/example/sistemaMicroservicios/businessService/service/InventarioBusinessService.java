package com.example.sistemaMicroservicios.businessService.service;

import com.example.sistemaMicroservicios.businessService.client.DataServiceClient;
import com.example.sistemaMicroservicios.businessService.dto.InventarioDTO;
import com.example.sistemaMicroservicios.businessService.exception.InventarioNoEncontradoException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InventarioBusinessService {

    private final DataServiceClient dataServiceClient;

    public InventarioBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    public List<InventarioDTO> obtenerProductosConStockBajo() {
        try {
            log.info("Iniciando llamada a data-service para obtener productos con stock bajo.");
            List<InventarioDTO> inventario = dataServiceClient.obtenerProductosConStockBajo();
            log.info("Llamada a data-service exitosa. Se encontraron {} productos con stock bajo.", inventario.size());
            return inventario;
        } catch (FeignException e) {
            log.error("Error al comunicarse con el microservicio de datos para obtener el inventario: {}", e.getMessage());
            throw new MicroserviceCommunicationException("Error de comunicación al intentar obtener productos con stock bajo.");
        }
    }

    public InventarioDTO obtenerInventarioPorProductoId(Long productoId) {
        try {
            log.info("Consultando inventario para el producto ID: {}", productoId);
            return dataServiceClient.obtenerInventarioPorProductoId(productoId);
        } catch (FeignException.NotFound e) {
            log.error("Inventario no encontrado para el producto ID: {}", productoId);
            throw new InventarioNoEncontradoException("No se encontró inventario para el producto con ID " + productoId);
        } catch (FeignException e) {
            log.error("Error de comunicación al obtener el inventario: {}", e.getMessage());
            throw new MicroserviceCommunicationException("Error de comunicación al obtener el inventario.");
        }
    }
}







