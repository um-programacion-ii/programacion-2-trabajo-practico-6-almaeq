package com.example.sistemaMicroservicios.dataService.repository;

import com.example.sistemaMicroservicios.dataService.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    Optional<Inventario> findByProductoId(Long id);

    @Query("SELECT i FROM Inventario i WHERE i.cantidad <= i.stockMinimo")
    List<Inventario> findByStockBajo();


    @Query("SELECT i FROM Inventario i JOIN FETCH i.producto")
    List<Inventario> findAllWithProducto();
}
