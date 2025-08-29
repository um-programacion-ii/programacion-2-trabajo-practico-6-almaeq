package com.example.sistemaMicroservicios.businessService.service;

import com.example.sistemaMicroservicios.businessService.client.DataServiceClient;
import com.example.sistemaMicroservicios.businessService.dto.InventarioDTO;
import com.example.sistemaMicroservicios.businessService.exception.InventarioNoEncontradoException;
import com.example.sistemaMicroservicios.businessService.exception.MicroserviceCommunicationException;
import com.example.sistemaMicroservicios.businessService.exception.ProductoNoEncontradoException;
import com.example.sistemaMicroservicios.businessService.exception.ValidacionNegocioException;
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

    public boolean verificarDisponibilidadStock(Long productoId, Integer cantidadSolicitada) {
        if (cantidadSolicitada <= 0) {
            throw new ValidacionNegocioException("La cantidad a verificar debe ser mayor que cero.");
        }
        try {
            log.info("BussinessService: Verificando disponibilidad para producto ID {} (cantidad: {})", productoId, cantidadSolicitada);
            InventarioDTO inventario = dataServiceClient.obtenerInventarioPorProductoId(productoId);
            return inventario.getCantidad() >= cantidadSolicitada;
        } catch (FeignException.NotFound e) {
            log.warn("Se intentó verificar el stock de un producto inexistente (ID: {})", productoId);
            return false; // Si el producto no existe, no hay disponibilidad.
        } catch (FeignException e) {
            log.error("Error de comunicación al verificar stock.", e);
            throw new MicroserviceCommunicationException("Error al verificar la disponibilidad del stock.");
        }
    }

    public InventarioDTO actualizarStock(Long productoId, Integer cantidad) {
        if (cantidad == 0) {
            throw new ValidacionNegocioException("La cantidad para actualizar el stock no puede ser cero.");
        }
        try {
            log.info("BussinessService: Actualizando stock para producto ID {} (cantidad: {})", productoId, cantidad);
            return dataServiceClient.actualizarStock(productoId, cantidad);
        } catch (FeignException.NotFound e) {
            throw new ProductoNoEncontradoException("No se puede actualizar stock, producto no encontrado con ID: " + productoId);
        } catch (FeignException e) {
            if (e.status() == 400) { // El data-service responde 400 si no hay stock
                throw new ValidacionNegocioException("No hay stock suficiente para realizar la operación.");
            }
            log.error("Error de comunicación al actualizar stock.", e);
            throw new MicroserviceCommunicationException("Error de comunicación al actualizar el stock.");
        }
    }
}







