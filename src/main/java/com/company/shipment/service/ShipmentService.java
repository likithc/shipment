package com.company.shipment.service;

import com.company.shipment.model.Shipment;
import com.company.shipment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Collection;

@Service
public class ShipmentService {
    private final ShipmentRepository repository;

    public ShipmentService(ShipmentRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void initDatabase() {
        if (repository.findByTrackingId("TRK1001").isEmpty()) {
            repository.save(new Shipment(null, "TRK1001", "Hyderabad", "Bangalore", "CREATED"));
        }
    }

    public Collection<Shipment> findAll() {
        return repository.findAll();
    }

    public Shipment findByTrackingId(String trackingId) {
        return repository.findByTrackingId(trackingId).orElse(null);
    }

    public Shipment createShipment(Shipment shipment) {
        return repository.save(shipment);
    }
}
