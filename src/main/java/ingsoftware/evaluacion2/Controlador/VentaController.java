package ingsoftware.evaluacion2.Controlador;

import ingsoftware.evaluacion2.Modelo.Cotizacion;
import ingsoftware.evaluacion2.Repositorio.CotizacionRepository;
import ingsoftware.evaluacion2.Repositorio.MuebleRepository;
import ingsoftware.evaluacion2.Servicio.VentaService;
import ingsoftware.evaluacion2.dto.CotizacionRequestDTO;

import java.util.List;

import ingsoftware.evaluacion2.dto.ItemCotizacionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;
    @Autowired
    private CotizacionRepository cotizacionRepository;
    @Autowired
    private MuebleRepository muebleRepository;

    @PostMapping("/cotizar")
    public ResponseEntity<Cotizacion> crearCotizacion(@RequestBody CotizacionRequestDTO requestDTO) {
        Cotizacion cotizacion = null;
        Long idCotizacionTemp = null;

        for (ItemCotizacionDTO item : requestDTO.getItems()) {

            cotizacion = ventaService.agregarItem(
                    idCotizacionTemp,
                    item.getMuebleId(),
                    item.getVarianteId(),
                    item.getCantidad()
            );

            idCotizacionTemp = cotizacion.getId();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(cotizacion);
    }

    @PostMapping("/confirmar/{idCotizacion}")
    public ResponseEntity<Cotizacion> confirmarVenta(@PathVariable Long idCotizacion) {
        Cotizacion cotizacion = ventaService.confirmarVenta(idCotizacion);
        return ResponseEntity.ok(cotizacion);
    }

    @GetMapping("/{idCotizacion}")
    public ResponseEntity<Cotizacion> obtenerCotizacionPorId(@PathVariable Long idCotizacion) {
        return ventaService.obtenerCotizacionPorId(idCotizacion)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Cotizacion>> listarTodasLasCotizaciones() {
        List<Cotizacion> cotizaciones = ventaService.listarTodasLasCotizaciones();
        return ResponseEntity.ok(cotizaciones);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        if (ex.getMessage().startsWith("stock insuficiente")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }


}
