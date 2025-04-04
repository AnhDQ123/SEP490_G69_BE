package org.ffb_be.service.product;


import org.ffb_be.dto.product.ProductCreateDTO;
import org.ffb_be.dto.product.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Service
public interface ProductService {
    void save(ProductCreateDTO productCreateDTO,MultipartFile avatar, List<MultipartFile>option) throws IOException;
    void update(Long id,ProductCreateDTO productCreateDTO,MultipartFile avatar, List<MultipartFile>option) throws IOException;
    Page<ProductResponseDTO> findAll(Pageable pageable);
    Page<ProductResponseDTO> findAllByShop(Long id,Pageable pageable);
    List<ProductResponseDTO> findPopularProducts();
    List<ProductResponseDTO> findByCategory(String cat);
    ProductResponseDTO findById(Long id);
    List<ProductResponseDTO> findSimimlarProduct(String name);
    List<Object[]> findTopSellingProductsToday(Long shopId);
    List<Object[]> findTopSellingProductsThisMonth(Long shopId);
    List<Object[]> findTopSellingProductsThisYear(Long shopId);
    List<ProductResponseDTO> findByCategoryByShop(Long shopId);
//    List<ProductResponseDTO> searchHighlySimilarProducts(String query, int page, int size);
//    String syncAllProducts();
}

