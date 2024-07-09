package com.clds.bottletalk.product.service;

import com.clds.bottletalk.common.Criteria;
import com.clds.bottletalk.product.model.Product;
import com.clds.bottletalk.product.model.ProductDTO;
import com.clds.bottletalk.product.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService{

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public ProductService(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    public ProductDTO findProductByProductId(long productId) {

        Product product = productRepository.findById(productId).get();
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        return productDTO;

    }



    public Page<ProductDTO> findProductListWithPaging(Criteria cri, String search) {
        int index = cri.getPageNum() -1;
        int count = cri.getAmount();

        Pageable paging = PageRequest.of(index, count, Sort.by("id").descending());
        Page<Product> result = productRepository.findByNameContainingIgnoreCase(search,paging);

        Page<ProductDTO> productDTOList = result.map(product -> modelMapper.map(product, ProductDTO.class));
        return productDTOList;
    }
}
