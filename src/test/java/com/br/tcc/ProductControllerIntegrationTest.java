package com.br.tcc;

import com.br.tcc.TestcontainersConfiguration;
import com.br.tcc.product.entity.Product;
import com.br.tcc.product.entity.ProductStatus;
import com.br.tcc.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void shouldCreateProduct() throws Exception {
        String request = """
                {
                  "name": "Keyboard",
                  "price": 250.00,
                  "stock": 10
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(250.00))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldRejectInvalidProduct() throws Exception {
        String request = """
                {
                  "name": "",
                  "price": -10.00,
                  "stock": -1
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.stock").exists());
    }

    @Test
    void shouldListProducts() throws Exception {
        productRepository.save(
                new Product(
                        "Mouse",
                        new BigDecimal("100.00"),
                        20,
                        ProductStatus.ACTIVE
                )
        );

        productRepository.save(
                new Product(
                        "Monitor",
                        new BigDecimal("900.00"),
                        5,
                        ProductStatus.ACTIVE
                )
        );

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetProductById() throws Exception {
        Product product = productRepository.save(
                new Product(
                        "Mouse",
                        new BigDecimal("100.00"),
                        20,
                        ProductStatus.ACTIVE
                )
        );

        mockMvc.perform(get(
                        "/api/v1/products/{productId}",
                        product.getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId().toString()))
                .andExpect(jsonPath("$.name").value("Mouse"));
    }

    @Test
    void shouldReturnNotFoundForUnknownProduct() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/products/00000000-0000-0000-0000-000000000001"
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldUpdateProductStock() throws Exception {
        Product product = productRepository.save(
                new Product(
                        "Keyboard",
                        new BigDecimal("250.00"),
                        10,
                        ProductStatus.ACTIVE
                )
        );

        String request = """
                {
                  "stock": 30
                }
                """;

        mockMvc.perform(patch(
                        "/api/v1/products/{productId}/stock",
                        product.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(30));
    }

    @Test
    void shouldUpdateProductStatus() throws Exception {
        Product product = productRepository.save(
                new Product(
                        "Keyboard",
                        new BigDecimal("250.00"),
                        10,
                        ProductStatus.ACTIVE
                )
        );

        String request = """
                {
                  "status": "INACTIVE"
                }
                """;

        mockMvc.perform(patch(
                        "/api/v1/products/{productId}/status",
                        product.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }
}