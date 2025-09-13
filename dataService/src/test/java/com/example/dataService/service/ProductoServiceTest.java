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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;
    private Categoria categoria;
    private ProductoRequest productoRequest;

    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "Electrónica", "Dispositivos", Collections.emptyList());
        producto = new Producto(10L, "Mouse", "Inalámbrico", BigDecimal.valueOf(50), categoria, null);
        Inventario inventario = new Inventario(100L, producto, 20, 5, null);
        producto.setInventario(inventario);

        productoRequest = new ProductoRequest("Mouse", "Inalámbrico", BigDecimal.valueOf(50), 1L, 20, 5);
    }

    @Test
    void obtenerTodosLosProductos_deberiaDevolverListaDTO() {

        when(productoRepository.findAll()).thenReturn(List.of(producto));

        List<ProductoDTO> resultado = productoService.obtenerTodosLosProductos();

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals("Mouse", resultado.get(0).getNombre());
    }

    @Test
    void obtenerProductoPorId_cuandoExiste_deberiaDevolverDTO() {

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        ProductoDTO resultado = productoService.obtenerProductoPorId(10L);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
    }

    @Test
    void obtenerProductoPorId_cuandoNoExiste_deberiaLanzarExcepcion() {

        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductoNoEncontradoException.class, () -> {
            productoService.obtenerProductoPorId(99L);
        });
    }

    @Test
    void crearProducto_conDatosValidos_deberiaCrearYDevolverDTO() {

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        // Usamos un ArgumentCaptor para verificar la entidad que se guarda
        ArgumentCaptor<Producto> productoCaptor = ArgumentCaptor.forClass(Producto.class);
        // Simulamos que al guardar se le asigna un ID
        when(productoRepository.save(productoCaptor.capture())).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductoDTO resultado = productoService.crearProducto(productoRequest);


        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Mouse", resultado.getNombre());
        // Verificamos que el inventario se haya creado correctamente
        Producto productoGuardado = productoCaptor.getValue();
        assertNotNull(productoGuardado.getInventario());
        assertEquals(20, productoGuardado.getInventario().getCantidad());
    }

    @Test
    void crearProducto_conCategoriaInexistente_deberiaLanzarExcepcion() {

        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());
        productoRequest.setCategoriaId(99L);

        assertThrows(CategoriaNoEncontradaException.class, () -> {
            productoService.crearProducto(productoRequest);
        });
        verify(productoRepository, never()).save(any());
    }

    @Test
    void actualizarProducto_conDatosValidos_deberiaActualizarYDevolverDTO() {

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        productoRequest.setNombre("Mouse Inalámbrico Pro");

        ProductoDTO resultado = productoService.actualizarProducto(10L, productoRequest);

        assertNotNull(resultado);
        assertEquals("Mouse Inalámbrico Pro", resultado.getNombre());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void buscarPorCategoriaNombre_cuandoCategoriaExiste_deberiaDevolverProductos() {

        when(categoriaRepository.findByNombre("Electrónica")).thenReturn(Optional.of(categoria));
        when(productoRepository.findByCategoriaId(1L)).thenReturn(List.of(producto));

        List<ProductoDTO> resultado = productoService.buscarPorCategoriaNombre("Electrónica");

        assertFalse(resultado.isEmpty());
        assertEquals("Mouse", resultado.get(0).getNombre());
    }

    @Test
    void buscarPorCategoriaNombre_cuandoCategoriaNoExiste_deberiaDevolverListaVacia() {

        when(categoriaRepository.findByNombre("Inexistente")).thenReturn(Optional.empty());

        List<ProductoDTO> resultado = productoService.buscarPorCategoriaNombre("Inexistente");

        assertTrue(resultado.isEmpty());
        verify(productoRepository, never()).findByCategoriaId(anyLong());
    }

    @Test
    void eliminarProducto_cuandoExiste_deberiaLlamarADelete() {

        when(productoRepository.existsById(10L)).thenReturn(true);
        doNothing().when(productoRepository).deleteById(10L);

        assertDoesNotThrow(() -> productoService.eliminarProducto(10L));

        verify(productoRepository, times(1)).deleteById(10L);
    }
}