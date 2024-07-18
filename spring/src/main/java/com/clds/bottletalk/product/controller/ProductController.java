package com.clds.bottletalk.product.controller;

import com.clds.bottletalk.common.Criteria;
import com.clds.bottletalk.common.PagingResponseDTO;
import com.clds.bottletalk.common.ResponseDTO;
import com.clds.bottletalk.product.document.Product;
import com.clds.bottletalk.product.document.ProductDTO;
import com.clds.bottletalk.product.service.ProductService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/products")
@Slf4j
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("")
    public List<Product> getAllProducts(@RequestParam(defaultValue = "_id") String sortBy) {
        Sort sort = Sort.by(sortBy).ascending(); // 기본 정렬을 오름차순으로 설정

        return productService.findAllProducts(sort);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ResponseDTO> getProduct(@PathVariable("productId") String productId) {
        ProductDTO productDTO = productService.findProductByProductId(productId);

        Gson gson = new Gson();
        // Gson을 활용해 productDTO -> JSON String
        String productJson = gson.toJson(productDTO);
        log.info(productJson);

        return ResponseEntity.ok().body(new ResponseDTO(HttpStatus.OK, "조회성공", productDTO));
    }

//    페이징 없는 전체조회 추후 사용 예정
//    @GetMapping("/search")
//    public  ResponseEntity<ResponseDTO> searchProducts(@RequestParam(name = "search", defaultValue = "") String search,
//                                                        @RequestParam(defaultValue = "_id") String sortBy)
//    {
//        Sort sort = Sort.by(sortBy).ascending(); //
//        List<ProductDTO> productDTOList = productService.searchProducts(search,sort);

//        return ResponseEntity.ok().body(new ResponseDTO(HttpStatus.OK,"조회성공",productDTOList));
//    }

    @GetMapping("/search")
    public ResponseEntity<ResponseDTO> getProductListWithPaging(@RequestParam(name = "search") String search,
                                                                @RequestParam(name = "offset", defaultValue = "1") String offset,
                                                                @RequestParam(defaultValue = "kor_name.keyword") String sortBy
    ) {
        Sort sort = Sort.by(sortBy).ascending(); //
        Criteria cri = new Criteria(Integer.valueOf(offset), 5,sort);
        PagingResponseDTO pagingResponseDTO = new PagingResponseDTO();

        Page<ProductDTO> productDTOList = productService.findProductListWithPaging(cri, search);
        pagingResponseDTO.setData(productDTOList);

        // searchKeyword가 있는 경우, keyword를 log로 출력
        if (search != null && !search.isEmpty()) {
            String searchJson = "{ \"searchKeyword\" : \"" + search + "\" }";
            log.info(searchJson);
        }
        return ResponseEntity.ok().body(new ResponseDTO(HttpStatus.OK, "조회성공", pagingResponseDTO));
    }
}
