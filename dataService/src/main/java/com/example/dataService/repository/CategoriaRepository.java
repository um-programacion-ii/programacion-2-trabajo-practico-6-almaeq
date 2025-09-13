package com.example.dataService.repository;

import com.example.dataService.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Boolean existsByNombreIgnoreCase(String nombreCategoria);
    Optional<Categoria> findByNombreIgnoreCase(String nombreCategoria);

    Optional<Categoria> findByNombre(String nombre);
}
