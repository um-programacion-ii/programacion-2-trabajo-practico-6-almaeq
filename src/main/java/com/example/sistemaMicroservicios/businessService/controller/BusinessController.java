package com.example.sistemaMicroservicios.businessService.controller;

import com.example.sistemaMicroservicios.businessService.dto.CategoriaDTO;
import com.example.sistemaMicroservicios.businessService.dto.InventarioDTO;
import com.example.sistemaMicroservicios.businessService.dto.ProductoDTO;
import com.example.sistemaMicroservicios.businessService.dto.ProductoRequest;
import com.example.sistemaMicroservicios.businessService.service.CategoriaBusinessService;
import com.example.sistemaMicroservicios.businessService.service.InventarioBusinessService;
import com.example.sistemaMicroservicios.businessService.service.ProductoBusinessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
public class BusinessController {

    private final ProductoBusinessService productoBusinessService;
    private final CategoriaBusinessService categoriaBusinessService;
    private final InventarioBusinessService inventarioBusinessService;

    public BusinessController(ProductoBusinessService productoBusinessService,
                              CategoriaBusinessService categoriaBusinessService,
                              InventarioBusinessService inventarioBusinessService) {
        this.productoBusinessService = productoBusinessService;
        this.categoriaBusinessService = categoriaBusinessService;
        this.inventarioBusinessService = inventarioBusinessService;
    }

    // --- Endpoints de Productos ---

    @GetMapping("/productos")
    public ResponseEntity<List<ProductoDTO>> obtenerCatalogoProductos() {
        return ResponseEntity.ok(productoBusinessService.obtenerTodosLosProductos());
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> obtenerDetalleProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoBusinessService.obtenerProductoPorId(id));
    }

    @PostMapping("/productos")
    public ResponseEntity<ProductoDTO> registrarNuevoProducto(@RequestBody ProductoRequest request) {
        ProductoDTO nuevoProducto = productoBusinessService.crearProducto(request);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    // --- Endpoints de Categorías ---

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaDTO>> obtenerCategorias() {
        return ResponseEntity.ok(categoriaBusinessService.obtenerTodasLasCategorias());
    }

    // --- Endpoints de Inventario y Acciones de Negocio ---

    @GetMapping("/inventario/{productoId}/disponibilidad")
    public ResponseEntity<Map<String, Boolean>> consultarDisponibilidad(
            @PathVariable Long productoId,
            @RequestParam Integer cantidad) {
        boolean disponible = inventarioBusinessService.verificarDisponibilidadStock(productoId, cantidad);
        return ResponseEntity.ok(Collections.singletonMap("disponible", disponible));
    }

    @PatchMapping("/inventario/{productoId}/actualizar-stock")
    public ResponseEntity<InventarioDTO> actualizarStockProducto(
            @PathVariable Long productoId,
            @RequestBody Integer cantidad) {
        InventarioDTO inventarioActualizado = inventarioBusinessService.actualizarStock(productoId, cantidad);
        return ResponseEntity.ok(inventarioActualizado);
    }

    // --- Endpoints de Reportes ---

    @GetMapping("/reportes/valor-total-inventario")
    public ResponseEntity<Map<String, BigDecimal>> generarReporteValorTotalInventario() {
        BigDecimal valorTotal = productoBusinessService.calcularValorTotalInventario();
        return ResponseEntity.ok(Collections.singletonMap("valorTotal", valorTotal));
    }

    @GetMapping("/reportes/stock-bajo")
    public ResponseEntity<List<InventarioDTO>> generarReporteStockBajo() {
        return ResponseEntity.ok(inventarioBusinessService.obtenerProductosConStockBajo());
    }
}