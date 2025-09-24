package com.example.businessService.controller;

import com.example.businessService.controller.BusinessController;
import com.example.businessService.dto.CategoriaDTO;
import com.example.businessService.dto.InventarioDTO;
import com.example.businessService.dto.ProductoDTO;
import com.example.businessService.dto.ProductoRequest;
import com.example.businessService.service.CategoriaBusinessService;
import com.example.businessService.service.InventarioBusinessService;
import com.example.businessService.service.ProductoBusinessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//Levanta solo la capa web para testear el DataController en aislamiento.
@WebMvcTest(controllers = BusinessController.class, excludeAutoConfiguration = FeignAutoConfiguration.class)
// Crea el bean MockMvc para hacer requests “falsos” a tus endpoints.
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = "data.service.url=http://dummy-url.com")
class BusinessControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean private ProductoBusinessService productoBusinessService;
    @MockBean private CategoriaBusinessService categoriaBusinessService;
    @MockBean private InventarioBusinessService inventarioBusinessService;

    private ProductoDTO p1;
    private ProductoDTO p2;

    @BeforeEach
    void init() {
        p1 = new ProductoDTO(1L, "Prod 1", "D1", new BigDecimal("50.00"), "Cat A", 10, false);
        p2 = new ProductoDTO(2L, "Prod 2", "D2", new BigDecimal("75.00"), "Cat B", 5, true);
    }

    @Test
    void catalogoProductos_ok() throws Exception {
        when(productoBusinessService.obtenerTodosLosProductos()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].stockBajo").value(true));
    }

    @Test
    void detalleProducto_ok() throws Exception {
        when(productoBusinessService.obtenerProductoPorId(1L)).thenReturn(p1);

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.precio").value(50.00));
    }

    @Test
    void registrarProducto_created() throws Exception {
        var req = new ProductoRequest("Nuevo", "Desc", new BigDecimal("100.00"), 3L, 12, 4);
        when(productoBusinessService.crearProducto(any(ProductoRequest.class))).thenReturn(p1);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerCategorias_ok() throws Exception {
        when(categoriaBusinessService.obtenerTodasLasCategorias())
                .thenReturn(List.of(new CategoriaDTO(1L, "Audio", "S"), new CategoriaDTO(2L, "Video", "P")));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Audio"))
                .andExpect(jsonPath("$[1].nombre").value("Video"));
    }

    @Test
    void consultarDisponibilidad_ok() throws Exception {
        when(inventarioBusinessService.verificarDisponibilidadStock(1L, 5)).thenReturn(true);

        mockMvc.perform(get("/api/inventario/1/disponibilidad").param("cantidad", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test
    void actualizarStock_ok() throws Exception {
        var inv = new InventarioDTO(9L, p1, 18, 5, LocalDateTime.now());
        when(inventarioBusinessService.actualizarStock(eq(1L), eq(8))).thenReturn(inv);

        mockMvc.perform(put("/api/inventario/1/actualizar-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(18));
    }

    @Test
    void reporteValorTotal_ok() throws Exception {
        when(productoBusinessService.calcularValorTotalInventario()).thenReturn(new BigDecimal("1234.56"));

        mockMvc.perform(get("/api/reportes/valor-total-inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(1234.56));
    }

    @Test
    void reporteStockBajo_ok() throws Exception {
        var inv = new InventarioDTO(1L, p2, 3, 5, LocalDateTime.now());
        when(inventarioBusinessService.obtenerProductosConStockBajo()).thenReturn(List.of(inv));

        mockMvc.perform(get("/api/reportes/stock-bajo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].producto.stockBajo").value(true))
                .andExpect(jsonPath("$[0].cantidad").value(3));
    }
}