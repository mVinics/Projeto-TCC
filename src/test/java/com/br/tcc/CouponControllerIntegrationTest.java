package com.br.tcc;

import com.br.tcc.TestcontainersConfiguration;
import com.br.tcc.coupon.entity.Coupon;
import com.br.tcc.coupon.entity.CouponStatus;
import com.br.tcc.coupon.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CouponControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponRepository couponRepository;

    @BeforeEach
    void setUp() {
        couponRepository.deleteAll();
    }

    @Test
    void shouldCreateCoupon() throws Exception {
        String request = """
                {
                  "code": "PROMO20",
                  "discountValue": 20.00,
                  "minimumOrderValue": 100.00,
                  "validFrom": "2026-10-01",
                  "validUntil": "2026-10-31"
                }
                """;

        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("PROMO20"))
                .andExpect(jsonPath("$.discountValue").value(20.00))
                .andExpect(jsonPath("$.minimumOrderValue").value(100.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldRejectDuplicatedCouponCode() throws Exception {
        couponRepository.save(
                new Coupon(
                        "PROMO20",
                        new BigDecimal("20.00"),
                        new BigDecimal("100.00"),
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        CouponStatus.ACTIVE
                )
        );

        String request = """
                {
                  "code": "PROMO20",
                  "discountValue": 10.00,
                  "minimumOrderValue": 50.00,
                  "validFrom": "2026-10-01",
                  "validUntil": "2026-10-31"
                }
                """;

        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Resource conflict"));
    }

    @Test
    void shouldGetCouponByCode() throws Exception {
        couponRepository.save(
                new Coupon(
                        "PROMO30",
                        new BigDecimal("30.00"),
                        new BigDecimal("150.00"),
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        CouponStatus.ACTIVE
                )
        );

        mockMvc.perform(get("/api/v1/coupons/{code}", "PROMO30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PROMO30"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnNotFoundForUnknownCoupon() throws Exception {
        mockMvc.perform(get("/api/v1/coupons/{code}", "UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldRejectInvalidCoupon() throws Exception {
        String request = """
                {
                  "code": "",
                  "discountValue": -1.00,
                  "minimumOrderValue": -10.00,
                  "validFrom": null,
                  "validUntil": null
                }
                """;

        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.errors.code").exists())
                .andExpect(jsonPath("$.errors.discountValue").exists())
                .andExpect(jsonPath("$.errors.minimumOrderValue").exists())
                .andExpect(jsonPath("$.errors.validFrom").exists())
                .andExpect(jsonPath("$.errors.validUntil").exists());
    }
}