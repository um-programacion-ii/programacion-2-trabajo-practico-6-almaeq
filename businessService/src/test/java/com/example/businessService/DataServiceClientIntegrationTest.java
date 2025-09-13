package com.example.businessService;

import com.example.businessService.client.DataServiceClient;
import com.example.businessService.dto.ProductoDTO;
import com.example.businessService.dto.ProductoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@EnableFeignClients(clients = DataServiceClient.class)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "data.service.url=http://localhost:${wiremock.server.port}",
        "logging.level.feign=DEBUG",
        "feign.client.config.default.loggerLevel=FULL"
})
class DataServiceClientIntegrationTest {

    @Autowired private DataServiceClient dataServiceClient;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void obtenerProductoPorId_cuando200_devuelveDTO() throws Exception {
        var dto = new ProductoDTO(1L, "Silla Ergonómica", "", new BigDecimal("250.50"),
                "Oficina", 30, false);
        var json = objectMapper.writeValueAsString(dto);

        stubFor(get(urlPathEqualTo("/data/productos/1"))
                .willReturn(okJson(json)));

        var res = dataServiceClient.obtenerProductoPorId(1L);

        assertNotNull(res);
        assertEquals(1L, res.getId());
        assertEquals("Silla Ergonómica", res.getNombre());
        verify(getRequestedFor(urlPathEqualTo("/data/productos/1")));
    }

    @Test
    void obtenerProductoPorId_cuando404_lanzaFeignNotFound() {
        stubFor(get(urlPathEqualTo("/data/productos/999"))
                .willReturn(aResponse().withStatus(404).withBody("Producto no encontrado")));

        var ex = assertThrows(FeignException.NotFound.class,
                () -> dataServiceClient.obtenerProductoPorId(999L));

        assertTrue(ex.contentUTF8().contains("Producto no encontrado"));
        verify(getRequestedFor(urlPathEqualTo("/data/productos/999")));
    }

    @Test
    void obtenerProductosPorCategoria_agregaQueryParam() throws Exception {
        var dto1 = new ProductoDTO(10L, "Teclado", "", new BigDecimal("10.00"), "Periféricos", 5, false);
        var dto2 = new ProductoDTO(11L, "Mouse", "", new BigDecimal("8.00"), "Periféricos", 8, false);
        var json = objectMapper.writeValueAsString(List.of(dto1, dto2));

        stubFor(get(urlPathEqualTo("/data/productos"))
                .withQueryParam("categoria", equalTo("Periféricos"))
                .willReturn(okJson(json)));

        var list = dataServiceClient.obtenerProductosPorCategoria("Periféricos");

        assertEquals(2, list.size());
        verify(getRequestedFor(urlPathEqualTo("/data/productos"))
                .withQueryParam("categoria", equalTo("Periféricos")));
    }

    @Test
    void crearProducto_enviaJsonYDevuelveDTO() throws Exception {
        var req = new ProductoRequest(
                "Notebook",
                "14\"",
                new BigDecimal("999.99"),
                5L,
                20,
                3
        );

        var resp = new ProductoDTO(
                100L,
                req.getNombre(),
                req.getDescripcion(),
                req.getPrecio(),
                "Computación",
                req.getStock(),
                false
        );

        var reqJson = objectMapper.writeValueAsString(req);
        var respJson = objectMapper.writeValueAsString(resp);

        stubFor(post(urlPathEqualTo("/data/productos"))
                .withRequestBody(equalToJson(reqJson, true, true))
                .willReturn(okJson(respJson)));

        var creado = dataServiceClient.crearProducto(req);

        assertEquals(100L, creado.getId());
        assertEquals("Notebook", creado.getNombre());
        assertEquals("Computación", creado.getCategoriaNombre());
        assertEquals(20, creado.getStock());
        verify(postRequestedFor(urlPathEqualTo("/data/productos")));
    }
}