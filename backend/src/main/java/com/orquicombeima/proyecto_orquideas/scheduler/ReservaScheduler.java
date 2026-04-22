package com.orquicombeima.proyecto_orquideas.scheduler;

import com.orquicombeima.proyecto_orquideas.service.StockReservaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Este componente corre tareas automáticas cada cierto tiempo
// Aquí tenemos la tarea que libera las reservas de stock cuando se vencen
@Component
@RequiredArgsConstructor
public class ReservaScheduler {

    // Logger para que podamos ver en los logs cuándo corrió la tarea
    private static final Logger log = LoggerFactory.getLogger(ReservaScheduler.class);

    private final StockReservaService stockReservaService;

    // @Scheduled con fixedRate = 300000 milisegundos = 5 minutos
    // Cada 5 minutos corre automáticamente mientras la aplicación esté encendida
    // El tiempo se cuenta desde el inicio de una ejecución hasta el inicio de la siguiente
    @Scheduled(fixedRate = 300000)
    public void liberarReservasExpiradas() {
        log.info("Tarea programada iniciada: liberando reservas expiradas");
        stockReservaService.liberarReservasExpiradas();
        log.info("Tarea programada finalizada correctamente");
    }
}