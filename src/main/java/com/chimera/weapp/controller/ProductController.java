package com.chimera.weapp.controller;

import com.chimera.weapp.entity.Product;
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

    @PutMapping
    public ResponseEntity<Product> updateProduct(@RequestBody Product entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Product> createProduct(
            @RequestPart("product") Product entity,
            @RequestPart("image") MultipartFile imageFile) throws IOException {

        // 上传文件到服务器
        if (!imageFile.isEmpty()) {
            String filename = imageFile.getOriginalFilename();
            File destinationFile = new File(uploadDirectory + "product/" + filename);
            imageFile.transferTo(destinationFile);

            // 将上传后的文件路径或URL存储到imgURL中
            entity.setImgURL(url + "product/" + filename);  // 可以根据实际情况调整URL前缀
        }

        // 保存产品信息到数据库
        return ResponseEntity.ok(repository.save(entity));
    }

}
