package com.example.dataService.controller;

import com.example.dataService.dto.CategoriaDTO;
import com.example.dataService.dto.InventarioDTO;
import com.example.dataService.dto.ProductoDTO;
import com.example.dataService.dto.ProductoRequest;
import com.example.dataService.service.CategoriaService;
import com.example.dataService.service.InventarioService;
import com.example.dataService.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el microservicio de datos.
 * Expone los endpoints para las operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * sobre las entidades de Producto, Categoria e Inventario.
 * Esta API es consumida internamente por otros servicios, como el business-service.
 */
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

    /**
     * Obtiene una lista de todos los productos.
     * @return ResponseEntity con una lista de ProductoDTO y estado 200 OK.
     */
    @GetMapping("/productos")
    public ResponseEntity<List<ProductoDTO>> obtenerTodosLosProductos() {
        return ResponseEntity.ok(productoService.obtenerTodosLosProductos());
    }

    /**
     * Obtiene un producto específico por su ID.
     * @param id El ID del producto a buscar.
     * @return ResponseEntity con el ProductoDTO encontrado y estado 200 OK.
     */
    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> obtenerProductoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerProductoPorId(id));
    }

    /**
     * Crea un nuevo producto y su inventario asociado.
     * @param request DTO con los datos para la creación del producto.
     * @return ResponseEntity con el ProductoDTO recién creado y estado 201 Created.
     */
    @PostMapping("/productos")
    public ResponseEntity<ProductoDTO> crearProducto(@RequestBody ProductoRequest request) {
        ProductoDTO nuevoProducto = productoService.crearProducto(request);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    /**
     * Actualiza un producto existente por su ID.
     * @param id El ID del producto a actualizar.
     * @param request DTO con los nuevos datos del producto.
     * @return ResponseEntity con el ProductoDTO actualizado y estado 200 OK.
     */
    @PutMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(@PathVariable Long id, @RequestBody ProductoRequest request) {
        ProductoDTO productoActualizado = productoService.actualizarProducto(id, request);
        return ResponseEntity.ok(productoActualizado);
    }

    /**
     * Elimina un producto por su ID.
     * @param id El ID del producto a eliminar.
     * Devuelve un estado 204 No Content si la operación es exitosa.
     */
    @DeleteMapping("/productos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
    }

    /**
     * Obtiene una lista de productos filtrada por el nombre de su categoría.
     * Ejemplo de llamada: GET /data/productos?categoria=Lacteos
     * @param nombre El nombre de la categoría por la que filtrar.
     * @return ResponseEntity con una lista de ProductoDTO filtrada y estado 200 OK.
     */
    @GetMapping(value = "/productos", params = "categoria")
    public ResponseEntity<List<ProductoDTO>> obtenerProductosPorCategoria(@RequestParam("categoria") String nombre) {
        return ResponseEntity.ok(productoService.buscarPorCategoriaNombre(nombre));
    }

    // --- Endpoints de Categorías ---

    /**
     * Obtiene una lista de todas las categorías.
     * @return ResponseEntity con una lista de CategoriaDTO y estado 200 OK.
     */
    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaDTO>> obtenerTodasLasCategorias() {
        return ResponseEntity.ok(categoriaService.obtenerTodas());
    }

    /**
     * Crea una nueva categoría.
     * @param request DTO con los datos de la categoría a crear.
     * @return ResponseEntity con la CategoriaDTO recién creada y estado 201 Created.
     */
    @PostMapping("/categorias")
    public ResponseEntity<CategoriaDTO> crearCategoria(@RequestBody CategoriaDTO request) {
        CategoriaDTO nuevaCategoria = categoriaService.crearCategoria(request);
        return new ResponseEntity<>(nuevaCategoria, HttpStatus.CREATED);
    }

    /**
     * Actualiza una categoría existente por su ID.
     * @param id El ID de la categoría a actualizar.
     * @param request DTO con los nuevos datos de la categoría.
     * @return ResponseEntity con la CategoriaDTO actualizada y estado 200 OK.
     */
    @PutMapping("/categorias/{id}")
    public ResponseEntity<CategoriaDTO> actualizarCategoria(@PathVariable Long id, @RequestBody CategoriaDTO request) {
        CategoriaDTO categoriaActualizada = categoriaService.actualizarCategoria(id, request);
        return ResponseEntity.ok(categoriaActualizada);
    }

    /**
     * Elimina una categoría por su ID.
     * @param id El ID de la categoría a eliminar.
     * Devuelve un estado 204 No Content si la operación es exitosa.
     */
    @DeleteMapping("/categorias/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrarCategoriaPorId(@PathVariable Long id) {
        categoriaService.borrarPorId(id);
    }

    // --- Endpoints de Inventario ---

    /**
     * Obtiene una lista de todos los productos que tienen un stock bajo (cantidad <= stockMinimo).
     * @return ResponseEntity con una lista de InventarioDTO y estado 200 OK.
     */
    @GetMapping("/inventario/stock-bajo")
    public ResponseEntity<List<InventarioDTO>> obtenerProductosConStockBajo() {
        return ResponseEntity.ok(inventarioService.obtenerProductosConStockBajo());
    }

    /**
     * Obtiene los detalles del inventario para un producto específico.
     * @param productoId El ID del producto cuyo inventario se quiere consultar.
     * @return ResponseEntity con el InventarioDTO encontrado y estado 200 OK.
     */
    @GetMapping("/inventario/{productoId}")
    public ResponseEntity<InventarioDTO> obtenerInventarioPorProductoId(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.obtenerInventarioPorProductoId(productoId));
    }

    /**
     * Actualiza el stock de un producto.
     * @param productoId El ID del producto cuyo stock se va a modificar.
     * @param cantidad La cantidad a sumar (positivo) o restar (negativo) del stock actual.
     * @return ResponseEntity con el InventarioDTO actualizado y estado 200 OK.
     */
    @PutMapping("/inventario/{productoId}") // <-- CAMBIADO DE @PatchMapping A @PutMapping
    public ResponseEntity<InventarioDTO> actualizarStock(@PathVariable Long productoId, @RequestBody Integer cantidad) {
        InventarioDTO inventarioActualizado = inventarioService.actualizarStock(productoId, cantidad);
        return ResponseEntity.ok(inventarioActualizado);
    }

}