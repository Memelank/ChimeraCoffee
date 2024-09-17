package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.Product;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Value("${file.upload-dir}")
    private String uploadDirectory;

    @Value("${app.url}")
    private String url;

    @Autowired
    private ProductRepository repository;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping("/uploadImage")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = imageFile.getOriginalFilename();
            File destinationFile = new File(uploadDirectory + "product/" + filename);
            imageFile.transferTo(destinationFile);
            return ResponseEntity.ok(filename); // Return only filename
        }
        return ResponseEntity.badRequest().body("No file uploaded or file is empty.");
    }


    @PutMapping(consumes = {"multipart/form-data"})
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Product> updateProduct(@RequestBody Product entity) {
        // Save the updated product information
        entity.setImgURL(url + "product/" + entity.getImgURL());
        return ResponseEntity.ok(repository.save(entity));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Product> createProduct(@RequestBody Product entity) {
        // Save the product information to the database
        entity.setImgURL(url + "product/" + entity.getImgURL());
        return ResponseEntity.ok(repository.save(entity));
    }

}
