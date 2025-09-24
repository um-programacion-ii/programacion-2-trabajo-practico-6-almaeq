package com.example.businessService.service;


import com.example.businessService.client.DataServiceClient;
import com.example.businessService.dto.CategoriaDTO;
import com.example.businessService.exception.MicroserviceCommunicationException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CategoriaBusinessService {

    private final DataServiceClient dataServiceClient;

    public CategoriaBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    public List<CategoriaDTO> obtenerTodasLasCategorias() {
        try {
            log.info("Iniciando llamada a data-service para obtener todas las categorías.");
            List<CategoriaDTO> categorias = dataServiceClient.obtenerTodasLasCategorias();
            log.info("Llamada a data-service exitosa. Se obtuvieron {} categorías.", categorias.size());
            return categorias;
        } catch (FeignException e) {
            log.error("Error al comunicarse con el microservicio de datos para obtener categorías: {}", e.getMessage());
            throw new MicroserviceCommunicationException("Error de comunicación al intentar obtener las categorías.");
        }
    }
    /**
     * Llama al data-service para crear una nueva categoría.
     * @param categoriaDTO El DTO con la información de la categoría a crear.
     * @return El DTO de la categoría ya creada con su ID.
     */
    public CategoriaDTO crearCategoria(CategoriaDTO categoriaDTO) {
        try {
            log.info("Iniciando llamada a data-service para crear la categoría: {}", categoriaDTO.getNombre());
            CategoriaDTO nuevaCategoria = dataServiceClient.crearCategoria(categoriaDTO);
            log.info("Categoría '{}' creada exitosamente con ID: {}", nuevaCategoria.getNombre(), nuevaCategoria.getId());
            return nuevaCategoria;
        } catch (FeignException e) {
            log.error("Error al comunicarse con el microservicio de datos para crear la categoría: {}", e.getMessage());
            // Aquí podrías manejar diferentes códigos de error, por ejemplo, un 409 Conflict si la categoría ya existe.
            throw new MicroserviceCommunicationException("Error de comunicación al intentar crear la categoría.");
        }
    }
}