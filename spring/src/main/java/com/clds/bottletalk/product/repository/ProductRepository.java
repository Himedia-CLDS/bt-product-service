package com.clds.bottletalk.product.repository;

import com.clds.bottletalk.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {


//    List<Product> findByNameContainingIgnoreCase(String search);

    Page<Product> findByNameContainingIgnoreCase(String search, Pageable paging);
}
