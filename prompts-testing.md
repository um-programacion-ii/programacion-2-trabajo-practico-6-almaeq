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