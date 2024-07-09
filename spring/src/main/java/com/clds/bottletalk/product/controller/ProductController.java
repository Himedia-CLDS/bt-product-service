package com.clds.bottletalk.product.controller;


import com.clds.bottletalk.common.Criteria;
import com.clds.bottletalk.common.PageDTO;
import com.clds.bottletalk.common.PagingResponseDTO;
import com.clds.bottletalk.common.ResponseDTO;
import com.clds.bottletalk.product.model.ProductDTO;
import com.clds.bottletalk.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;



@Controller
@RequestMapping("/v1/api/products")
@Slf4j
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ResponseDTO> getProduct(@PathVariable("productId") long productId){
        ProductDTO productDTO = productService.findProductByProductId(productId);



        log.info("selected product name => {}", productDTO.getName());
        log.info("selected product DTO => {}", productDTO.toString());


        return ResponseEntity.ok().body(new ResponseDTO(HttpStatus.OK,"조회성공",productDTO));

    }


    @GetMapping()
    public ResponseEntity<ResponseDTO> getProductListWithPaging(@RequestParam(name = "search", defaultValue = "") String search,
                                                      @RequestParam(name = "offset", defaultValue = "1") String offset){
        Criteria cri = new Criteria(Integer.valueOf(offset),5);
        PagingResponseDTO pagingResponseDTO = new PagingResponseDTO();
        Page<ProductDTO> productDTOList = productService.findProductListWithPaging(cri,search);
        pagingResponseDTO.setData(productDTOList);

        log.info("searched Keyword => {}  " , search);

        return ResponseEntity.ok().body(new ResponseDTO(HttpStatus.OK,"조회성공",pagingResponseDTO));

    }















}
