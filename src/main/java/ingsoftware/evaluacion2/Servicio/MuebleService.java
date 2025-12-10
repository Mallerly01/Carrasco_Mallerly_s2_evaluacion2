package ingsoftware.evaluacion2.Servicio;

import ingsoftware.evaluacion2.Modelo.EstadoMueble;
import ingsoftware.evaluacion2.Modelo.Mueble;
import ingsoftware.evaluacion2.Repositorio.MuebleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MuebleService {

    @Autowired
    private MuebleRepository muebleRepository;

    public List<Mueble> listarMueblesActivos() {
        return muebleRepository.findByEstado(EstadoMueble.ACTIVO);
    }

    public List<Mueble> listarTodosLosMuebles() {
        return muebleRepository.findAll();
    }

    public Mueble crearMueble(Mueble mueble) {
        mueble.setEstado(EstadoMueble.ACTIVO);
        return muebleRepository.save(mueble);
    }

    public Optional<Mueble> obtenerMueblePorId(Long id) {
        return muebleRepository.findById(id);
    }

    public Mueble actualizarMueble(Long id, Mueble muebleDetalles) {
        Mueble mueble = muebleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mueble no encontrado con id: " + id));

        mueble.setNombreMueble(muebleDetalles.getNombreMueble());
        mueble.setTipo(muebleDetalles.getTipo());
        mueble.setPrecioBase(muebleDetalles.getPrecioBase());
        mueble.setStock(muebleDetalles.getStock());
        mueble.setEstado(muebleDetalles.getEstado());
        mueble.setTamaño(muebleDetalles.getTamaño());
        mueble.setMaterial(muebleDetalles.getMaterial());

        return muebleRepository.save(mueble);
    }

    public Mueble desactivarMueble(Long id) {
        Mueble mueble = muebleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mueble no encontrado con id: " + id));

        mueble.setEstado(EstadoMueble.INACTIVO);
        return muebleRepository.save(mueble);
    }
    // ... dentro de MuebleService ...

    public void agregarStock(Long idMueble, int cantidadExtra) {
        Mueble mueble = muebleRepository.findById(idMueble)
                .orElseThrow(() -> new RuntimeException("Mueble no encontrado"));

        // Sumamos lo que había + lo nuevo
        mueble.setStock(mueble.getStock() + cantidadExtra);

        muebleRepository.save(mueble);
    }
}
