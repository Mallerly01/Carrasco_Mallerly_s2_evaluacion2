package ingsoftware.evaluacion2.Controlador;

import ingsoftware.evaluacion2.Modelo.Cotizacion;
import ingsoftware.evaluacion2.Modelo.ItemCotizacion;
import ingsoftware.evaluacion2.Modelo.Mueble;
import ingsoftware.evaluacion2.Modelo.Variante;
import ingsoftware.evaluacion2.Servicio.MuebleService;
import ingsoftware.evaluacion2.Servicio.VarianteService;
import ingsoftware.evaluacion2.Servicio.VentaService;
import ingsoftware.evaluacion2.dto.CotizacionRequestDTO;
import ingsoftware.evaluacion2.dto.ItemCotizacionDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/web")
public class WebController {
    @Autowired
    private MuebleService muebleService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VarianteService varianteService;

    // --- PANTALLA PRINCIPAL ---
    @GetMapping
    public String index() {
        return "index";
    }

    // --- GUSTAVO (ADMINISTRADOR) ---
    @GetMapping("/admin")
    public String adminMuebles(Model model) {
        //cargar Muebles existentes
        model.addAttribute("muebles", muebleService.listarTodosLosMuebles());
        model.addAttribute("nuevoMueble", new Mueble());

        //cargar Variantes existentes
        model.addAttribute("variantes", varianteService.obtenerTodas());
        model.addAttribute("nuevaVariante", new Variante());

        return "admin";
    }

    @PostMapping("/admin/variantes/guardar")
    public String guardarVariante(@ModelAttribute Variante variante) {
        varianteService.guardar(variante);
        return "redirect:/web/admin";
    }

    @PostMapping("/admin/guardar")
    public String guardarMueble(@ModelAttribute Mueble mueble) {
        if (mueble.getIdMueble() != null && mueble.getIdMueble() > 0) {
            muebleService.actualizarMueble(mueble.getIdMueble(), mueble);
        } else {
            muebleService.crearMueble(mueble);
        }
        return "redirect:/web/admin";
    }

    @GetMapping("/admin/desactivar/{id}")
    public String desactivarMueble(@PathVariable Long id) {
        muebleService.desactivarMueble(id);
        return "redirect:/web/admin";
    }
    @PostMapping("/admin/stock/agregar")
    public String agregarStock(@RequestParam Long idMueble, @RequestParam int cantidad) {
        muebleService.agregarStock(idMueble, cantidad);
        return "redirect:/web/admin";
    }

    // --- CLIENTE ---
    @GetMapping("/cliente")
    public String catalogo(@RequestParam(required = false) String busqueda, Model model, HttpSession session) {
        // lógica de búsqueda de muebles
        List<Mueble> muebles = muebleService.listarMueblesActivos();

        if (busqueda != null && !busqueda.isEmpty()) {
            muebles = muebles.stream()
                    .filter(m -> m.getNombreMueble().toLowerCase().contains(busqueda.toLowerCase()))
                    .toList();
        }
        model.addAttribute("muebles", muebles);
        model.addAttribute("variantes", varianteService.obtenerTodas());

        Long idCotizacion = (Long) session.getAttribute("idCotizacionActual");
        int cantidadEnCarrito = 0;

        if (idCotizacion != null) {
            List<ItemCotizacion> items = ventaService.obtenerItemsDeCotizacion(idCotizacion);
            cantidadEnCarrito = items.size();
        }
        model.addAttribute("cantidadCarrito", cantidadEnCarrito);

        return "catalogo";
    }

    @PostMapping("/cliente/agregar")
    public String agregarAlCarrito(
            @RequestParam Long idMueble,
            @RequestParam int cantidad,
            @RequestParam(required = false) Long idVariante,
            HttpSession session) {

        try {
            // recuperamos el ID de cotización del usuario
            Long idCotizacionActual = (Long) session.getAttribute("idCotizacionActual");

            // llamamos al servicio
            Cotizacion c = ventaService.agregarItem(idCotizacionActual, idMueble, idVariante, cantidad);

            // guardamos el ID en la sesión del usuario
            session.setAttribute("idCotizacionActual", c.getId());

            return "redirect:/web/cliente?exito=Producto agregado";
        } catch (Exception e) {
            return "redirect:/web/cliente?error=" + e.getMessage();
        }
    }

    @GetMapping("/cliente/carrito")
    public String verCarrito(Model model, HttpSession session) {
        Long idCotizacion = (Long) session.getAttribute("idCotizacionActual");

        List<ItemCotizacion> items = new ArrayList<>();
        double total = 0.0;

        if (idCotizacion != null) {
            // buscamos los items directo de la BD, no de la lista de la cotización
            items = ventaService.obtenerItemsDeCotizacion(idCotizacion);

            // recalculamos el total
            total = items.stream()
                    .mapToDouble(i -> i.getPrecioUnitarioFinal() * i.getCantidad())
                    .sum();
        }

        model.addAttribute("items", items);
        model.addAttribute("total", total);

        return "carrito";
    }

    @PostMapping("/cliente/confirmar")
    public String confirmarVenta(HttpSession session) {
        // recuperamos el ID de la cotización de la base
        Long idCotizacion = (Long) session.getAttribute("idCotizacionActual");

        if (idCotizacion == null) {
            return "redirect:/web/cliente?error=No hay cotización activa para confirmar";
        }

        try {
            // llamamos al servicio solo para finalizar (descontar stock y cambiar estado)
            ventaService.finalizarVenta(idCotizacion);

            // limpiamos la sesión porque la venta ya terminó
            session.removeAttribute("idCotizacionActual");

            return "redirect:/web/cliente?exito=¡Compra realizada con éxito!";

        } catch (Exception e) {
            return "redirect:/web/cliente/carrito?error=" + e.getMessage();
        }
    }

    @GetMapping("/cliente/limpiar")
    public String limpiarCarrito(HttpSession session) {
        Long idCotizacion = (Long) session.getAttribute("idCotizacionActual");
        if (idCotizacion != null) {
            try {
                ventaService.cancelarCotizacion(idCotizacion);
                System.out.println("✅ Cotización " + idCotizacion + " eliminada permanentemente.");
            } catch (Exception e) {
                System.err.println("⚠️ Error al intentar borrar de BD: " + e.getMessage());
            }
        }
        session.removeAttribute("idCotizacionActual");
        return "redirect:/web/cliente";
    }
}
