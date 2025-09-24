package com.example.businessService.client;


import com.example.businessService.dto.CategoriaDTO;
import com.example.businessService.dto.InventarioDTO;
import com.example.businessService.dto.ProductoDTO;
import com.example.businessService.dto.ProductoRequest;
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

    @PostMapping("/data/categorias")
    CategoriaDTO crearCategoria(@RequestBody CategoriaDTO categoriaDTO);

    @GetMapping(value = "/data/productos", params = "categoria")
    List<ProductoDTO> obtenerProductosPorCategoria(@RequestParam("categoria") String nombre);

    @GetMapping("/data/categorias")
    List<CategoriaDTO> obtenerTodasLasCategorias();

    @PutMapping("/data/categorias/{id}")
    CategoriaDTO actualizarCategoria(@PathVariable Long id, @RequestBody CategoriaDTO categoriaDTO);

    @DeleteMapping("/data/categorias/{id}")
    void eliminarCategoria(@PathVariable Long id);

    @GetMapping("/data/inventario/stock-bajo")
    List<InventarioDTO> obtenerProductosConStockBajo();

    @GetMapping("/data/inventario/{productoId}")
    InventarioDTO obtenerInventarioPorProductoId(@PathVariable Long productoId);

    @PutMapping("/data/inventario/{productoId}")
    InventarioDTO actualizarStock(@PathVariable Long productoId, @RequestBody Integer cantidad);
}
