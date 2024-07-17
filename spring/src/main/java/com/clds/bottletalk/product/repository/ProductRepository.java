package com.clds.bottletalk.product.repository;


import com.clds.bottletalk.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends ElasticsearchRepository<Product, String> {


//    List<Product> findByNameContainingIgnoreCase(String search);

    Page<Product> findByKorNameContainingIgnoreCase(String search, Pageable paging);

    Product findById(String productId);

    List<Product> findByKorNameContaining(String search, Sort sort);

    List<Product> findByEngNameContaining(String search, Sort sort);
}
