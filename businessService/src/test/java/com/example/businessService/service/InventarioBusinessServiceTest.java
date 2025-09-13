package com.example.businessService.service;

import com.example.businessService.client.DataServiceClient;
import com.example.businessService.dto.InventarioDTO;
import com.example.businessService.exception.MicroserviceCommunicationException;
import com.example.businessService.exception.ProductoNoEncontradoException;
import com.example.businessService.exception.ValidacionNegocioException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioBusinessServiceTest {

    @Mock
    private DataServiceClient dataServiceClient; // Se simula el cliente Feign

    @InjectMocks
    private InventarioBusinessService inventarioBusinessService; // La clase bajo prueba

    private InventarioDTO inventarioDTO;
    private Request dummyRequest;

    @BeforeEach
    void setUp() {
        // Objeto DTO reutilizable para las pruebas
        inventarioDTO = new InventarioDTO();
        inventarioDTO.setCantidad(100);
        inventarioDTO.setStockMinimo(20);
        inventarioDTO.setId(1L);

        // Objeto Request falso para construir FeignException
        dummyRequest = Request.create(Request.HttpMethod.GET, "/fake", new HashMap<>(), null, new RequestTemplate());
    }

    @Test
    void obtenerProductosConStockBajo_cuandoClienteFunciona_deberiaDevolverLista() {

        when(dataServiceClient.obtenerProductosConStockBajo()).thenReturn(List.of(inventarioDTO));

        List<InventarioDTO> resultado = inventarioBusinessService.obtenerProductosConStockBajo();

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(dataServiceClient, times(1)).obtenerProductosConStockBajo();
    }

    @Test
    void verificarDisponibilidadStock_conStockSuficiente_deberiaDevolverTrue() {

        when(dataServiceClient.obtenerInventarioPorProductoId(1L)).thenReturn(inventarioDTO);

        boolean disponible = inventarioBusinessService.verificarDisponibilidadStock(1L, 50);

        assertTrue(disponible);
    }

    @Test
    void verificarDisponibilidadStock_conStockInsuficiente_deberiaDevolverFalse() {

        when(dataServiceClient.obtenerInventarioPorProductoId(1L)).thenReturn(inventarioDTO);

        boolean disponible = inventarioBusinessService.verificarDisponibilidadStock(1L, 150);

        assertFalse(disponible);
    }

    @Test
    void verificarDisponibilidadStock_conCantidadInvalida_deberiaLanzarExcepcion() {

        int cantidadInvalida = 0;

        assertThrows(ValidacionNegocioException.class, () -> {
            inventarioBusinessService.verificarDisponibilidadStock(1L, cantidadInvalida);
        });
        verify(dataServiceClient, never()).obtenerInventarioPorProductoId(anyLong());
    }

    @Test
    void verificarDisponibilidadStock_cuandoProductoNoExiste_deberiaDevolverFalse() {
        // Preparación: Simular una FeignException de tipo "Not Found"
        when(dataServiceClient.obtenerInventarioPorProductoId(99L))
                .thenThrow(new FeignException.NotFound("Not Found", dummyRequest, null, null));

        boolean disponible = inventarioBusinessService.verificarDisponibilidadStock(99L, 10);

        assertFalse(disponible);
    }

    @Test
    void actualizarStock_conCantidadCero_deberiaLanzarExcepcion() {

        int cantidadCero = 0;

        assertThrows(ValidacionNegocioException.class, () -> {
            inventarioBusinessService.actualizarStock(1L, cantidadCero);
        });
        verify(dataServiceClient, never()).actualizarStock(anyLong(), anyInt());
    }

    @Test
    void actualizarStock_cuandoProductoNoExiste_deberiaLanzarProductoNoEncontradoException() {

        when(dataServiceClient.actualizarStock(99L, 10))
                .thenThrow(new FeignException.NotFound("Not Found", dummyRequest, null, null));

        assertThrows(ProductoNoEncontradoException.class, () -> {
            inventarioBusinessService.actualizarStock(99L, 10);
        });
    }

    @Test
    void actualizarStock_cuandoDataServiceReportaStockInsuficiente_deberiaLanzarValidacionNegocioException() {
        // Preparación: Simular una FeignException de tipo "Bad Request" (400)
        when(dataServiceClient.actualizarStock(1L, -200))
                .thenThrow(new FeignException.BadRequest("Stock insuficiente", dummyRequest, null, null));

        assertThrows(ValidacionNegocioException.class, () -> {
            inventarioBusinessService.actualizarStock(1L, -200);
        });
    }

    @Test
    void actualizarStock_cuandoOcurreErrorDeComunicacion_deberiaLanzarMicroserviceCommunicationException() {
        // Preparación: Simular una FeignException genérica (ej. 500 Internal Server Error)
        when(dataServiceClient.actualizarStock(1L, 10))
                .thenThrow(new FeignException.InternalServerError("Server Error", dummyRequest, null, null));

        assertThrows(MicroserviceCommunicationException.class, () -> {
            inventarioBusinessService.actualizarStock(1L, 10);
        });
    }
}