package ingsoftware.evaluacion2.Repositorio;

import ingsoftware.evaluacion2.Modelo.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
    @Modifying
    @Query(value = "DELETE FROM cotizaciones WHERE id = :id", nativeQuery = true)
    void borrarCotizacionNativo(@Param("id") Long id);
}