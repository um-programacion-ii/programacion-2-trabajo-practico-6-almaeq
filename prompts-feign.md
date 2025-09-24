# Prompts de Feign

## Prompt 1: ver que otra lógica de negocio podía agregar a InventarioBusinessService

### Prompt Utilizado: del inventario se puede hacer alguna otra cosa aparte de obtener los prod con stock bajo?

### Respuesta Recibida:
¡Excelente pregunta! Sí, definitivamente se pueden y se deben hacer muchas otras cosas. La gestión de inventario es una de las partes más importantes de un sistema de ventas.

Además de solo consultar el stock bajo, las operaciones más críticas son las de actualización de stock. Aquí te muestro las funcionalidades más comunes que podrías agregar, con todo el código necesario.

1. Actualizar el Stock de un Producto
   Esta es la operación más fundamental. Se usa cuando se vende un producto (restar stock) o cuando llega nuevo mercancía (sumar stock).

A. Crear un DTO para la Petición
Primero, creamos un DTO para enviar la información necesaria para la actualización.

Archivo: ActualizarStockRequest.java

````bash
package com.example.sistemaMicroservicios.businessService.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarStockRequest {

    @NotNull(message = "La cantidad no puede ser nula.")
    private Integer cantidad; // Puede ser positiva (añadir) o negativa (restar)
}
````
B. Añadir el Método al Feign Client
Ahora, declaramos el nuevo endpoint en la interfaz DataServiceClient. Usaremos un método PATCH porque es una actualización parcial.

Archivo: DataServiceClient.java

````bash
// ... otros imports
import com.example.sistemaMicroservicios.businessService.dto.ActualizarStockRequest; // <-- Importar

@FeignClient(name = "data-service", url = "${data.service.url}")
public interface DataServiceClient {
// ... otros métodos ...

    @PatchMapping("/data/inventario/{productoId}")
    InventarioDTO actualizarStock(@PathVariable Long productoId, @RequestBody ActualizarStockRequest request);
}
````
C. Implementar el Servicio en InventarioBusinessService
Finalmente, añadimos el método en el servicio de negocio, incluyendo validaciones importantes.

Archivo: InventarioBusinessService.java

````bash
// ... otros imports
import com.example.sistemaMicroservicios.businessService.dto.ActualizarStockRequest; // <-- Importar
import com.example.sistemaMicroservicios.businessService.exception.ValidacionNegocioException; // <-- Importar

@Service
@Slf4j
public class InventarioBusinessService {

    // ... constructor y método obtenerProductosConStockBajo ...

    /**
     * Actualiza el stock de un producto específico.
     * @param productoId El ID del producto a actualizar.
     * @param request Contiene la cantidad a sumar o restar.
     * @return El DTO del inventario actualizado.
     */
    public InventarioDTO actualizarStock(Long productoId, ActualizarStockRequest request) {
        // Validación de negocio
        if (request.getCantidad() == 0) {
            throw new ValidacionNegocioException("La cantidad para actualizar no puede ser cero.");
        }

        try {
            log.info("Iniciando llamada para actualizar stock del producto ID: {} con cantidad: {}", productoId, request.getCantidad());
            InventarioDTO inventarioActualizado = dataServiceClient.actualizarStock(productoId, request);
            log.info("Stock actualizado correctamente para el producto ID: {}", productoId);
            return inventarioActualizado;
        } catch (FeignException.NotFound e) {
            log.error("Producto no encontrado en data-service con ID: {}", productoId);
            throw new ProductoNoEncontradoException("No se puede actualizar el stock porque el producto con ID " + productoId + " no existe.");
        } catch (FeignException e) {
            // Captura de otros posibles errores, como un 400 (Bad Request) si se intenta restar más stock del disponible
            if (e.status() == 400) {
                throw new ValidacionNegocioException("No hay stock suficiente para realizar la operación.");
            }
            log.error("Error de comunicación al intentar actualizar el stock: {}", e.getMessage());
            throw new MicroserviceCommunicationException("Error de comunicación al actualizar el stock.");
        }
    }
}
````
2. Consultar el Inventario de un Producto Específico
   Es muy útil tener un endpoint para ver los detalles del inventario de un solo producto.

A. Añadir el Método al Feign Client
Archivo: DataServiceClient.java

````bash
// ...
public interface DataServiceClient {
// ... otros métodos ...

    @GetMapping("/data/inventario/{productoId}")
    InventarioDTO obtenerInventarioPorProductoId(@PathVariable Long productoId);
}
````
B. Implementar el Servicio en InventarioBusinessService
Archivo: InventarioBusinessService.java

````bash
// ...
@Service
@Slf4j
public class InventarioBusinessService {
// ...

    /**
     * Obtiene los detalles del inventario para un producto específico.
     * @param productoId El ID del producto.
     * @return El DTO del inventario.
     */
    public InventarioDTO obtenerInventarioPorProductoId(Long productoId) {
        try {
            log.info("Consultando inventario para el producto ID: {}", productoId);
            return dataServiceClient.obtenerInventarioPorProductoId(productoId);
        } catch (FeignException.NotFound e) {
            log.error("Inventario no encontrado para el producto ID: {}", productoId);
            throw new InventarioNoEncontradoException("No se encontró inventario para el producto con ID " + productoId);
        } catch (FeignException e) {
            log.error("Error de comunicación al obtener el inventario: {}", e.getMessage());
            throw new MicroserviceCommunicationException("Error de comunicación al obtener el inventario.");
        }
    }
}
````
Con estas dos nuevas funcionalidades, tu InventarioBusinessService se vuelve mucho más completo y funcional para un sistema real. 📈

### Modificaciones Realizadas:
- Agregué solo obtenerInventarioPorProductoId porque no quería crear otros archivos

### Explicación del Prompt:
Quería ver qué otra cosa se podía agregar y no se me ocurría

### Aprendizajes Obtenidos:
- Con el primer ejemplo dado, terminé de entender cómo es la conexión entre las request, el DataServiceInterface y los services


## Prompt 2: error

### Prompt Utilizado: me da esto: [Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2025-09-12T23:50:40.377-03:00 ERROR 31173 --- [businessService] [  restartedMain] o.s.boot.SpringApplication               : Application run failed
java.lang.IllegalArgumentException: http://${data.service.url} is malformed
at org.springframework.cloud.openfeign.FeignClientsRegistrar.getUrl(FeignClientsRegistrar.java:127) ~[spring-cloud-openfeign-core-4.3.0.jar:4.3.0]]

### Respuesta Recibida:

¡Entendido! Este es un error clásico cuando se configura Feign y es el último paso para que todo funcione. Vamos a solucionarlo.

🤔 Análisis del Error: ¿Qué está pasando?
El error java.lang.IllegalArgumentException: http://${data.service.url} is malformed es muy claro:

Tu business-service está intentando crear el cliente Feign (DataServiceClient) para comunicarse con el data-service.

En la configuración de ese cliente, le dijiste que la URL del data-service es http://${data.service.url}.

Sin embargo, cuando ejecutas la aplicación con el perfil mysql (o cualquier otro perfil local), Spring no encuentra un valor para la propiedad data.service.url.

Como no la encuentra, intenta usar el texto literal http://${data.service.url}, lo cual no es una URL válida y la aplicación falla al arrancar.

✅ La Solución: Definir la URL para Entornos Locales
Necesitamos decirle al business-service a qué dirección debe apuntar cuando corre en tu máquina. Como el data-service se ejecuta localmente, la dirección será http://localhost:8081.

Para solucionar esto, vamos a añadir esta configuración en el business-service.

1. Edita el archivo application.properties del business-service
   Abre el siguiente archivo:
   sistemaMicroservicios/businessService/src/main/resources/application.properties

Y añade esta línea al final. Esto establecerá la URL por defecto para tu data-service:

````bash
data.service.url=http://localhost:8081
````
Este cambio le dará a Spring el valor que necesita para construir el cliente Feign correctamente.

⚠️ Nota Importante: Posible Conflicto de Puertos
He notado que en tu configuración para los perfiles dev y mysql, tanto el data-service como el business-service intentan usar el mismo puerto 8081. Esto causará un error de "puerto en uso" (Port already in use).

Para evitarlo, te recomiendo hacer un pequeño ajuste para que siempre usen puertos diferentes. Por ejemplo, que el business-service siempre use el 8080.

Ajusta el puerto en business-service
Abre businessService/src/main/resources/application-dev.yaml y asegúrate de que el puerto sea 8080.

Abre businessService/src/main/resources/application-mysql.yaml y asegúrate de que el puerto sea 8080.

Abre businessService/src/main/resources/application-postgres.yaml y asegúrate de que el puerto sea 8080.

Con estos dos cambios (añadir la URL y estandarizar los puertos), tus servicios podrán iniciarse y comunicarse sin problemas cuando sigas las instrucciones de tu profesor.

### Modificaciones Realizadas:
- No realicé modificaciones a lo que me dió

### Explicación del Prompt:
Me daba un error que no entendía

### Aprendizajes Obtenidos:
Como comunicar el BusinessService con el DataService