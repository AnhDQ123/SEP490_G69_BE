package org.ffb_be.service.user;

import org.ffb_be.dto.auth.product.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
public interface ProductService {
    void save(EmployeeCreateDTO employeeCreateDTO, MultipartFile avatar) throws IOException;
    Page<ProductResponseDTO> findAll(Long id,Pageable pageable);
}
