package ingsoftware.evaluacion2.Servicio;

import ingsoftware.evaluacion2.Modelo.*;
import ingsoftware.evaluacion2.Repositorio.CotizacionRepository;
import ingsoftware.evaluacion2.Repositorio.ItemCotizacionRepository;
import ingsoftware.evaluacion2.Repositorio.MuebleRepository;
import ingsoftware.evaluacion2.Repositorio.VarianteRepository;
import ingsoftware.evaluacion2.dto.CotizacionRequestDTO;
import ingsoftware.evaluacion2.dto.ItemCotizacionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    @Autowired
    private CotizacionRepository cotizacionRepository;

    @Autowired
    private ItemCotizacionRepository itemCotizacionRepository;

    @Autowired
    private MuebleRepository muebleRepository;

    @Autowired
    private VarianteRepository varianteRepository;
    @Autowired
    private ItemCotizacionRepository itemRepository;

    @Transactional
    public Cotizacion agregarItem(Long idCotizacion, Long idMueble, Long idVariante, int cantidad) {
        Cotizacion cotizacion = null;

        // intentar recupar id si lo hay
        if (idCotizacion != null) {
            cotizacion = cotizacionRepository.findById(idCotizacion).orElse(null);
        }

        // Si el ID venía nulo o si no existía en la BD creamos uno nuevo
        if (cotizacion == null) {
            cotizacion = new Cotizacion();
            cotizacion.setFecha(LocalDate.now());
            cotizacion.setEstado(EstadoCotizacion.PENDIENTE);
            cotizacion = cotizacionRepository.save(cotizacion);
        }

        // obtener mueble
        Mueble mueble = muebleRepository.findById(idMueble)
                .orElseThrow(() -> new RuntimeException("Mueble no encontrado"));

        // buscar si existe un carrito
        ItemCotizacion itemExistente = null;

        List<ItemCotizacion> itemsActuales = itemRepository.findByCotizacionId(cotizacion.getId());

        for (ItemCotizacion item : itemsActuales) {
            boolean mismoMueble = item.getMueble().getIdMueble().equals(idMueble);
            Long varianteIdItem = (item.getVariante() != null) ? item.getVariante().getId() : null;
            boolean mismaVariante = (idVariante == null && varianteIdItem == null) ||
                    (idVariante != null && idVariante.equals(varianteIdItem));

            if (mismoMueble && mismaVariante) {
                itemExistente = item;
                break;
            }
        }

        // logica de si el producto seleccionado ya existia en el carrito
        if (itemExistente != null) {
            int nuevaCantidadTotal = itemExistente.getCantidad() + cantidad;

            if (nuevaCantidadTotal > mueble.getStock()) {
                throw new RuntimeException("Stock insuficiente. Total en carrito: " + nuevaCantidadTotal + ". Disponible: " + mueble.getStock());
            }

            itemExistente.setCantidad(nuevaCantidadTotal);
            itemRepository.save(itemExistente);

        } else {
            // logica crear nuevo item
            if (cantidad > mueble.getStock()) {
                throw new RuntimeException("Stock insuficiente. Pides " + cantidad + " pero solo quedan " + mueble.getStock());
            }

            Variante variante = null;
            double precioFinal = mueble.getPrecioBase();

            if (idVariante != null) {
                variante = varianteRepository.findById(idVariante).orElse(null);
                if (variante != null) {
                    precioFinal += variante.getAumentoPrecio();
                }
            }

            ItemCotizacion newItem = new ItemCotizacion();
            newItem.setCotizacion(cotizacion);
            newItem.setMueble(mueble);
            newItem.setVariante(variante);
            newItem.setCantidad(cantidad);
            newItem.setPrecioUnitarioFinal(precioFinal);

            itemRepository.save(newItem);
        }

        return cotizacion;
    }

    @Transactional
    public Cotizacion confirmarVenta(Long idCotizacion) {

        Cotizacion cotizacion = cotizacionRepository.findById(idCotizacion)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

        if (cotizacion.getEstado() == EstadoCotizacion.VENDIDA) {
            throw new RuntimeException("Esta cotización ya fue confirmada como venta.");
        }

        for (ItemCotizacion item : cotizacion.getItems()) {
            Mueble mueble = item.getMueble();
            if (mueble.getStock() < item.getCantidad()) {
                throw new RuntimeException("stock insuficiente para " + mueble.getNombreMueble());
            }
        }

        for (ItemCotizacion item : cotizacion.getItems()) {
            Mueble mueble = item.getMueble();
            int nuevoStock = mueble.getStock() - item.getCantidad();
            mueble.setStock(nuevoStock);
        }

        cotizacion.setEstado(EstadoCotizacion.VENDIDA);
        return cotizacionRepository.save(cotizacion);
    }

    
    public Optional<Cotizacion> obtenerCotizacionPorId(Long id) {
        return cotizacionRepository.findById(id);
    }

    public List<Cotizacion> listarTodasLasCotizaciones() {
        return cotizacionRepository.findAll();
    }
    @Transactional
    public void finalizarVenta(Long idCotizacion) {
        // recuperar la cotización
        Cotizacion cotizacion = cotizacionRepository.findById(idCotizacion)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

        // iterar sobre cada item para revalidar stock
        List<ItemCotizacion> items = itemRepository.findByCotizacionId(idCotizacion);

        for (ItemCotizacion item : items) {
            Mueble mueble = item.getMueble();

            // validar si hay suficientes unidades (por si el stock se actualizo mientras el producto estaba en el carrito)
            if (item.getCantidad() > mueble.getStock()) {
                throw new RuntimeException("¡Lo sentimos! Mientras cotizabas, el stock de '" +
                        mueble.getNombreMueble() +
                        "' cambió. Ya no hay suficientes unidades.");
            }

            // descontar stock
            mueble.setStock(mueble.getStock() - item.getCantidad());
            muebleRepository.save(mueble);
        }

        // cerrar la venta
        cotizacion.setEstado(EstadoCotizacion.VENDIDA); // O PAGADA
        cotizacionRepository.save(cotizacion);
    }
    public List<ItemCotizacion> obtenerItemsDeCotizacion(Long idCotizacion) {
        return itemRepository.findByCotizacionId(idCotizacion);
    }
    @Transactional
    public void cancelarCotizacion(Long idCotizacion) {
        if (idCotizacion != null) {
            // borramos los items
            itemRepository.borrarItemsNativo(idCotizacion);

            // borramos la cotización
            cotizacionRepository.borrarCotizacionNativo(idCotizacion);
        }
    }

}
