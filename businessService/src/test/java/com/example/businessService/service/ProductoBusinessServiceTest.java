package com.example.businessService.service;

import com.example.businessService.client.DataServiceClient;
import com.example.businessService.dto.ProductoDTO;
import com.example.businessService.dto.ProductoRequest;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoBusinessServiceTest {

    @Mock
    private DataServiceClient dataServiceClient; // Se simula el cliente Feign

    @InjectMocks
    private ProductoBusinessService productoBusinessService; // La clase bajo prueba

    private ProductoRequest productoRequest;
    private ProductoDTO productoDTO;

    @BeforeEach
    void setUp() {
        // Datos de prueba reutilizables
        productoRequest = new ProductoRequest("Laptop", "Gamer", BigDecimal.valueOf(1500), 1L, 10, 5);
        productoDTO = new ProductoDTO(1L, "Laptop", "Gamer", BigDecimal.valueOf(1500), "Electrónica", 10, false);
    }

    @Test
    void obtenerTodosLosProductos_cuandoClienteFunciona_deberiaDevolverLista() {

        when(dataServiceClient.obtenerTodosLosProductos()).thenReturn(List.of(productoDTO));

        List<ProductoDTO> resultado = productoBusinessService.obtenerTodosLosProductos();

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(dataServiceClient, times(1)).obtenerTodosLosProductos();
    }

    @Test
    void obtenerProductoPorId_cuandoClienteResponde404_deberiaLanzarProductoNoEncontrado() {
        // Preparación: Simular una FeignException de tipo "Not Found"
        Request request = Request.create(Request.HttpMethod.GET, "/fake", new HashMap<>(), null, new RequestTemplate());
        when(dataServiceClient.obtenerProductoPorId(99L)).thenThrow(new FeignException.NotFound("Not Found", request, null, null));

        assertThrows(ProductoNoEncontradoException.class, () -> {
            productoBusinessService.obtenerProductoPorId(99L);
        });
    }

    @Test
    void crearProducto_conPrecioCero_deberiaLanzarValidacionNegocioException() {

        productoRequest.setPrecio(BigDecimal.ZERO);

        assertThrows(ValidacionNegocioException.class, () -> {
            productoBusinessService.crearProducto(productoRequest);
        });
        // Asegurarse de que nunca se llamó al cliente si la validación falla
        verify(dataServiceClient, never()).crearProducto(any());
    }

    @Test
    void crearProducto_conStockNegativo_deberiaLanzarValidacionNegocioException() {

        productoRequest.setStock(-1);

        assertThrows(ValidacionNegocioException.class, () -> {
            productoBusinessService.crearProducto(productoRequest);
        });
        verify(dataServiceClient, never()).crearProducto(any());
    }

    @Test
    void crearProducto_conDatosValidos_deberiaLlamarAlClienteYDevolverDTO() {

        when(dataServiceClient.crearProducto(productoRequest)).thenReturn(productoDTO);

        ProductoDTO resultado = productoBusinessService.crearProducto(productoRequest);

        assertNotNull(resultado);
        assertEquals("Laptop", resultado.getNombre());
        verify(dataServiceClient, times(1)).crearProducto(productoRequest);
    }

    @Test
    void calcularValorTotalInventario_deberiaDevolverSumaCorrecta() {

        ProductoDTO p1 = new ProductoDTO(1L, "Producto A", "", BigDecimal.valueOf(10.5), "Cat A", 10, false); // Valor: 105.0
        ProductoDTO p2 = new ProductoDTO(2L, "Producto B", "", BigDecimal.valueOf(20), "Cat B", 5, false);     // Valor: 100.0
        when(dataServiceClient.obtenerTodosLosProductos()).thenReturn(List.of(p1, p2));

        BigDecimal valorTotal = productoBusinessService.calcularValorTotalInventario();

        assertEquals(new BigDecimal("205.0"), valorTotal);
    }

    @Test
    void calcularValorTotalInventario_cuandoClienteFalla_deberiaLanzarMicroserviceCommunicationException() {

        when(dataServiceClient.obtenerTodosLosProductos()).thenThrow(FeignException.class);

        assertThrows(MicroserviceCommunicationException.class, () -> {
            productoBusinessService.calcularValorTotalInventario();
        });
    }
}