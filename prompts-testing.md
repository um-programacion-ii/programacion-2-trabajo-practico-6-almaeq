# Prompts de Testing

## Prompt 1: Entender un error

### Prompt Utilizado: tengo este error[java.lang.IllegalArgumentException: http://${data.service.url} is malformed at org.springframework.cloud.openfeign.FeignClientsRegistrar.getUrl(FeignClientsRegistrar.java:127) ~[spring-cloud-openfeign-core-4.3.0.jar:4.3.0] at org.springframework.cloud.openfeign.FeignClientsRegistrar.getUrl(FeignClientsRegistrar.java:370) ~[spring-cloud-openfeign-core-4.3.0.jar:4.3.0] at org.springframework.cloud.openfeign.FeignClientsRegistrar.eagerlyRegisterFeignClientBeanDefinition(FeignClientsRegistrar.java:226) ~[spring-cloud-openfeign-core-4.3.0.jar:4.3.0]]

### Respuesta Recibida:
¡Bien visto! El fallo no es del test en sí, sino del contexto que intenta registrar tus Feign clients con una URL sin resolver:
````bash
http://${data.service.url}  ->  (no se reemplazó la propiedad)
````

En un @WebMvcTest no necesitás levantar Feign; por eso hay dos arreglos posibles. Te dejo ambos—con el recomendado primero.

✅ Opción A (recomendada): excluir Feign en el slice test

Evita que @WebMvcTest registre Feign y listo.
````bash
import org.springframework.cloud.openfeign.FeignAutoConfiguration;

@WebMvcTest(controllers = DataController.class,
excludeAutoConfiguration = FeignAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class DataControllerWebMvcTest {
// ...
}
````

No hace falta @MockBean del cliente Feign ni definir propiedades. Es la forma más limpia para tests de MVC.

✅ Opción B: mantener Feign pero definir la propiedad y mockear el cliente

Si por alguna razón querés que Feign esté en el contexto del test:

Definí la propiedad para que la URL sea válida:

Con anotación:
````bash
import org.springframework.test.context.TestPropertySource;

@WebMvcTest(controllers = DataController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "data.service.url=localhost:8080")
class DataControllerWebMvcTest {
// ...
}

````
(o ponelo en src/test/resources/application-test.yml si usás @ActiveProfiles("test")):
````bash
data:
service:
url: localhost:8080
````

Mockeá el Feign client para que no intente llamar nada:
````bash
@MockBean
private com.example.sistemaMicroservicios.businessService.client.DataServiceClient dataServiceClient;
````

Con esto http://${data.service.url} se resuelve a http://localhost:8080 y el contexto arranca.

¿Por qué pasa?

Tu app principal seguramente tiene @EnableFeignClients (o algo que lo importa). Aunque @WebMvcTest es un “slice”, algunas configs terminan presentes y Feign intenta registrar clientes al iniciar. Si la propiedad ${data.service.url} no está definida en el classpath de test, queda la cadena literal http://${...} y revienta con URISyntaxException.

Qué te recomiendo aplicar ahora

Para tus tests de controller, aplica Opción A (excluir Feign). Es la más simple y evita dependencias innecesarias en el slice MVC.

Si más adelante armás tests de integración donde sí quieras el contexto completo, usá la Opción B (propiedad + @MockBean).

Si querés, te devuelvo tu DataControllerWebMvcTest ya con excludeAutoConfiguration = FeignAutoConfiguration.class aplicado a la cabecera y sin el @MockBean DataServiceClient.

### Modificaciones Realizadas:
- Decidí agregar esta línea:@TestPropertySource(properties = "data.service.url=localhost:8080") y además crear un GlobalExceptionHandler

### Explicación del Prompt:
Me daba un error q no entendía

### Aprendizajes Obtenidos:
- @TestPropertySource(properties = "data.service.url=localhost:8080") inyecta/overridea propiedades solo para este test.


## Prompt 2: Entender cómo testear Feign Client

### Prompt Utilizado: como se testea el feign client?

### Respuesta Recibida:

¡Excelente pregunta! Testear un Feign Client es crucial para asegurar que la comunicación entre tus microservicios es robusta. No se prueba como un servicio normal (con mocks), sino a través de un test de integración que verifica la comunicación HTTP real.

La mejor manera de hacerlo es usando una herramienta llamada WireMock.

¿Qué es WireMock y cómo funciona? 🤔
Imagina que quieres probar si tu business-service puede llamar correctamente al data-service, pero no quieres tener que levantar el data-service real con su base de datos solo para una prueba.

WireMock crea un servidor web falso que simula ser el data-service. Tú le das instrucciones muy específicas:

"Oye, WireMock, si alguien te hace una petición GET a la URL /data/productos/1, quiero que respondas con el código de estado 200 y este texto JSON que simula un producto."

Luego, tu test ejecuta el DataServiceClient real, que intentará conectarse a esa URL. Como hemos configurado el test para que apunte al servidor falso de WireMock, la llamada se completa y podemos verificar si nuestro cliente Feign fue capaz de interpretar la respuesta correctamente.

Cómo implementarlo en tu proyecto
Aquí tienes el código exacto para testear tu DataServiceClient.

1. Añade la dependencia de WireMock
   Asegúrate de que esta dependencia esté en tu pom.xml:

````bash

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-contract-stub-runner</artifactId>
    <scope>test</scope>
</dependency>
````
2. Crea el archivo de Test
Este test verifica que el método obtenerProductoPorId de tu cliente Feign funcione.

Crea este nuevo archivo: src/test/java/com/example/sistemaMicroservicios/businessService/client/DataServiceClientIntegrationTest.java

````bash

package com.example.sistemaMicroservicios.businessService.client;

import com.example.sistemaMicroservicios.businessService.dto.ProductoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // Carga el contexto completo de la aplicación
@ActiveProfiles("test") // Usa un perfil de prueba para no cargar configuraciones de desarrollo
@AutoConfigureWireMock(port = 0) // 1. Inicia un servidor WireMock en un puerto aleatorio
@TestPropertySource(properties = {
// 2. Sobrescribe la propiedad para que Feign apunte a nuestro servidor falso
"data.service.url=http://localhost:${wiremock.server.port}"
})
class DataServiceClientIntegrationTest {

    @Autowired
    private DataServiceClient dataServiceClient; // 3. El cliente Feign REAL que vamos a probar

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos a JSON

    @Test
    void obtenerProductoPorId_cuandoDataServiceRespondeOK_deberiaDeserializarCorrectamente() throws Exception {
        // --- 1. Preparación (Arrange) ---
        // Creamos el DTO que esperamos que el servidor falso devuelva
        ProductoDTO productoFalso = new ProductoDTO(1L, "Silla Gamer", "Ergonómica", new BigDecimal("299.99"), "Oficina", 15, false);
        // Lo convertimos a un string JSON
        String jsonResponse = objectMapper.writeValueAsString(productoFalso);

        // --- 2. Simulación (Stubbing) ---
        // Le decimos a WireMock cómo debe comportarse
        stubFor(get(urlEqualTo("/data/productos/1")) // CUANDO reciba un GET a esta URL...
                .willReturn(aResponse() // ENTONCES responde con...
                        .withStatus(200) // Un código de estado 200 OK
                        .withHeader("Content-Type", "application/json") // Esta cabecera
                        .withBody(jsonResponse))); // Y este cuerpo JSON

        // --- 3. Ejecución (Act) ---
        // Llamamos al método de nuestro cliente Feign. Esta es la llamada HTTP real.
        ProductoDTO resultado = dataServiceClient.obtenerProductoPorId(1L);

        // --- 4. Verificación (Assert) ---
        // Verificamos que el cliente haya procesado la respuesta JSON correctamente
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Silla Gamer", resultado.getNombre());
        assertEquals(new BigDecimal("299.99"), resultado.getPrecio());
    }
}
````

### Modificaciones Realizadas:
- Agregué también un método para cuando da un 404

### Explicación del Prompt:
No sabía como testear el Feign Client

### Aprendizajes Obtenidos:
- Como testear el Feign Client

## Prompt 3: Entender un error

### Prompt Utilizado: tengo este error[/home/alma/.jdks/openjdk-24.0.1/bin/java -javaagent:/opt/idea-IU-251.27812.49/plugins/java/lib/rt/debugger-agent.jar=file:///tmp/capture335220238614171899.props -ea -Didea.test.cyclic.buffer.size=1048576 -javaagent:/opt/idea-IU-251.27812.49/lib/idea_rt.jar=38647 -Dkotlinx.coroutines.debug.enable.creation.stack.trace=false -Ddebugger.agent.enable.coroutines=true -Dkotlinx.coroutines.debug.enable.flows.stack.trace=true -Dkotlinx.coroutines.debug.enable.mutable.state.flows.stack.trace=true -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath /home/alma/.m2/repository/org/junit/platform/junit-]

### Respuesta Recibida:

El error dice que Spring no encuentra una clase con @SpringBootConfiguration (tu @SpringBootApplication) al “mirar hacia arriba” desde el paquete del test. Pasa cuando:

El paquete del test no está bajo el paquete base de tu app, o

No le pasaste explícitamente una clase de configuración a @SpringBootTest.

Tenés dos formas de arreglarlo. Te dejo la recomendada (A) que es 100% auto-contenida para tests de Feign y no necesita levantar toda tu app; y también una alternativa (B) si preferís apuntar a tu clase principal.

A) Fix auto-contenido para tests de Feign (recomendado)

Usa una mini app de test dentro del propio test:

````bash
@SpringBootTest(
classes = DataServiceClientIntegrationTest.TestApp.class,
webEnvironment = WebEnvironment.NONE
)
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
"data.service.url=http://localhost:${wiremock.server.port}",
"logging.level.feign=DEBUG",
"feign.client.config.default.loggerLevel=FULL"
})
class DataServiceClientIntegrationTest {

    // ===== Mini aplicación SOLO para este test =====
    @Configuration
    @EnableAutoConfiguration // activa auto-config de Spring Boot
    @EnableFeignClients(clients = DataServiceClient.class) // registra tu cliente
    @ImportAutoConfiguration({
            JacksonAutoConfiguration.class,
            HttpMessageConvertersAutoConfiguration.class,
            WebMvcAutoConfiguration.class,
            RestTemplateAutoConfiguration.class
    })
    static class TestApp { }

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
        // Tu ProductoRequest real: nombre, descripcion, precio, categoriaId, stock, stockMinimo
        var req = new ProductoRequest("Notebook", "14\"", new BigDecimal("999.99"), 5L, 20, 3);

        // El data-service responde con ProductoDTO (categoría como String y destacado boolean)
        var resp = new ProductoDTO(100L, req.getNombre(), req.getDescripcion(), req.getPrecio(),
                "Computación", req.getStock(), false);

        var reqJson  = objectMapper.writeValueAsString(req);
        var respJson = objectMapper.writeValueAsString(resp);

        stubFor(post(urlPathEqualTo("/data/productos"))
                .withRequestBody(equalToJson(reqJson, true, true))
                .willReturn(okJson(respJson)));

        var creado = dataServiceClient.crearProducto(req);

        assertEquals(100L, creado.getId());
        assertEquals("Notebook", creado.getNombre());
        assertEquals("Computación", creado.getCategoria());
        assertEquals(20, creado.getStock());
        verify(postRequestedFor(urlPathEqualTo("/data/productos")));
    }
}
````

¿Qué hace esto?

Define una config mínima (TestApp) con @EnableAutoConfiguration y @EnableFeignClients para registrar solo tu Feign.

@SpringBootTest(classes = TestApp.class) le dice a Spring qué configuración cargar → desaparece el error de “no se encontró @SpringBootConfiguration”.

Mantiene WireMock y los stubs igual que antes.

B) Alternativa: apuntar a tu clase principal

Si preferís que el test levante tu app “real”, indicá la clase de arranque:

````bash
@SpringBootTest(
classes = com.example.sistemaMicroservicios.SistemaMicroserviciosApplication.class,
webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@EnableFeignClients(clients = DataServiceClient.class)
@AutoConfigureWireMock(port = 0)
````

Cambiá SistemaMicroserviciosApplication y el paquete por el que tengas en tu proyecto.
Asegurate también de que el paquete del test esté igual o debajo del paquete base de esa clase.

Extra: por qué te pasaba

Spring busca una clase con @SpringBootConfiguration (normalmente tu @SpringBootApplication) subiendo paquetes desde com.example.dataService. Si tu clase principal está en, por ejemplo, com.example.sistemaMicroservicios, no la va a encontrar. Las dos soluciones de arriba corrigen ese desalineamiento.

Si querés, me pasás el nombre/paquete de tu clase @SpringBootApplication y te dejo el (B) listo 1:1 con tu estructura.

### Modificaciones Realizadas:
- Decidí usar la opción B sin cambiar la forma en la q me dió el código

### Explicación del Prompt:
No sabía cómo arreglar el error

### Aprendizajes Obtenidos:
- Como testear el Feign Client