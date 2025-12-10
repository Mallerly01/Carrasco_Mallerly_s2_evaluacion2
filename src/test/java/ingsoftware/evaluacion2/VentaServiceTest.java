package ingsoftware.evaluacion2;

import ingsoftware.evaluacion2.Modelo.Cotizacion;
import ingsoftware.evaluacion2.Modelo.EstadoCotizacion;
import ingsoftware.evaluacion2.Modelo.ItemCotizacion;
import ingsoftware.evaluacion2.Modelo.Mueble;
import ingsoftware.evaluacion2.Modelo.Variante;
import ingsoftware.evaluacion2.Repositorio.CotizacionRepository;
import ingsoftware.evaluacion2.Repositorio.ItemCotizacionRepository;
import ingsoftware.evaluacion2.Repositorio.MuebleRepository;
import ingsoftware.evaluacion2.Repositorio.VarianteRepository;
import ingsoftware.evaluacion2.Servicio.VentaService;
import org.mockito.ArgumentCaptor;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VentaServiceTest {

    @InjectMocks
    private VentaService ventaService;
    @Mock
    private MuebleRepository muebleRepository;
    @Mock
    private VarianteRepository varianteRepository;
    @Mock
    private CotizacionRepository cotizacionRepository;
    @Mock 
    private ItemCotizacionRepository itemRepository;

    @Test
    void testAgregarItem_AplicaPrecioConVariante() {
        Long muebleId = 1L;
        Long varianteId = 101L;

        Mueble muebleBase = new Mueble();
        muebleBase.setIdMueble(muebleId);
        muebleBase.setPrecioBase(100.00);
        muebleBase.setStock(10);

        Variante variante = new Variante();
        variante.setId(varianteId);
        variante.setAumentoPrecio(15.00);

        Cotizacion cotizacionGuardadaMock = new Cotizacion();
        cotizacionGuardadaMock.setId(10L);

        when(muebleRepository.findById(muebleId)).thenReturn(Optional.of(muebleBase));
        when(varianteRepository.findById(varianteId)).thenReturn(Optional.of(variante));

        when(cotizacionRepository.save(any(Cotizacion.class))).thenReturn(cotizacionGuardadaMock);

        when(itemRepository.findByCotizacionId(any())).thenReturn(new ArrayList<>());

        ventaService.agregarItem(null, muebleId, varianteId, 1);

        ArgumentCaptor<ItemCotizacion> itemCaptor = ArgumentCaptor.forClass(ItemCotizacion.class);
        verify(itemRepository).save(itemCaptor.capture());

        ItemCotizacion itemGuardado = itemCaptor.getValue();

        assertEquals(115.00, itemGuardado.getPrecioUnitarioFinal(), 0.001,
                "El precio unitario debe incluir el aumento de la variante.");
    }

    @Test
    void testConfirmarVenta_Exito() {
        Long cotizacionId = 5L;
        Mueble mueble = new Mueble();
        mueble.setIdMueble(1L);
        mueble.setStock(10); 

        ItemCotizacion item = new ItemCotizacion();
        item.setMueble(mueble);
        item.setCantidad(3); 

        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setId(cotizacionId);
        cotizacion.setEstado(EstadoCotizacion.PENDIENTE);
        cotizacion.setItems(List.of(item));

        when(cotizacionRepository.findById(cotizacionId)).thenReturn(Optional.of(cotizacion));

        when(cotizacionRepository.save(any(Cotizacion.class))).thenReturn(cotizacion);
        Cotizacion resultado = ventaService.confirmarVenta(cotizacionId);

        assertEquals(EstadoCotizacion.VENDIDA, resultado.getEstado(), "El estado debe cambiar a VENDIDA.");
        assertEquals(7, mueble.getStock(), "El stock debe decrementar correctamente.");
        verify(cotizacionRepository, times(1)).save(any(Cotizacion.class)); 
    }
    @Test
    void testConfirmarVenta_StockInsuficiente_Falla() {
        Long cotizacionId = 6L;
        Mueble mueble = new Mueble();
        mueble.setIdMueble(2L);
        mueble.setStock(5); 

        ItemCotizacion item = new ItemCotizacion();
        item.setMueble(mueble);
        item.setCantidad(8);
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setId(cotizacionId);
        cotizacion.setItems(List.of(item));

        when(cotizacionRepository.findById(cotizacionId)).thenReturn(Optional.of(cotizacion));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            ventaService.confirmarVenta(cotizacionId);
        }, "Debe lanzar una RuntimeException por stock insuficiente.");

        assertTrue(thrown.getMessage().contains("stock insuficiente"), "El mensaje de error debe indicar stock.");
        assertEquals(5, mueble.getStock(), "El stock NO debe cambiar al fallar la transacción.");
        verify(cotizacionRepository, never()).save(any()); 
    }
}