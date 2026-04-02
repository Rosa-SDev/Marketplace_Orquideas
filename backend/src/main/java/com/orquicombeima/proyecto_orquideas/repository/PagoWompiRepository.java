package com.orquicombeima.proyecto_orquideas.repository;

import com.orquicombeima.proyecto_orquideas.model.PagoWompi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagoWompiRepository extends JpaRepository<PagoWompi, Long> {
    //Metodo para encontrar un pago utilizando la referencia de pago
    Optional<PagoWompi> findByReferenciaPago(String referenciaPago);

    //Metodo para encontrar un pago utilizando el numero de la transaccion de Wompi
    Optional<PagoWompi> findByTransaccionWompiId(String transaccionWompiId);
}
