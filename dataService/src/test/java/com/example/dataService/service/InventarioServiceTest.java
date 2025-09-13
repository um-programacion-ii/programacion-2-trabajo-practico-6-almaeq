package com.example.dataService.service;

import com.example.dataService.dto.InventarioDTO;
import com.example.dataService.entity.Categoria;
import com.example.dataService.entity.Inventario;
import com.example.dataService.entity.Producto;
import com.example.dataService.exception.InventarioNoEncontradoException;
import com.example.dataService.exception.ValidacionNegocioException;
import com.example.dataService.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private InventarioService inventarioService;

    private Inventario inventario;
    private Producto producto;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        // Configuración de datos de prueba reutilizables
        categoria = new Categoria(1L, "Electrónica", "Dispositivos", Collections.emptyList());
        producto = new Producto(10L, "Laptop", "Core i7", BigDecimal.valueOf(1200), categoria, null);
        inventario = new Inventario(100L, producto, 50, 10, LocalDateTime.now());
        producto.setInventario(inventario); // Importante para la conversión a DTO
    }

    @Test
    void obtenerInventarioPorProductoId_cuandoExiste_deberiaDevolverDTO() {

        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(inventario));

        InventarioDTO resultado = inventarioService.obtenerInventarioPorProductoId(10L);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        assertEquals("Laptop", resultado.getProducto().getNombre());
    }

    @Test
    void obtenerInventarioPorProductoId_cuandoNoExiste_deberiaLanzarExcepcion() {

        when(inventarioRepository.findByProductoId(99L)).thenReturn(Optional.empty());

        assertThrows(InventarioNoEncontradoException.class, () -> {
            inventarioService.obtenerInventarioPorProductoId(99L);
        });
    }

    @Test
    void obtenerProductosConStockBajo_deberiaDevolverListaCorrecta() {

        inventario.setCantidad(5); // Ajustar para que cumpla la condición de stock bajo
        when(inventarioRepository.findByStockBajo()).thenReturn(List.of(inventario));

        List<InventarioDTO> resultado = inventarioService.obtenerProductosConStockBajo();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getProducto().getStockBajo());
    }

    @Test
    void actualizarStock_agregandoUnidades_deberiaSumarCorrectamente() {

        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);
        int cantidadOriginal = inventario.getCantidad(); // 50
        int cantidadAAgregar = 20;

        InventarioDTO resultado = inventarioService.actualizarStock(10L, cantidadAAgregar);

        assertNotNull(resultado);
        assertEquals(cantidadOriginal + cantidadAAgregar, resultado.getCantidad());
        verify(inventarioRepository, times(1)).save(inventario);
    }

    @Test
    void actualizarStock_restandoUnidades_deberiaRestarCorrectamente() {

        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);
        int cantidadOriginal = inventario.getCantidad(); // 50
        int cantidadARestar = -30;

        InventarioDTO resultado = inventarioService.actualizarStock(10L, cantidadARestar);

        assertNotNull(resultado);
        assertEquals(cantidadOriginal + cantidadARestar, resultado.getCantidad());
    }

    @Test
    void actualizarStock_conStockInsuficiente_deberiaLanzarExcepcion() {

        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(inventario));
        int cantidadARestar = -60; // Más que el stock actual de 50

        assertThrows(ValidacionNegocioException.class, () -> {
            inventarioService.actualizarStock(10L, cantidadARestar);
        });
        verify(inventarioRepository, never()).save(any());
    }
}
