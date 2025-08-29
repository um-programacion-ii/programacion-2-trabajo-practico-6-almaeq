package com.example.sistemaMicroservicios.dataService.repository;

import com.example.sistemaMicroservicios.dataService.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Boolean existsByNombreIgnoreCase(String nombre);
    @Query("SELECT e FROM Producto e WHERE e.categoria.nombre = :nombreCategoria")
    List<Producto> findByCategoriaId(Long categoriaId);
}
