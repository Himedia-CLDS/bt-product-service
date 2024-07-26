package com.clds.bottletalk.product.service;

import com.clds.bottletalk.common.Criteria;
import com.clds.bottletalk.product.document.Product;
import com.clds.bottletalk.product.document.ProductDTO;
import com.clds.bottletalk.product.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService{

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public ProductService(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    public ProductDTO findProductByProductId(String productId) {

        Product product = productRepository.findById(productId);
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        return productDTO;
    }

    public Page<ProductDTO> findProductListWithPaging(Criteria cri, String search) {
        int index = cri.getPageNum() -1;
        int count = cri.getAmount();
        Page<Product> result;


        Pageable paging = PageRequest.of(index, count, Sort.by("id.keyword").descending());

        if(isKorean(search)) {

         result = productRepository.findByKorNameContainingIgnoreCase(search, paging);

        }else{

          result = productRepository.findByEngNameContainingIgnoreCase(search, paging);
        }


        Page<ProductDTO> productDTOList = result.map(product -> modelMapper.map(product, ProductDTO.class));
        return productDTOList;
    }

    public List<Product> findAllProducts(Sort sort) {
        List<Product> productList = (List)productRepository.findAll(sort);

        return productList;
    }

//    public List<ProductDTO> searchProducts(String search,Sort sort) {
//
//        if(isKorean(search)){
//            List<Product> productList = productRepository.findByKorNameContaining(search,sort);
//            List<ProductDTO> productDTOList =  productList.stream()
//                    .map(product -> modelMapper.map(product, ProductDTO.class)).toList();
//
//            return productDTOList;
//
//        }else{
//            List<Product> productList = productRepository.findByEngNameContaining(search,sort);
//            List<ProductDTO> productDTOList = productList.stream()
//                    .map(product -> modelMapper.map(product, ProductDTO.class)).toList();
//
//            return productDTOList;
//        }
//    }
    //페이징 없는 상품검색 입니다.

    private boolean isKorean(String search) {
        return search.codePoints().anyMatch(ch -> (ch >= 0x1100 && ch <= 0x11FF) || (ch >= 0x3130 && ch <= 0x318F) || (ch >= 0xAC00 && ch <= 0xD7A3));
    }
}
