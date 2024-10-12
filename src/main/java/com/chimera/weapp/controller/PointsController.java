package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.PointsProduct;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.CouponRepository;
import com.chimera.weapp.repository.PointsProductRepository;
import com.chimera.weapp.service.BenefitService;
import com.chimera.weapp.vo.PointsProductIns;
import io.swagger.v3.oas.annotations.Operation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/points")
public class PointsController {

    @Value("${file.upload-dir}")
    private String uploadDirectory;

    @Value("${app.url}")
    private String url;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private BenefitService benefitService;

    @Autowired
    private PointsProductRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping
    @LoginRequired
    @Operation(summary = "获取积分兑换商品")
    public List<PointsProduct> getAllPointsProducts() {
        return repository.findAllByDeleteIsAndStatusIs(0, 1);
    }

    @GetMapping(("/shop"))
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<List<PointsProduct>> getAllPointsProductsShop() {
        return ResponseEntity.ok(repository.findAllByDeleteIs(0));
    }

    @PostMapping("/exchangeCoupon")
    @LoginRequired
    @Operation(summary = "积分兑换优惠券接口")
    public ResponseEntity<String> exchangeCoupon(
            @RequestParam String userId,
            @RequestParam String couponId) {
        try {
            ObjectId userObjectId = new ObjectId(userId);
            ObjectId couponObjectId = new ObjectId(couponId);
            benefitService.exchangePointsForCoupon(userObjectId, couponObjectId);
            return ResponseEntity.ok("Coupon exchanged successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/redeemProduct")
    @LoginRequired
    @Operation(summary = "使用积分兑换积分商品")
    public ResponseEntity<String> redeemPointsProduct(
            @RequestParam String userId,
            @RequestParam String productId,
            @RequestParam int sendType,
            @RequestParam(required = false) String sendName,
            @RequestParam(required = false) String sendAddr,
            @RequestParam(required = false) String sendNum) {
        try {
            ObjectId userObjectId = new ObjectId(userId);
            ObjectId productObjectId = new ObjectId(productId);
            benefitService.exchangePointsForPointsProduct(userObjectId, productObjectId, sendType, sendName, sendAddr, sendNum);
            return ResponseEntity.ok("Product redeemed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping(("/pointsProductInsList"))
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public List<PointsProductIns> getAllPointsProductIns() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("pointsProducts").ne(null)),
                Aggregation.unwind("pointsProducts"),
                Aggregation.replaceRoot("pointsProducts")
        );

        AggregationResults<PointsProductIns> results = mongoTemplate.aggregate(aggregation, "user", PointsProductIns.class);
        return results.getMappedResults();
    }

    @PostMapping("/receiveProduct")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<String> setPointsProductAsReceived(
            @RequestParam String userId,
            @RequestParam String uuid) {
        try {
            ObjectId userObjectId = new ObjectId(userId);
            benefitService.setPointsProductAsReceived(userObjectId, uuid);
            return ResponseEntity.ok("Points product marked as received successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping(value = "/uploadImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<String> uploadImagePointsProduct(@RequestParam("image") MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = imageFile.getOriginalFilename();
                File destinationDir = new File(uploadDirectory + "points_product/");
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs(); // 创建目录
                }
                File destinationFile = new File(destinationDir, filename);
                imageFile.transferTo(destinationFile);
                return ResponseEntity.ok(filename); // Return only filename
            }
            return ResponseEntity.badRequest().body("No file uploaded or file is empty.");
        } catch (MultipartException e) {
            // 打印详细错误信息
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to parse multipart request");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save file");
        }
    }

    @PutMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<PointsProduct> updatePointsProduct(@RequestBody PointsProduct entity) {
        entity.setImgURL(url + "points_product/" + entity.getImgURL());
        return ResponseEntity.ok(repository.save(entity));
    }

    @PostMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<PointsProduct> createPointsProduct(@RequestBody PointsProduct entity) {
        entity.setImgURL(url + "points_product/" + entity.getImgURL());
        return ResponseEntity.ok(repository.save(entity));
    }

}
