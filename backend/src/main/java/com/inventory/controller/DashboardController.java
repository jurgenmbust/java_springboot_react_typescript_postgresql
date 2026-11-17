package com.inventory.controller;
import com.inventory.dto.DashboardResponse;
import com.inventory.service.DashboardService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
/** Provides totals and recent records for the dashboard. */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service = service; }
    /** Reads the inventory summary. */
    @GetMapping
    @Operation(summary = "Get inventory totals and recent products")
    public DashboardResponse get() { return service.get(); }
}
