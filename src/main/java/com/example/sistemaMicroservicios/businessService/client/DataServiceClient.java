package com.example.sistemaMicroservicios.businessService.client;

import com.example.sistemaMicroservicios.businessService.dto.CategoriaDTO;
import com.example.sistemaMicroservicios.businessService.dto.InventarioDTO;
import com.example.sistemaMicroservicios.businessService.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "data-service", url = "${data.service.url}")
public interface DataServiceClient {

    @GetMapping("/data/productos")
    List<ProductoDTO> obtenerTodosLosProductos();

    @GetMapping("/data/productos/{id}")
    ProductoDTO obtenerProductoPorId(@PathVariable Long id);

    @PostMapping("/data/productos")
    ProductoDTO crearProducto(@RequestBody ProductoRequest request);

    @PutMapping("/data/productos/{id}")
    ProductoDTO actualizarProducto(@PathVariable Long id, @RequestBody ProductoRequest request);

    @DeleteMapping("/data/productos/{id}")
    void eliminarProducto(@PathVariable Long id);

    @GetMapping("/data/productos/categoria/{nombre}")
    List<ProductoDTO> obtenerProductosPorCategoria(@PathVariable String nombre);

    @GetMapping("/data/categorias")
    List<CategoriaDTO> obtenerTodasLasCategorias();

    @GetMapping("/data/inventario/stock-bajo")
    List<InventarioDTO> obtenerProductosConStockBajo();

    @GetMapping("/data/inventario/{productoId}")
    InventarioDTO obtenerInventarioPorProductoId(@PathVariable Long productoId);
}
