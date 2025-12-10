package ingsoftware.evaluacion2.Repositorio;

import ingsoftware.evaluacion2.Modelo.ItemCotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemCotizacionRepository extends JpaRepository<ItemCotizacion, Long> {
    List<ItemCotizacion> findByCotizacionId(Long idCotizacion);
    @Modifying
    @Query(value = "DELETE FROM items_cotizacion WHERE cotizacion_id = :id", nativeQuery = true)
    void borrarItemsNativo(@Param("id") Long id);
}
