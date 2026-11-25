package com.nivora.nivora.metricas;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Protegido por hasRole("ADMIN") no SecurityConfig (/api/admin/**). */
@RestController
@RequestMapping("/api/admin/metricas")
public class MetricasController {

    private final MetricasService service;

    public MetricasController(MetricasService service) {
        this.service = service;
    }

    @GetMapping
    public MetricasResponse resumo() {
        return service.resumo();
    }
}
