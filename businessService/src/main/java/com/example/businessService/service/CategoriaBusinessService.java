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
}