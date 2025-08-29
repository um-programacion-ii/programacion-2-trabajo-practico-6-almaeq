package com.example.sistemaMicroservicios.dataService.service;

import com.example.sistemaMicroservicios.dataService.dto.InventarioDTO;
import com.example.sistemaMicroservicios.dataService.dto.ProductoDTO;
import com.example.sistemaMicroservicios.dataService.entity.Inventario;
import com.example.sistemaMicroservicios.dataService.exception.InventarioNoEncontradoException;
import com.example.sistemaMicroservicios.dataService.exception.ValidacionNegocioException;
import com.example.sistemaMicroservicios.dataService.repository.InventarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventarioService {
    private final InventarioRepository inventarioRepository;

    public InventarioService(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    public InventarioDTO obtenerInventarioPorProductoId(Long productoId) {
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new InventarioNoEncontradoException("No se encontró inventario para el producto con ID: " + productoId));
        return convertirAInventarioDTO(inventario);
    }

    public List<InventarioDTO> obtenerProductosConStockBajo() {
        return inventarioRepository.findByStockBajo().stream()
                .map(this::convertirAInventarioDTO)
                .collect(Collectors.toList());
    }

    public InventarioDTO actualizarStock(Long productoId, Integer cantidad) {
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new InventarioNoEncontradoException("No se encontró inventario para el producto con ID: " + productoId));

        int nuevoStock = inventario.getCantidad() + cantidad;
        if (nuevoStock < 0) {
            throw new ValidacionNegocioException("No hay stock suficiente. Stock actual: " + inventario.getCantidad() + ", se intentó restar: " + (-cantidad));
        }

        inventario.setCantidad(nuevoStock);
        inventario.setFechaActualizacion(LocalDateTime.now());
        Inventario inventarioActualizado = inventarioRepository.save(inventario);

        return convertirAInventarioDTO(inventarioActualizado);
    }

    // Método de utilidad para la conversión
    private InventarioDTO convertirAInventarioDTO(Inventario inventario) {
        // Para el DTO de Inventario, necesitamos el DTO de Producto
        ProductoDTO productoDTO = new ProductoDTO(
                inventario.getProducto().getId(),
                inventario.getProducto().getNombre(),
                inventario.getProducto().getDescripcion(),
                inventario.getProducto().getPrecio(),
                inventario.getProducto().getCategoria().getNombre(),
                inventario.getCantidad(),
                inventario.getCantidad() <= inventario.getStockMinimo()
        );

        return new InventarioDTO(
                inventario.getId(),
                productoDTO,
                inventario.getCantidad(),
                inventario.getStockMinimo(),
                inventario.getFechaActualizacion()
        );
    }
}
