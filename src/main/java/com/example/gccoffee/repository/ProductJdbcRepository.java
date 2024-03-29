package com.example.gccoffee.repository;

import com.example.gccoffee.model.Category;
import com.example.gccoffee.model.Product;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.example.gccoffee.Utils.toLocalDateTime;
import static com.example.gccoffee.Utils.toUUID;

@Repository
@Profile({"prod", "test"})
public class ProductJdbcRepository implements ProductRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProductJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Product> findAll() {
        return jdbcTemplate.query("select * from products", productRowMapper);
    }

    @Override
    public Product insert(Product product) {
        var update = jdbcTemplate.update("insert into products (product_id, product_name, category, price, description, created_at, updated_at)" +
                " VALUES(UUID_TO_BIN(:productId), :productName, :category, :price, :description, :createdAt, :updatedAt)", toParamMap(product));
        if(update != 1) {
            throw new RuntimeException("Nothing was inserted");
        }
        return product;
    }

    @Override
    public Product update(Product product) {
        int update = jdbcTemplate.update(
                "update products set product_name = :productName, category = :category, price = :price, description = :description, created_at = :createdAt, updated_at = :updatedAt " +
                        "where product_id = UUID_TO_BIN(:productId)",
                toParamMap(product)
        );

        if(update != 1) {
            throw new RuntimeException("Nothing was updated");
        }
        return product;
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        try {
            return Optional.of(
                    jdbcTemplate.queryForObject("select * from products where product_id = UUID_TO_BIN(:productId)",
                            Collections.singletonMap("productId", productId.toString().getBytes()), productRowMapper)
            );
        } catch(EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Product> findByName(String productName) {
        try {
            return Optional.of(
                    jdbcTemplate.queryForObject("select * from products where product_name = :productName",
                            Collections.singletonMap("productName", productName), productRowMapper)
            );
        } catch(EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findByCategory(Category category) {
        return jdbcTemplate.query(
                "select * from products where category = :category",
                Collections.singletonMap("category", category.toString()),
                productRowMapper);
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("delete from products", Collections.emptyMap());
    }

    private static final RowMapper<Product> productRowMapper = (resultSet, i) -> {
        return new Product(toUUID(resultSet.getBytes("product_id")),
                resultSet.getString("product_name"),
                Category.valueOf(resultSet.getString("category")),
                resultSet.getLong("price"),
                resultSet.getString("description"),
                toLocalDateTime(resultSet.getTimestamp("created_at")),
                toLocalDateTime(resultSet.getTimestamp("updated_at")));
    };

    private Map<String, Object> toParamMap(Product product) {
        var paramMap = new HashMap<String, Object>();

        paramMap.put("productId", product.getProductId().toString().getBytes());
        paramMap.put("productName", product.getProductName());
        paramMap.put("category", product.getCategory().toString());
        paramMap.put("price", product.getPrice());
        paramMap.put("description", product.getDescription());
        paramMap.put("createdAt", product.getCreatedAt());
        paramMap.put("updatedAt", product.getUpdatedAt());
        return paramMap;
    }
}
