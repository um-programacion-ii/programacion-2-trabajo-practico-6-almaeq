package com.example.businessService.service;

import com.example.businessService.client.DataServiceClient;
import com.example.businessService.dto.CategoriaDTO;
import com.example.businessService.exception.MicroserviceCommunicationException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaBusinessServiceTest {

    @Mock
    private DataServiceClient dataServiceClient; // Se simula el cliente Feign

    @InjectMocks
    private CategoriaBusinessService categoriaBusinessService; // La clase que estamos probando

    private Request dummyRequest;

    @BeforeEach
    void setUp() {
        // Objeto Request falso necesario para construir una FeignException
        dummyRequest = Request.create(Request.HttpMethod.GET, "/fake", new HashMap<>(), null, new RequestTemplate());
    }

    @Test
    void obtenerTodasLasCategorias_cuandoClienteFunciona_deberiaDevolverLista() {
        // Preparación: Simular una respuesta exitosa del cliente Feign
        List<CategoriaDTO> categoriasSimuladas = List.of(
                new CategoriaDTO(1L, "Electrónica", "Dispositivos electrónicos"),
                new CategoriaDTO(2L, "Hogar", "Artículos para el hogar")
        );
        when(dataServiceClient.obtenerTodasLasCategorias()).thenReturn(categoriasSimuladas);

        List<CategoriaDTO> resultado = categoriaBusinessService.obtenerTodasLasCategorias();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Electrónica", resultado.get(0).getNombre());
        // Verificar que el método del cliente fue llamado exactamente una vez
        verify(dataServiceClient, times(1)).obtenerTodasLasCategorias();
    }

    @Test
    void obtenerTodasLasCategorias_cuandoClienteFalla_deberiaLanzarMicroserviceCommunicationException() {
        // Preparación: Simular que el cliente Feign lanza una excepción
        when(dataServiceClient.obtenerTodasLasCategorias())
                .thenThrow(new FeignException.InternalServerError("Error en el servidor de datos", dummyRequest, null, null));

        assertThrows(MicroserviceCommunicationException.class, () -> {
            categoriaBusinessService.obtenerTodasLasCategorias();
        });

        // Verificar que el método del cliente fue llamado
        verify(dataServiceClient, times(1)).obtenerTodasLasCategorias();
    }
}