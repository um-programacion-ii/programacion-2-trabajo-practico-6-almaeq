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

