package com.example.sistemaMicroservicios.dataService.service;

import com.example.sistemaMicroservicios.dataService.dto.CategoriaDTO;
import com.example.sistemaMicroservicios.dataService.entity.Categoria;
import com.example.sistemaMicroservicios.dataService.exception.CategoriaDuplicadaException;
import com.example.sistemaMicroservicios.dataService.exception.CategoriaNoEncontradaException;
import com.example.sistemaMicroservicios.dataService.exception.ValidacionNegocioException;
import com.example.sistemaMicroservicios.dataService.repository.CategoriaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoriaService {
    private CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaDTO> obtenerTodas() {
        List<Categoria> categorias = categoriaRepository.findAll();
        return categorias.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public CategoriaDTO obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException("Categoría no encontrada con ID: " + id));
        return convertirADto(categoria);
    }

    public Categoria guardar(Categoria categoria) {
        if (categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())) {
            throw new CategoriaDuplicadaException("La categoría ya está registrada: " + categoria.getNombre());
        }
        return categoriaRepository.save(categoria);
    }

    public CategoriaDTO crearCategoria(CategoriaDTO categoriaDto) {
        categoriaRepository.findByNombre(categoriaDto.getNombre()).ifPresent(c -> {
            throw new CategoriaDuplicadaException("Ya existe una categoría con el nombre: " + categoriaDto.getNombre());
        });

        Categoria nuevaCategoria = new Categoria();
        nuevaCategoria.setNombre(categoriaDto.getNombre());
        nuevaCategoria.setDescripcion(categoriaDto.getDescripcion());

        Categoria categoriaGuardada = categoriaRepository.save(nuevaCategoria);

        return convertirADto(categoriaGuardada);
    }

    public void borrarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException("No se puede eliminar. Categoría no encontrada con ID: " + id));

        // Comprobar si la categoría tiene productos
        if (categoria.getProductos() != null && !categoria.getProductos().isEmpty()) {
            throw new ValidacionNegocioException("No se puede eliminar la categoría porque tiene " + categoria.getProductos().size() + " productos asociados.");
        }

        categoriaRepository.deleteById(id);
    }

    public CategoriaDTO actualizarCategoria(Long id, CategoriaDTO categoriaDto) {
        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException("No se puede actualizar. Categoría no encontrada con ID: " + id));

        categoriaRepository.findByNombre(categoriaDto.getNombre()).ifPresent(c -> {
            if (!c.getId().equals(id)) {
                throw new CategoriaDuplicadaException("El nombre '" + categoriaDto.getNombre() + "' ya está en uso por otra categoría.");
            }
        });

        categoriaExistente.setNombre(categoriaDto.getNombre());
        categoriaExistente.setDescripcion(categoriaDto.getDescripcion());

        Categoria categoriaActualizada = categoriaRepository.save(categoriaExistente);
        return convertirADto(categoriaActualizada);
    }

    private CategoriaDTO convertirADto(Categoria categoria) {
        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}
