package com.example.sistemaMicroservicios;

import com.example.sistemaMicroservicios.businessService.client.DataServiceClient;
import com.example.sistemaMicroservicios.businessService.dto.ProductoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "data.service.url=http://localhost:${wiremock.server.port}"
})
class DataServiceClientIntegrationTest {

    @Autowired private DataServiceClient dataServiceClient;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void obtenerProductoPorId_cuando200_devuelveDTO() throws Exception {
        // Arrange
        var dto = new ProductoDTO(1L, "Silla Ergonómica", "", new BigDecimal("250.50"),
                "Oficina", 30, false);
        var json = objectMapper.writeValueAsString(dto);

        // Si tu Feign usa path="/data", este es correcto:
        stubFor(get(urlEqualTo("/data/productos/1"))
                .willReturn(okJson(json)));

        var res = dataServiceClient.obtenerProductoPorId(1L);

        assertNotNull(res);
        assertEquals(1L, res.getId());
        assertEquals("Silla Ergonómica", res.getNombre());

        // Y verificamos que se hizo el request esperado
        verify(getRequestedFor(urlEqualTo("/data/productos/1")));
    }

    @Test
    void obtenerProductoPorId_cuando404_lanzaFeignNotFound() {
        // Arrange: 404
        stubFor(get(urlEqualTo("/data/productos/999"))
                .willReturn(aResponse().withStatus(404).withBody("Producto no encontrado")));

        var ex = assertThrows(FeignException.NotFound.class,
                () -> dataServiceClient.obtenerProductoPorId(999L));

        assertTrue(ex.contentUTF8().contains("Producto no encontrado")); // body útil para logs
        verify(getRequestedFor(urlEqualTo("/data/productos/999")));
    }
}