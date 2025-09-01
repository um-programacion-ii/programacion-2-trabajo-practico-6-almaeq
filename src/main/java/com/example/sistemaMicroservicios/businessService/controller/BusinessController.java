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

/**
 * Controlador REST para el microservicio de negocio.
 * Expone la API pública y versionada (/api/v1) para los clientes finales.
 * Orquesta las llamadas a los diferentes servicios de negocio para ejecutar
 * las operaciones y lógicas de la aplicación.
 */
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

    /**
     * Obtiene el catálogo completo de productos disponibles.
     * @return ResponseEntity con una lista de ProductoDTO y estado 200 OK.
     */
    @GetMapping("/productos")
    public ResponseEntity<List<ProductoDTO>> obtenerCatalogoProductos() {
        return ResponseEntity.ok(productoBusinessService.obtenerTodosLosProductos());
    }

    /**
     * Obtiene los detalles de un producto específico por su ID.
     * @param id El ID del producto a consultar.
     * @return ResponseEntity con el ProductoDTO encontrado y estado 200 OK.
     */
    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> obtenerDetalleProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoBusinessService.obtenerProductoPorId(id));
    }

    /**
     * Registra un nuevo producto en el sistema.
     * @param request DTO con los datos del producto a crear.
     * @return ResponseEntity con el ProductoDTO recién creado y estado 201 Created.
     */
    @PostMapping("/productos")
    public ResponseEntity<ProductoDTO> registrarNuevoProducto(@RequestBody ProductoRequest request) {
        ProductoDTO nuevoProducto = productoBusinessService.crearProducto(request);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    // --- Endpoints de Categorías ---

    /**
     * Obtiene una lista de todas las categorías de productos.
     * @return ResponseEntity con una lista de CategoriaDTO y estado 200 OK.
     */
    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaDTO>> obtenerCategorias() {
        return ResponseEntity.ok(categoriaBusinessService.obtenerTodasLasCategorias());
    }

    // --- Endpoints de Inventario y Acciones de Negocio ---

    /**
     * Consulta la disponibilidad de stock para una cantidad específica de un producto.
     * Es útil para verificar si un pedido se puede realizar antes de procesarlo.
     * @param productoId El ID del producto a consultar.
     * @param cantidad La cantidad de unidades que se desea verificar.
     * @return ResponseEntity con un mapa {"disponible": true/false} y estado 200 OK.
     */
    @GetMapping("/inventario/{productoId}/disponibilidad")
    public ResponseEntity<Map<String, Boolean>> consultarDisponibilidad(
            @PathVariable Long productoId,
            @RequestParam Integer cantidad) {
        boolean disponible = inventarioBusinessService.verificarDisponibilidadStock(productoId, cantidad);
        return ResponseEntity.ok(Collections.singletonMap("disponible", disponible));
    }

    /**
     * Actualiza la cantidad de stock de un producto.
     * Se usa para registrar ventas (cantidad negativa) o reposiciones (cantidad positiva).
     * @param productoId El ID del producto cuyo stock se va a modificar.
     * @param cantidad La cantidad a sumar o restar del stock actual.
     * @return ResponseEntity con el InventarioDTO actualizado y estado 200 OK.
     */
    @PatchMapping("/inventario/{productoId}/actualizar-stock")
    public ResponseEntity<InventarioDTO> actualizarStockProducto(
            @PathVariable Long productoId,
            @RequestBody Integer cantidad) {
        InventarioDTO inventarioActualizado = inventarioBusinessService.actualizarStock(productoId, cantidad);
        return ResponseEntity.ok(inventarioActualizado);
    }

    // --- Endpoints de Reportes ---

    /**
     * Genera un reporte con el valor total monetario de todo el inventario (precio * stock).
     * @return ResponseEntity con un mapa {"valorTotal": 12345.67} y estado 200 OK.
     */
    @GetMapping("/reportes/valor-total-inventario")
    public ResponseEntity<Map<String, BigDecimal>> generarReporteValorTotalInventario() {
        BigDecimal valorTotal = productoBusinessService.calcularValorTotalInventario();
        return ResponseEntity.ok(Collections.singletonMap("valorTotal", valorTotal));
    }

    /**
     * Genera un reporte de todos los productos cuyo stock está por debajo del mínimo establecido.
     * @return ResponseEntity con una lista de InventarioDTO y estado 200 OK.
     */
    @GetMapping("/reportes/stock-bajo")
    public ResponseEntity<List<InventarioDTO>> generarReporteStockBajo() {
        return ResponseEntity.ok(inventarioBusinessService.obtenerProductosConStockBajo());
    }
}