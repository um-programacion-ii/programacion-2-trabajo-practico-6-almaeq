package com.example.dataService.controller;

import com.example.dataService.dto.CategoriaDTO;
import com.example.dataService.dto.InventarioDTO;
import com.example.dataService.dto.ProductoDTO;
import com.example.dataService.dto.ProductoRequest;
import com.example.dataService.exception.ProductoNoEncontradoException;
import com.example.dataService.service.CategoriaService;
import com.example.dataService.service.InventarioService;
import com.example.dataService.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.util.List.of;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DataController.class, excludeAutoConfiguration = FeignAutoConfiguration .class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
//@TestPropertySource(properties = "data.service.url=http://localhost:8080")
class DataControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ProductoService productoService;
    @MockBean CategoriaService categoriaService;
    @MockBean InventarioService inventarioService;

    // Lo incluyo porque aparece en tu proyecto y evita fallos de contexto
    //@MockBean
    //DataServiceClient dataServiceClient;

    // --------- Fixtures (se rellenan en @BeforeEach) ----------
    private ProductoDTO prod1;
    private ProductoDTO prod2;
    private CategoriaDTO cat1;
    private CategoriaDTO cat2;
    private InventarioDTO inv1;

    @BeforeEach
    void setUp() {
        prod1 = buildProductoDTO(1L, "Producto 1", "Desc 1", "Cat A", "50.00", 10, false);
        prod2 = buildProductoDTO(2L, "Producto 2", "Desc 2", "Cat B", "75.00", 5, true);

        cat1 = new CategoriaDTO(1L, "Audio", "Sonido");
        cat2 = new CategoriaDTO(2L, "Video", "Pantallas");

        inv1 = new InventarioDTO();
        inv1.setId(99L);
        inv1.setProducto(prod1);
        inv1.setCantidad(3);
        inv1.setStockMinimo(5);
        inv1.setFechaActualizacion(LocalDateTime.now());
    }

    // ---------- Helpers ----------
    private static ProductoDTO buildProductoDTO(Long id, String nombre, String desc, String cat,
                                                String precio, int cantidad, boolean stockBajo) {
        var p = new ProductoDTO();
        p.setId(id);
        p.setNombre(nombre);
        p.setDescripcion(desc);
        p.setPrecio(new BigDecimal(precio));
        p.setCategoriaNombre(cat);
        p.setStock(cantidad);
        p.setStockBajo(stockBajo);
        return p;
    }

    private static ProductoRequest buildProductoReq(String nombre, String desc, String precio,
                                                    Long categoriaId, Integer cantidad, Integer stockMinimo) {
        return new ProductoRequest(
                nombre,
                desc,
                new BigDecimal(precio),
                categoriaId,
                cantidad,
                stockMinimo
        );
    }

    // ===================== PRODUCTOS =====================

    @Test
    void obtenerTodosLosProductos_ok() throws Exception {
        when(productoService.obtenerTodosLosProductos()).thenReturn(of(prod1, prod2));

        mockMvc.perform(get("/data/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Producto 1"))
                .andExpect(jsonPath("$[1].stockBajo").value(true));
    }

    @Test
    void obtenerProductoPorId_ok() throws Exception {
        when(productoService.obtenerProductoPorId(1L)).thenReturn(prod1);

        mockMvc.perform(get("/data/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Producto 1"))
                .andExpect(jsonPath("$.categoriaNombre").value("Cat A"))
                .andExpect(jsonPath("$.precio").value(50.00));
    }

    @Test
    void crearProducto_created() throws Exception {
        var req = buildProductoReq("Producto Nuevo", "Desc nuevo", "80.00", 1L, 7, 3);
        // el controller devuelve un DTO; reuso prod1 como respuesta mock
        when(productoService.crearProducto(any(ProductoRequest.class))).thenReturn(prod1);

        mockMvc.perform(post("/data/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Producto 1"))
                .andExpect(jsonPath("$.categoriaNombre").value("Cat A"));
    }

    @Test
    void actualizarProducto_ok() throws Exception {
        var req = buildProductoReq("Producto Actualizado", "Desc actualizada", "150.00", 1L, 25, 5);
        var actualizado = buildProductoDTO(1L, "Producto Actualizado", "Desc actualizada", "Cat A", "150.00", 25, false);

        when(productoService.actualizarProducto(eq(1L), any(ProductoRequest.class))).thenReturn(actualizado);

        mockMvc.perform(put("/data/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Producto Actualizado"))
                .andExpect(jsonPath("$.precio").value(150.00));
    }

    @Test
    void eliminarProducto_noContent() throws Exception {
        doNothing().when(productoService).eliminarProducto(1L);

        mockMvc.perform(delete("/data/productos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void obtenerProductosPorCategoria_ok() throws Exception {
        when(productoService.buscarPorCategoriaNombre("Electrónicos"))
                .thenReturn(of(buildProductoDTO(10L, "Laptop", "Gaming", "Electrónicos", "1500.00", 12, false)));

        mockMvc.perform(get("/data/productos").param("categoria", "Electrónicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoriaNombre").value("Electrónicos"))
                .andExpect(jsonPath("$[0].nombre").value("Laptop"));
    }

    @Test
    void buscarProductoInexistente_notFound() throws Exception {
        when(productoService.obtenerProductoPorId(999L))
                .thenThrow(new ProductoNoEncontradoException("Producto no encontrado con ID: 999"));

        mockMvc.perform(get("/data/productos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarProductoInexistente_notFound() throws Exception {
        var req = buildProductoReq("X", "Y", "1.00", 1L, 1, 1);

        when(productoService.actualizarProducto(eq(999L), any(ProductoRequest.class)))
                .thenThrow(new ProductoNoEncontradoException("Producto no encontrado con ID: 999"));

        mockMvc.perform(put("/data/productos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarProductoInexistente_notFound() throws Exception {
        doThrow(new ProductoNoEncontradoException("Producto no encontrado con ID: 999"))
                .when(productoService).eliminarProducto(999L);

        mockMvc.perform(delete("/data/productos/999"))
                .andExpect(status().isNotFound());
    }

    // ===================== CATEGORÍAS =====================

    @Test
    void obtenerTodasLasCategorias_ok() throws Exception {
        when(categoriaService.obtenerTodas()).thenReturn(of(cat1, cat2));

        mockMvc.perform(get("/data/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].nombre").value("Video"));
    }

    @Test
    void crearCategoria_created() throws Exception {
        var req = new CategoriaDTO(null, "Hogar", "Casa");
        var res = new CategoriaDTO(5L, "Hogar", "Casa");

        when(categoriaService.crearCategoria(any(CategoriaDTO.class))).thenReturn(res);

        mockMvc.perform(post("/data/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nombre").value("Hogar"));
    }

    @Test
    void actualizarCategoria_ok() throws Exception {
        var req = new CategoriaDTO(null, "Hogar y Jardín", "Actualizada");
        var res = new CategoriaDTO(5L, "Hogar y Jardín", "Actualizada");

        when(categoriaService.actualizarCategoria(eq(5L), any(CategoriaDTO.class))).thenReturn(res);

        mockMvc.perform(put("/data/categorias/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Hogar y Jardín"))
                .andExpect(jsonPath("$.descripcion").value("Actualizada"));
    }

    @Test
    void borrarCategoria_noContent() throws Exception {
        doNothing().when(categoriaService).borrarPorId(9L);

        mockMvc.perform(delete("/data/categorias/9"))
                .andExpect(status().isNoContent());
    }

    // ===================== INVENTARIO =====================

    @Test
    void obtenerProductosConStockBajo_ok() throws Exception {
        var bajo = new InventarioDTO();
        bajo.setId(1L);
        bajo.setProducto(buildProductoDTO(2L, "Memoria", "DDR4", "RAM", "30.00", 3, true));
        bajo.setCantidad(3);
        bajo.setStockMinimo(5);
        bajo.setFechaActualizacion(LocalDateTime.now());

        when(inventarioService.obtenerProductosConStockBajo()).thenReturn(of(bajo));

        mockMvc.perform(get("/data/inventario/stock-bajo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].producto.nombre").value("Memoria"))
                .andExpect(jsonPath("$[0].cantidad").value(3));
    }

    @Test
    void obtenerInventarioPorProductoId_ok() throws Exception {
        when(inventarioService.obtenerInventarioPorProductoId(1L)).thenReturn(inv1);

        mockMvc.perform(get("/data/inventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.producto.nombre").value("Producto 1"))
                .andExpect(jsonPath("$.cantidad").value(3));
    }

    @Test
    void actualizarStock_ok() throws Exception {
        // Simulamos que sumaste 15 -> 3 + 15 = 18
        var actualizado = new InventarioDTO();
        actualizado.setId(99L);
        actualizado.setProducto(prod1);
        actualizado.setCantidad(18);
        actualizado.setStockMinimo(5);
        actualizado.setFechaActualizacion(LocalDateTime.now());

        when(inventarioService.actualizarStock(1L, 15)).thenReturn(actualizado);

        mockMvc.perform(put("/data/inventario/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(15)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.producto.id").value(1))
                .andExpect(jsonPath("$.cantidad").value(18));
    }
}