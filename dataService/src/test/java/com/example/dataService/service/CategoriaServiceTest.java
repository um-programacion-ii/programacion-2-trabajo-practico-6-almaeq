package com.example.dataService.service;


import com.example.dataService.dto.CategoriaDTO;
import com.example.dataService.entity.Categoria;
import com.example.dataService.entity.Producto;
import com.example.dataService.exception.CategoriaDuplicadaException;
import com.example.dataService.exception.CategoriaNoEncontradaException;
import com.example.dataService.exception.ValidacionNegocioException;
import com.example.dataService.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;
    private CategoriaDTO categoriaDTO;

    @BeforeEach
    void setUp() {
        // Objeto base para usar en múltiples tests
        categoria = new Categoria(1L, "Tecnología", "Dispositivos electrónicos", new ArrayList<>());
        categoriaDTO = new CategoriaDTO(1L, "Tecnología", "Dispositivos electrónicos");
    }

    @Test
    void obtenerTodas_deberiaDevolverListaDeCategoriasDTO() {

        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));

        List<CategoriaDTO> resultado = categoriaService.obtenerTodas();

        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals("Tecnología", resultado.get(0).getNombre());
    }

    @Test
    void obtenerPorId_cuandoExiste_deberiaDevolverCategoriaDTO() {

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        CategoriaDTO resultado = categoriaService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerPorId_cuandoNoExiste_deberiaLanzarExcepcion() {

        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoriaNoEncontradaException.class, () -> {
            categoriaService.obtenerPorId(99L);
        });
    }

    @Test
    void crearCategoria_conDatosValidos_deberiaGuardarYDevolverDTO() {

        when(categoriaRepository.findByNombre(anyString())).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        CategoriaDTO resultado = categoriaService.crearCategoria(new CategoriaDTO(null, "Tecnología", "Dispositivos electrónicos"));

        assertNotNull(resultado);
        assertEquals("Tecnología", resultado.getNombre());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void crearCategoria_conNombreDuplicado_deberiaLanzarExcepcion() {

        when(categoriaRepository.findByNombre("Tecnología")).thenReturn(Optional.of(categoria));

        assertThrows(CategoriaDuplicadaException.class, () -> {
            categoriaService.crearCategoria(categoriaDTO);
        });
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void actualizarCategoria_conDatosValidos_deberiaActualizarYDevolverDTO() {

        CategoriaDTO datosActualizados = new CategoriaDTO(1L, "Tecno Avanzada", "Nueva descripción");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        CategoriaDTO resultado = categoriaService.actualizarCategoria(1L, datosActualizados);

        assertNotNull(resultado);
        assertEquals("Tecno Avanzada", resultado.getNombre());
        verify(categoriaRepository, times(1)).save(categoria);
    }

    @Test
    void borrarPorId_sinProductosAsociados_deberiaEliminar() {

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        doNothing().when(categoriaRepository).deleteById(1L);

        assertDoesNotThrow(() -> categoriaService.borrarPorId(1L));

        verify(categoriaRepository, times(1)).deleteById(1L);
    }

    @Test
    void borrarPorId_conProductosAsociados_deberiaLanzarExcepcion() {

        categoria.setProductos(List.of(new Producto())); // Simular que tiene un producto
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        assertThrows(ValidacionNegocioException.class, () -> {
            categoriaService.borrarPorId(1L);
        });
        verify(categoriaRepository, never()).deleteById(anyLong());
    }
}