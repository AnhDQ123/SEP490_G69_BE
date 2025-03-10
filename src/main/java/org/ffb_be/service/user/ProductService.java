package org.ffb_be.service.user;

import org.ffb_be.dto.auth.product.ProductCreateDTO;
import org.ffb_be.dto.auth.product.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Service
public interface ProductService {
   void save(ProductCreateDTO productCreateDTO,MultipartFile avatar, List<MultipartFile>option) throws IOException;
    Page<ProductResponseDTO> findAll(Long id,Pageable pageable);
}
