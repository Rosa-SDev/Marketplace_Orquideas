package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.CrearPedidoDTO;
import com.orquicombeima.proyecto_orquideas.dto.DireccionEnvioDTO;
import com.orquicombeima.proyecto_orquideas.dto.ItemPedidoDTO;
import com.orquicombeima.proyecto_orquideas.dto.PedidoDTO;
import com.orquicombeima.proyecto_orquideas.model.Carrito;
import com.orquicombeima.proyecto_orquideas.model.DireccionEnvio;
import com.orquicombeima.proyecto_orquideas.model.ItemCarrito;
import com.orquicombeima.proyecto_orquideas.model.ItemPedido;
import com.orquicombeima.proyecto_orquideas.model.Pedido;
import com.orquicombeima.proyecto_orquideas.model.Producto;
import com.orquicombeima.proyecto_orquideas.model.Usuario;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPago;
import com.orquicombeima.proyecto_orquideas.repository.CarritoRepository;
import com.orquicombeima.proyecto_orquideas.repository.PedidoRepository;
import com.orquicombeima.proyecto_orquideas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// Servicio que orquesta todo el flujo de creación y consulta de pedidos
//  - crearPedido: convierte el carrito del usuario en un Pedido, genera el link de Wompi, confirma reservas y vacía el carrito
//  - obtenerHistorial: lista los pedidos del usuario autenticado
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final StockReservaService stockReservaService;
    private final CarritoService carritoService;
    private final PagoWompiService pagoWompiService;

    // Costo de envío fijo por ahora ($10.000 COP). Si más adelante hay tarifas variables, se mueve a la BD
    private static final double COSTO_ENVIO_FIJO = 10000.0;

    // POST /api/pedidos
    // Toda la operación es @Transactional: si algo falla a la mitad, se hace rollback completo
    // (no queremos crear el pedido pero olvidar vaciar el carrito, por ejemplo)
    @Transactional
    public PedidoDTO crearPedido(String emailUsuario, CrearPedidoDTO dto) {
        // 1. Buscamos al usuario por su email (sale del JWT en el filtro de seguridad)
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + emailUsuario));

        // 2. Buscamos el carrito del usuario
        Carrito carrito = carritoRepository.findByUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado para: " + emailUsuario));

        // 3. Validamos que el carrito tenga items (no se puede pedir nada vacío)
        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("No se puede crear un pedido con el carrito vacío");
        }

        // 4. Creamos la dirección de envío con los datos que vinieron en el body
        DireccionEnvio direccion = mapearADireccionEnvio(dto.getDireccionEnvio());

        // 5. Creamos el pedido vacío y le asociamos usuario y dirección
        // El estado PENDIENTE y la fechaPedido los pone solito el @PrePersist de la entidad
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDireccionEnvio(direccion);

        // 6. Convertimos cada item del carrito en un item del pedido
        // Capturamos precioUnitario AHORA (snapshot): si el admin cambia el precio mañana,
        // este pedido conserva el precio que el cliente realmente pagó
        double subtotal = 0.0;
        List<ItemPedido> itemsPedido = new ArrayList<>();
        for (ItemCarrito itemCarrito : carrito.getItems()) {
            Producto producto = itemCarrito.getProducto();
            int cantidad = itemCarrito.getCantidad();
            double precioUnitario = producto.getPrecio();
            double subtotalItem = precioUnitario * cantidad;

            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setPedido(pedido);
            itemPedido.setProducto(producto);
            itemPedido.setCantidad(cantidad);
            itemPedido.setPrecioUnitario(precioUnitario);
            itemPedido.setSubtotal(subtotalItem);

            itemsPedido.add(itemPedido);
            subtotal = subtotal + subtotalItem;
        }
        pedido.setItems(itemsPedido);

        // 7. Calculamos los totales (subtotal de productos + costo fijo de envío)
        pedido.setSubtotal(subtotal);
        pedido.setCostoEnvio(COSTO_ENVIO_FIJO);
        pedido.setTotal(subtotal + COSTO_ENVIO_FIJO);

        // 8. Guardamos el pedido
        // Por las anotaciones cascade=ALL en Pedido, también se guardan en cascada los items y la dirección
        pedido = pedidoRepository.save(pedido);

        // 9. Generamos el link de Wompi: esto crea el PagoWompi en BD, lo asocia al pedido y devuelve la URL
        String linkPago = pagoWompiService.generarEnlacePago(pedido);

        // 10. Confirmamos las reservas del carrito: descuenta el stock real y limpia el stockReservado
        // Los items que estaban "apartados" pasan a estar realmente vendidos
        stockReservaService.confirmarReservas(carrito.getId());

        // 11. Vaciamos el carrito (limpia todos los items)
        carritoService.vaciarCarrito(emailUsuario);

        // 12. Devolvemos el DTO con el link de pago para que el frontend redirija al usuario a Wompi
        return mapearAPedidoDTO(pedido, linkPago);
    }

    // GET /api/pedidos/historial
    // Devuelve los pedidos del usuario autenticado, los más recientes primero
    @Transactional(readOnly = true)
    public List<PedidoDTO> obtenerHistorial(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + emailUsuario));

        // El repository ya tiene el método con orderBy fechaPedido descendente
        List<Pedido> pedidos = pedidoRepository.findByUsuarioIdOrderByFechaPedidoDesc(usuario.getId());

        // Convertimos cada pedido a DTO; en el historial NO mandamos linkPago (ya pasó el momento de pagar)
        List<PedidoDTO> resultado = new ArrayList<>();
        for (Pedido pedido : pedidos) {
            resultado.add(mapearAPedidoDTO(pedido, null));
        }
        return resultado;
    }


    // ----------------- helpers de mapeo entre DTO y entidad -----------------

    // Crea una entidad DireccionEnvio desde el DTO que vino en el body del POST
    private DireccionEnvio mapearADireccionEnvio(DireccionEnvioDTO dto) {
        DireccionEnvio direccion = new DireccionEnvio();
        direccion.setNombreDestinatario(dto.getNombreDestinatario());
        direccion.setTelefonoDestinatario(dto.getTelefonoDestinatario());
        direccion.setDepartamento(dto.getDepartamento());
        direccion.setCiudad(dto.getCiudad());
        direccion.setDireccion(dto.getDireccion());
        direccion.setCodigoPostal(dto.getCodigoPostal());
        direccion.setInstruccionesAdicionales(dto.getInstruccionesAdicionales());
        return direccion;
    }

    // Convierte la entidad DireccionEnvio en DTO para devolverla al frontend
    private DireccionEnvioDTO mapearADireccionEnvioDTO(DireccionEnvio direccion) {
        return DireccionEnvioDTO.builder()
                .id(direccion.getId())
                .nombreDestinatario(direccion.getNombreDestinatario())
                .telefonoDestinatario(direccion.getTelefonoDestinatario())
                .departamento(direccion.getDepartamento())
                .ciudad(direccion.getCiudad())
                .direccion(direccion.getDireccion())
                .codigoPostal(direccion.getCodigoPostal())
                .instruccionesAdicionales(direccion.getInstruccionesAdicionales())
                .build();
    }

    // Convierte un ItemPedido en su DTO, aplanando los datos del producto
    private ItemPedidoDTO mapearAItemPedidoDTO(ItemPedido item) {
        return ItemPedidoDTO.builder()
                .id(item.getId())
                .idProducto(item.getProducto().getId())
                .nombreProducto(item.getProducto().getNombre())
                .imagenUrl(item.getProducto().getImageUrl())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(item.getSubtotal())
                .build();
    }

    // Convierte el Pedido completo en su DTO de respuesta
    // El linkPago viene como parámetro porque solo se llena al crear (no en el historial)
    private PedidoDTO mapearAPedidoDTO(Pedido pedido, String linkPago) {
        // Mapeamos cada item con un for normal para que sea fácil de leer
        List<ItemPedidoDTO> itemsDTO = new ArrayList<>();
        for (ItemPedido item : pedido.getItems()) {
            itemsDTO.add(mapearAItemPedidoDTO(item));
        }

        // Si el pedido tiene pago asociado (siempre debería en POST, opcional en historial), sacamos sus datos
        EstadoPago estadoPago = null;
        String referenciaPago = null;
        if (pedido.getPago() != null) {
            estadoPago = pedido.getPago().getEstado();
            referenciaPago = pedido.getPago().getReferenciaPago();
        }

        return PedidoDTO.builder()
                .id(pedido.getId())
                .estado(pedido.getEstado())
                .estadoPago(estadoPago)
                .items(itemsDTO)
                .direccionEnvio(mapearADireccionEnvioDTO(pedido.getDireccionEnvio()))
                .subtotal(pedido.getSubtotal())
                .costoEnvio(pedido.getCostoEnvio())
                .total(pedido.getTotal())
                .fechaPedido(pedido.getFechaPedido())
                .linkPago(linkPago)
                .referenciaPago(referenciaPago)
                .build();
    }
}