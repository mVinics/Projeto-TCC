package com.br.tcc.product.service;

import com.br.tcc.product.dto.CreateProductRequest;
import com.br.tcc.product.dto.ProductResponse;
import com.br.tcc.product.dto.UpdateProductStatusRequest;
import com.br.tcc.product.dto.UpdateProductStockRequest;
import com.br.tcc.product.entity.Product;
import com.br.tcc.product.entity.ProductStatus;
import com.br.tcc.product.repository.ProductRepository;
import com.br.tcc.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        Product product = new Product(
                request.name(),
                request.price(),
                request.stock(),
                ProductStatus.ACTIVE
        );

        Product savedProduct = productRepository.save(product);

        return ProductResponse.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(UUID productId) {
        return ProductResponse.from(findProduct(productId));
    }

    @Transactional
    public ProductResponse updateStock(
            UUID productId,
            UpdateProductStockRequest request
    ) {
        Product product = findProduct(productId);

        product.setStock(request.stock());

        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse updateStatus(
            UUID productId,
            UpdateProductStatusRequest request
    ) {
        Product product = findProduct(productId);

        product.setStatus(request.status());

        return ProductResponse.from(product);
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + productId
                ));
    }
}