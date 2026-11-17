package com.inventory.dto;
import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
/** Contains database totals and small lists for the dashboard. */
public record DashboardResponse(long totalProducts, long totalCategories,
    @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal totalInventoryValue,
    long lowStockCount, int lowStockThreshold, List<ProductResponse> recentProducts, List<ProductResponse> lowStockProducts) {}
