package com.br.tcc.product.controller;

import com.br.tcc.product.dto.CreateProductRequest;
import com.br.tcc.product.dto.ProductResponse;
import com.br.tcc.product.dto.UpdateProductStatusRequest;
import com.br.tcc.product.dto.UpdateProductStockRequest;
import com.br.tcc.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            @Valid @RequestBody CreateProductRequest request
    ) {
        return productService.create(request);
    }

    @GetMapping
    public List<ProductResponse> list() {
        return productService.list();
    }

    @GetMapping("/{productId}")
    public ProductResponse getById(
            @PathVariable UUID productId
    ) {
        return productService.getById(productId);
    }

    @PatchMapping("/{productId}/stock")
    public ProductResponse updateStock(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductStockRequest request
    ) {
        return productService.updateStock(productId, request);
    }

    @PatchMapping("/{productId}/status")
    public ProductResponse updateStatus(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductStatusRequest request
    ) {
        return productService.updateStatus(productId, request);
    }
}