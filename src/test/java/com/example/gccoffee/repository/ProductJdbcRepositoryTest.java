package com.example.gccoffee.repository;

import com.example.gccoffee.model.Category;
import com.example.gccoffee.model.Product;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductJdbcRepositoryTest {

    @Autowired
    ProductRepository repository;
    private Product newProduct = new Product(UUID.randomUUID(), "new-product", Category.COFFEE_BEAN_PACKAGE, 1000);

    @AfterEach
    void afterEach() {
        repository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("상품을 추가할 수 있다.")
    void testInsert() {
        repository.insert(newProduct);
        List<Product> all = repository.findAll();
        assertThat(all.isEmpty(), is(false));
    }

    @Test
    @Order(2)
    @DisplayName("모든 상품을 조회할 수 있다.")
    void testFindAll() {
        List<Product> all = repository.findAll();
        assertThat(all.isEmpty(), is(true));
    }

    @Test
    @Order(3)
    @DisplayName("모든 상품을 삭제할 수 있다.")
    void testDeleteAll() {
        repository.deleteAll();
        List<Product> all = repository.findAll();
        assertThat(all.isEmpty(), is(true));
    }
}