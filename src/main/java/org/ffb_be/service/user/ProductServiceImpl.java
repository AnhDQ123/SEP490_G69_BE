package org.ffb_be.service.user;


import org.ffb_be.dto.auth.product.ProductResponseDTO;

import org.ffb_be.repository.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

//        public void save(ProductCreateDTO employeeCreateDTO, Map<String, List<MultipartFile>> files) throws IOException {
//        Product employee = new Product();
//        BeanUtils.copyProperties(employeeCreateDTO, employee);
//
//        Department department = departmentRepository.findById(employeeCreateDTO.getDepartment())
//                .orElseThrow(() -> new EntityNotFoundException("Department not found!"));
//
//        Role role = roleRepository.findById(employeeCreateDTO.getRole())
//                .orElseThrow(() -> new EntityNotFoundException("Role not found!"));
//
//        employeeRepository.findByUsername(employeeCreateDTO.getUsername()).ifPresent((x) -> {
//            throw new NonUniqueResultException("Username already exists!");
//        });
//
//        employeeRepository.findByEmail(employeeCreateDTO.getEmail()).ifPresent((x) -> {
//            throw new NonUniqueResultException("Email already exists!");
//        });
//
//        employeeRepository.findByPhone(employeeCreateDTO.getPhone()).ifPresent((x) -> {
//            throw new NonUniqueResultException("Phone already exists!");
//        });
//
//        List<EmployeeAvatar> avatarEntities = new ArrayList<>();
//        List<EmployeeDocument> documentEntities = new ArrayList<>();
//
//
//        if (files.containsKey("product")) {
//            for (MultipartFile file : files.get("product")) {
//                String url = cloudinaryUpload.uploadFile(file);
//                EmployeeAvatar avatar = new EmployeeAvatar();
//                avatar.setImageUrl(url);
//                avatar.setEmployee(employee);
//                avatarEntities.add(avatar);
//            }
//        }
//
//        // Xử lý ảnh tài liệu (Document)
//        if (files.containsKey("option")) {
//            for (MultipartFile file : files.get("option")) {
//                String url = cloudinaryUpload.uploadFile(file);
//                EmployeeDocument document = new EmployeeDocument();
//                document.setDocumentUrl(url);
//                document.setEmployee(employee);
//                documentEntities.add(document);
//            }
//        }
//
//        employee.setAvatars(avatarEntities);
//        employee.setDocuments(documentEntities);
//
//        employeeRepository.save(employee);
//    }

    @Override
    public Page<ProductResponseDTO> findAll(Long id,Pageable pageable) {
        return productRepository.findAllByShop_Id(id,pageable).map(product -> {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            BeanUtils.copyProperties(product, productResponseDTO);
            return productResponseDTO;
        });
}
}
