package ingsoftware.evaluacion2.Servicio;

import ingsoftware.evaluacion2.Modelo.Variante;
import ingsoftware.evaluacion2.Repositorio.VarianteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VarianteService {
    @Autowired
    private VarianteRepository varianteRepository;

    public List<Variante> obtenerTodas() {
        return varianteRepository.findAll();
    }

    public Variante guardar(Variante variante) {
        return varianteRepository.save(variante);
    }
}
