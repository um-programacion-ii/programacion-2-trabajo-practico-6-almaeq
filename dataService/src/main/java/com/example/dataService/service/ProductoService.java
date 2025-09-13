package com.example.dataService.service;

import com.example.dataService.dto.ProductoDTO;
import com.example.dataService.dto.ProductoRequest;
import com.example.dataService.entity.Categoria;
import com.example.dataService.entity.Inventario;
import com.example.dataService.entity.Producto;
import com.example.dataService.exception.CategoriaNoEncontradaException;
import com.example.dataService.exception.ProductoNoEncontradoException;
import com.example.dataService.repository.CategoriaRepository;
import com.example.dataService.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<ProductoDTO> obtenerTodosLosProductos() {
        return productoRepository.findAll().stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    public ProductoDTO obtenerProductoPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado con ID: " + id));
        return convertirAProductoDTO(producto);
    }

    public ProductoDTO crearProducto(ProductoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new CategoriaNoEncontradaException("La categoría con ID " + request.getCategoriaId() + " no existe."));

        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre(request.getNombre());
        nuevoProducto.setDescripcion(request.getDescripcion());
        nuevoProducto.setPrecio(request.getPrecio());
        nuevoProducto.setCategoria(categoria);

        // Crear el inventario inicial asociado al producto
        Inventario inventario = new Inventario();
        inventario.setProducto(nuevoProducto);
        inventario.setCantidad(request.getStock());
        inventario.setStockMinimo(request.getStockMinimo());
        inventario.setFechaActualizacion(LocalDateTime.now());

        // La relación @OneToOne con Cascade.ALL se encarga de guardar el inventario
        nuevoProducto.setInventario(inventario);

        Producto productoGuardado = productoRepository.save(nuevoProducto);
        return convertirAProductoDTO(productoGuardado);
    }

    public ProductoDTO actualizarProducto(Long id, ProductoRequest request) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado con ID: " + id));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new CategoriaNoEncontradaException("La categoría con ID " + request.getCategoriaId() + " no existe."));

        productoExistente.setNombre(request.getNombre());
        productoExistente.setDescripcion(request.getDescripcion());
        productoExistente.setPrecio(request.getPrecio());
        productoExistente.setCategoria(categoria);

        // Actualizar también el inventario
        Inventario inventario = productoExistente.getInventario();
        inventario.setCantidad(request.getStock());
        inventario.setStockMinimo(request.getStockMinimo());
        inventario.setFechaActualizacion(LocalDateTime.now());

        Producto productoActualizado = productoRepository.save(productoExistente);
        return convertirAProductoDTO(productoActualizado);
    }

    public List<ProductoDTO> buscarPorCategoriaNombre(String nombreCategoria) {
        // Primero, encontramos la categoría por su nombre
        return categoriaRepository.findByNombre(nombreCategoria)
                .map(categoria -> {
                    // Si la categoría existe, buscamos los productos por su ID
                    List<Producto> productos = productoRepository.findByCategoriaId(categoria.getId());
                    return productos.stream()
                            .map(this::convertirAProductoDTO)
                            .collect(Collectors.toList());
                })
                // Si la categoría no existe, devolvemos una lista vacía
                .orElse(Collections.emptyList());
    }

    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ProductoNoEncontradoException("No se puede eliminar. Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }
    
    private ProductoDTO convertirAProductoDTO(Producto producto) {
        Inventario inventario = producto.getInventario();
        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getCategoria().getNombre(),
                inventario.getCantidad(),
                inventario.getCantidad() <= inventario.getStockMinimo()
        );
    }
}
