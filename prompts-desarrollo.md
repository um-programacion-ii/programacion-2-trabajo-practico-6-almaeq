# Prompts de Desarrollo

## Prompt 1: entender que hace PatchMapping

### Prompt Utilizado: que es el patchmapping?

### Respuesta Recibida:
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
