package com.example.sistemaMicroservicios.dataService.controller;

import com.example.sistemaMicroservicios.dataService.dto.CategoriaDTO;
import com.example.sistemaMicroservicios.dataService.dto.InventarioDTO;
import com.example.sistemaMicroservicios.dataService.dto.ProductoDTO;
import com.example.sistemaMicroservicios.dataService.dto.ProductoRequest;
import com.example.sistemaMicroservicios.dataService.service.CategoriaService;
import com.example.sistemaMicroservicios.dataService.service.InventarioService;
import com.example.sistemaMicroservicios.dataService.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/data")
public class DataController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final InventarioService inventarioService;

    public DataController(ProductoService productoService,
                          CategoriaService categoriaService,
                          InventarioService inventarioService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.inventarioService = inventarioService;
    }

    // --- Endpoints de Productos ---

    @GetMapping("/productos")
    public ResponseEntity<List<ProductoDTO>> obtenerTodosLosProductos() {
        return ResponseEntity.ok(productoService.obtenerTodosLosProductos());
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> obtenerProductoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerProductoPorId(id));
    }

    @PostMapping("/productos")
    public ResponseEntity<ProductoDTO> crearProducto(@RequestBody ProductoRequest request) {
        ProductoDTO nuevoProducto = productoService.crearProducto(request);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(@PathVariable Long id, @RequestBody ProductoRequest request) {
        ProductoDTO productoActualizado = productoService.actualizarProducto(id, request);
        return ResponseEntity.ok(productoActualizado);
    }

    @DeleteMapping("/productos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
    }

    @GetMapping(value = "/productos", params = "categoria")
    public ResponseEntity<List<ProductoDTO>> obtenerProductosPorCategoria(@RequestParam("categoria") String nombre) {
        return ResponseEntity.ok(productoService.buscarPorCategoriaNombre(nombre));
    }

    // --- Endpoints de Categorías ---

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaDTO>> obtenerTodasLasCategorias() {
        return ResponseEntity.ok(categoriaService.obtenerTodas());
    }

    @PostMapping("/categorias")
    public ResponseEntity<CategoriaDTO> crearCategoria(@RequestBody CategoriaDTO request) {
        CategoriaDTO nuevaCategoria = categoriaService.crearCategoria(request);
        return new ResponseEntity<>(nuevaCategoria, HttpStatus.CREATED);
    }

    @PutMapping("/categorias/{id}")
    public ResponseEntity<CategoriaDTO> actualizarCategoria(@PathVariable Long id, @RequestBody CategoriaDTO request) {
        CategoriaDTO categoriaActualizada = categoriaService.actualizarCategoria(id, request);
        return ResponseEntity.ok(categoriaActualizada);
    }

    @DeleteMapping("/categorias/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrarCategoriaPorId(@PathVariable Long id) {
        categoriaService.borrarPorId(id);
    }

    // --- Endpoints de Inventario ---

    @GetMapping("/inventario/stock-bajo")
    public ResponseEntity<List<InventarioDTO>> obtenerProductosConStockBajo() {
        return ResponseEntity.ok(inventarioService.obtenerProductosConStockBajo());
    }


    @GetMapping("/inventario/{productoId}")
    public ResponseEntity<InventarioDTO> obtenerInventarioPorProductoId(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.obtenerInventarioPorProductoId(productoId));
    }
    @PatchMapping("/inventario/{productoId}")
    public ResponseEntity<InventarioDTO> actualizarStock(@PathVariable Long productoId, @RequestBody Integer cantidad) {
        InventarioDTO inventarioActualizado = inventarioService.actualizarStock(productoId, cantidad);
        return ResponseEntity.ok(inventarioActualizado);
    }

}