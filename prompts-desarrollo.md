# Prompts de Desarrollo

c
@PatchMapping es una anotación de Spring que se usa para indicar que un método de un controlador manejará peticiones HTTP de tipo PATCH.

En el contexto de una API REST, una petición PATCH se utiliza para realizar una actualización parcial de un recurso.

Piénsalo de esta manera:

@PutMapping (Actualización Completa): Es como si rellenaras un formulario completo con todos los datos de un usuario para actualizar su perfil. Envías toda la información, incluso si solo querías cambiar el número de teléfono.

@PatchMapping (Actualización Parcial): Es como si enviaras una pequeña nota que dice: "Solo cambia el número de teléfono de este usuario a '555-1234'". Solo envías el dato específico que quieres modificar.

¿Por qué lo usamos para actualizarStock?
El caso de actualizarStock en tu proyecto es el ejemplo perfecto para usar @PatchMapping.

````bash

// En tu DataController
@PatchMapping("/inventario/{productoId}")
public ResponseEntity<InventarioDTO> actualizarStock(@PathVariable Long productoId, @RequestBody Integer cantidad) {
// ...
}
````
Aquí, la operación es muy específica: solo quieres cambiar un único campo del recurso Inventario (el campo cantidad).

Es eficiente: No necesitas enviar toda la información del producto y del inventario (ID, nombre, precio, stock mínimo, etc.) solo para sumar o restar 5 unidades al stock. Solo envías el número 5 o -5.

Es semánticamente correcto: Estás indicando claramente que esta operación es una modificación pequeña y específica, no un reemplazo total del recurso.

En resumen, @PatchMapping es la herramienta correcta cuando tu intención es actualizar solo una parte de un recurso, como en el caso de modificar el stock. Es más eficiente y describe mejor la intención de tu API.

### Modificaciones Realizadas:
- Usé PatchMapping para actualizar el stock

### Explicación del Prompt:
No sabía como poner para que se actualice el stock

### Aprendizajes Obtenidos:
- El uso de Patch Mapping

## Prompt 2: error

### Prompt Utilizado: [ 2025-09-13T00:03:33.266-03:00 ERROR 36040 --- [businessService] [nio-8080-exec-8] c.e.b.service.InventarioBusinessService  : Error de comunicación al actualizar stock.
feign.RetryableException: Invalid HTTP method: PATCH executing PATCH http://localhost:8081/data/inventario/1
at feign.FeignException.errorExecuting(FeignException.java:300) ~[feign-core-13.6.jar:na]
at feign.SynchronousMethodHandler.executeAndDecode(SynchronousMethodHandler.java:105) ~[feign-core-13.6.jar:na]
at feign.SynchronousMethodHandler.invoke(SynchronousMethodHandler.java:53) ~[feign-core-13.6.jar:na]
at feign.ReflectiveFeign$FeignInvocationHandler.invoke(ReflectiveFeign.java:104) ~[feign-core-13.6.jar:na]
at jdk.proxy4/jdk.proxy4.$Proxy127.actualizarStock(Unknown Source) ~[na:na]
at com.example.businessService.service.InventarioBusinessService.actualizarStock(InventarioBusinessService.java:74) ~[classes/:na]]

### Respuesta Recibida:
Of course. I've analyzed the error log, and it points to a very specific technical issue with how Feign is configured by default. It's a classic problem, and thankfully, it's easy to fix.

🤔 Análisis del Error: ¿Qué está pasando?
El error java.net.ProtocolException: Invalid HTTP method: PATCH significa que el cliente HTTP que Feign está usando por defecto no soporta el método PATCH. Por increíble que parezca, el cliente HTTP básico de Java (HttpURLConnection) no permite PATCH, y Feign lo usa si no se le indica lo contrario.

Cuando tu business-service intenta llamar al endpoint actualizarStock del data-service usando PATCH, este cliente HTTP base falla, resultando en el error que ves.

✅ La Solución: Añadir un Cliente HTTP más moderno
Para solucionarlo, solo necesitas añadir una dependencia a tu businessService. Al hacerlo, Spring Boot la detectará automáticamente y configurará Feign para que la use en lugar de la predeterminada.

1. Edita el archivo pom.xml del business-service

Abre el siguiente archivo:
sistemaMicroservicios/businessService/pom.xml

2. Añade la siguiente dependencia

Agrega este bloque de código dentro de la sección <dependencies> de tu pom.xml:

````bash
        <dependency>
            <groupId>io.github.openfeign</groupId>
            <artifactId>feign-httpclient</artifactId>
        </dependency>
````
(No necesitas especificar una versión, Spring Boot la gestionará por ti para asegurar la compatibilidad).

Una vez que hayas añadido la dependencia, Maven la descargará la próxima vez que ejecutes la aplicación. Reinicia tu business-service y el comando curl para actualizar el stock (PATCH) debería funcionar sin problemas.

### Modificaciones Realizadas:
- Agregué esa dependencia pero el error persistió, así que cambié el patch por el put. Arreglé todos los métodos que usaban PUT

### Explicación del Prompt:
ME daba ese error que no entendía

### Aprendizajes Obtenidos:
- el cliente HTTP básico de Java no permite el uso de PATCH