package com.chimera.weapp.service;

import com.chimera.weapp.entity.Coupon;
import com.chimera.weapp.entity.PointsProduct;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.CouponRepository;
import com.chimera.weapp.repository.PointsProductRepository;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.vo.CouponIns;
import com.chimera.weapp.vo.PointsProductIns;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BenefitService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointsProductRepository pointsProductRepository;


    public void addCouponToUser(String userId, String couponId) throws Exception {
        // Convert userId and couponId to ObjectId
        ObjectId userObjectId;
        ObjectId couponObjectId;
        try {
            userObjectId = new ObjectId(userId);
            couponObjectId = new ObjectId(couponId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid userId or couponId");
        }

        // Find the User
        Optional<User> userOptional = userRepository.findById(userObjectId);
        if (!userOptional.isPresent()) {
            throw new Exception("User not found");
        }
        User user = userOptional.get();

        // Find the Coupon
        Optional<Coupon> couponOptional = couponRepository.findById(couponObjectId);
        if (!couponOptional.isPresent()) {
            throw new Exception("Coupon not found");
        }
        Coupon coupon = couponOptional.get();

        // Check if the coupon is valid (status=1)
        if (coupon.getStatus() != 1) {
            throw new Exception("Coupon is not active");
        }

        // Check if the coupon is still valid (not expired)
        if (coupon.getValidity() != null && coupon.getValidity().before(new Date())) {
            throw new Exception("Coupon is expired");
        }

        // Create a new CouponIns
        CouponIns couponIns = CouponIns.builder()
                .uuid(UUID.randomUUID().toString())
                .couponId(coupon.getId().toHexString())
                .name(coupon.getName())
                .status(0) // 0=未使用
                .cateId(coupon.getCateId())
                .dePrice(coupon.getDePrice())
                .validity(coupon.getValidity())
                .build();

        // Add the CouponIns to the User's coupons list
        if (user.getCoupons() == null) {
            user.setCoupons(new ArrayList<>());
        }
        user.getCoupons().add(couponIns);

        // Save the User
        userRepository.save(user);

        // Update the issueNum of the coupon
        coupon.setIssueNum(coupon.getIssueNum() + 1);
        couponRepository.save(coupon);
    }

    public User issueNewCustomerCouponsToUser(ObjectId userObjectId) throws Exception {

        // Find the User
        Optional<User> userOptional = userRepository.findById(userObjectId);
        if (!userOptional.isPresent()) {
            throw new Exception("User not found");
        }
        User user = userOptional.get();

        // Find all Coupons with type="新客" and status=1
        List<Coupon> coupons = couponRepository.findByTypeAndStatus("新客", 1);

        if (coupons == null || coupons.isEmpty()) {
            throw new Exception("No valid '新客' coupons found");
        }

        for (Coupon coupon : coupons) {
            // Check if the coupon is still valid (not expired)
            if (coupon.getValidity() != null && coupon.getValidity().before(new Date())) {
                continue; // Skip expired coupons
            }

            // Check if the user already has this coupon
            boolean alreadyHasCoupon = false;
            if (user.getCoupons() != null) {
                for (CouponIns couponIns : user.getCoupons()) {
                    if (couponIns.getCouponId().equals(coupon.getId().toHexString())) {
                        alreadyHasCoupon = true;
                        break;
                    }
                }
            }

            if (alreadyHasCoupon) {
                continue; // Skip if user already has the coupon
            }

            // Create a new CouponIns
            CouponIns couponIns = CouponIns.builder()
                    .uuid(UUID.randomUUID().toString())
                    .couponId(coupon.getId().toHexString())
                    .name(coupon.getName())
                    .status(0) // 0=未使用
                    .cateId(coupon.getCateId())
                    .dePrice(coupon.getDePrice())
                    .validity(coupon.getValidity())
                    .build();

            // Add the CouponIns to the User's coupons list
            if (user.getCoupons() == null) {
                user.setCoupons(new ArrayList<>());
            }
            user.getCoupons().add(couponIns);

            // Update the issueNum of the coupon
            coupon.setIssueNum(coupon.getIssueNum() + 1);
            couponRepository.save(coupon);
        }

        // Save the User
        return userRepository.save(user);
    }

    public User issueActivityCouponsToUser(ObjectId userObjectId) throws Exception {

        // Retrieve the User
        Optional<User> userOptional = userRepository.findById(userObjectId);
        if (!userOptional.isPresent()) {
            throw new Exception("User not found");
        }
        User user = userOptional.get();

        // Initialize user's coupon list if null
        if (user.getCoupons() == null) {
            user.setCoupons(new ArrayList<>());
        }

        // Remove expired coupons from user's coupon list
        Date now = new Date();
        List<CouponIns> validCoupons = new ArrayList<>();
        for (CouponIns couponIns : user.getCoupons()) {
            if (couponIns.getValidity() == null || !couponIns.getValidity().before(now)) {
                validCoupons.add(couponIns);
            }
        }
        user.setCoupons(validCoupons);

        // Fetch coupons with type="活动" and status=1
        List<Coupon> coupons = couponRepository.findByTypeAndStatus("活动", 1);

        if (coupons == null || coupons.isEmpty()) {
//            throw new Exception("No valid '活动' coupons found");
            return userRepository.save(user);
        }

        // Collect coupon IDs the user already has
        Set<String> userCouponIds = user.getCoupons().stream()
                .map(CouponIns::getCouponId)
                .collect(Collectors.toSet());

        for (Coupon coupon : coupons) {
            // Skip expired coupons
            if (coupon.getValidity() != null && coupon.getValidity().before(now)) {
                continue;
            }

            // Skip if user already has the coupon
            if (userCouponIds.contains(coupon.getId().toHexString())) {
                continue;
            }

            // Create a new CouponIns
            CouponIns couponIns = CouponIns.builder()
                    .uuid(UUID.randomUUID().toString())
                    .couponId(coupon.getId().toHexString())
                    .name(coupon.getName())
                    .status(0) // 0=未使用
                    .cateId(coupon.getCateId())
                    .dePrice(coupon.getDePrice())
                    .validity(coupon.getValidity())
                    .build();

            // Add the CouponIns to the User's coupons list
            user.getCoupons().add(couponIns);

            // Update the issueNum of the coupon
            coupon.setIssueNum(coupon.getIssueNum() + 1);
            couponRepository.save(coupon);
        }

        // Save the updated User
        return userRepository.save(user);
    }

    public void redeemUserCoupon(ObjectId userId, String couponUuid) throws Exception {
        // Retrieve the User
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new Exception("User not found");
        }
        User user = userOptional.get();

        // Check if the user has any coupons
        if (user.getCoupons() == null || user.getCoupons().isEmpty()) {
            throw new Exception("User has no coupons");
        }

        // Find the coupon in the user's coupons list by uuid
        CouponIns targetCouponIns = null;
        for (CouponIns couponIns : user.getCoupons()) {
            if (couponIns.getUuid().equals(couponUuid)) {
                targetCouponIns = couponIns;
                break;
            }
        }

        if (targetCouponIns == null) {
            throw new Exception("Coupon not found in user's coupons");
        }

        // Validate the coupon
        if (targetCouponIns.getStatus() != 0) {
            throw new Exception("Coupon has already been used");
        }

        Date now = new Date();
        if (targetCouponIns.getValidity() != null && targetCouponIns.getValidity().before(now)) {
            throw new Exception("Coupon has expired");
        }

        // Update the coupon's status to 1 (used)
        targetCouponIns.setStatus(1);

        // Save the updated user
        userRepository.save(user);

        // Optionally, update the useNum of the corresponding Coupon
        ObjectId couponObjectId = new ObjectId(targetCouponIns.getCouponId());
        Optional<Coupon> couponOptional = couponRepository.findById(couponObjectId);
        if (couponOptional.isPresent()) {
            Coupon coupon = couponOptional.get();
            coupon.setUseNum(coupon.getUseNum() + 1);
            couponRepository.save(coupon);
        }
    }

    public void exchangePointsForCoupon(ObjectId userId, ObjectId couponId) throws Exception {
        // Retrieve the User
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new Exception("User not found");
        }
        User user = userOptional.get();

        // Retrieve the Coupon
        Optional<Coupon> couponOptional = couponRepository.findById(couponId);
        if (!couponOptional.isPresent()) {
            throw new Exception("Coupon not found");
        }
        Coupon coupon = couponOptional.get();

        // Check if the coupon is convertible
        if (!coupon.isConvertible()) {
            throw new Exception("Coupon cannot be exchanged for points");
        }

        // Check the coupon's status
        if (coupon.getStatus() != 1) {
            throw new Exception("Coupon is not active");
        }

        // Check if the coupon has expired
        Date now = new Date();
        if (coupon.getValidity() != null && coupon.getValidity().before(now)) {
            throw new Exception("Coupon has expired");
        }

        // Check if user has enough points
        if (user.getPoints() < coupon.getCostPoints()) {
            throw new Exception("User does not have enough points");
        }

        // Deduct points from the user
        user.setPoints(user.getPoints() - coupon.getCostPoints());

        // Update issueNum of the coupon
        coupon.setIssueNum(coupon.getIssueNum() + 1);

        // Create a new CouponIns
        CouponIns couponIns = CouponIns.builder()
                .uuid(UUID.randomUUID().toString())
                .couponId(coupon.getId().toHexString())
                .name(coupon.getName())
                .status(0) // 0=未使用
                .cateId(coupon.getCateId())
                .dePrice(coupon.getDePrice())
                .validity(coupon.getValidity())
                .build();

        // Add the CouponIns to the User's coupons list
        if (user.getCoupons() == null) {
            user.setCoupons(new ArrayList<>());
        }
        user.getCoupons().add(couponIns);

        // Save the updated User and Coupon
        userRepository.save(user);
        couponRepository.save(coupon);
    }

    public void exchangePointsForPointsProduct(ObjectId userId, ObjectId pointsProductId, int sendType,
                                         String sendName, String sendAddr, String sendNum) throws Exception {
        // 检索用户
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new Exception("User not found");
        }
        User user = userOptional.get();

        // 检索积分商品
        Optional<PointsProduct> pointsProductOptional = pointsProductRepository.findById(pointsProductId);
        if (!pointsProductOptional.isPresent()) {
            throw new Exception("Points pointsProduct not found");
        }
        PointsProduct pointsProduct = pointsProductOptional.get();

        // 检查商品是否可兑换
        if (pointsProduct.getStatus() != 1) {
            throw new Exception("Product is not available for redemption");
        }

        // 检查用户积分是否足够
        if (user.getPoints() < pointsProduct.getCostPoints()) {
            throw new Exception("User does not have enough points to redeem this pointsProduct");
        }

        // 处理领取方式和邮递信息
        if (sendType == 1) { // 邮递
            if (sendName == null || sendName.isEmpty() ||
                    sendAddr == null || sendAddr.isEmpty() ||
                    sendNum == null || sendNum.isEmpty()) {
                throw new Exception("Shipping information is required for delivery");
            }
        } else if (sendType != 0) { // 非法的领取方式
            throw new Exception("Invalid sendType. Must be 0 (self-pickup) or 1 (delivery)");
        }

        // 扣除用户积分
        user.setPoints(user.getPoints() - pointsProduct.getCostPoints());

        // 增加商品的已兑换数量
        pointsProduct.setRedeemedNum(pointsProduct.getRedeemedNum() + 1);

        // 创建新的 PointsProductIns 实例
        PointsProductIns pointsProductIns = PointsProductIns.builder()
                .uuid(UUID.randomUUID().toString())
                .pointsProductId(pointsProduct.getId().toHexString())
                .name(pointsProduct.getName())
                .sendType(sendType)
                .sendName(sendName)
                .sendAddr(sendAddr)
                .sendNum(sendNum)
                .userId(userId.toHexString())
                .build();

        // 将 PointsProductIns 添加到用户的 pointsProducts 列表
        if (user.getPointsProducts() == null) {
            user.setPointsProducts(new ArrayList<>());
        }
        user.getPointsProducts().add(pointsProductIns);

        // 保存更新后的用户和商品信息
        userRepository.save(user);
        pointsProductRepository.save(pointsProduct);
    }

    public void setPointsProductAsReceived(ObjectId userId, String uuid) throws Exception {
        // 检索用户
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new Exception("User not found");
        }
        User user = userOptional.get();

        // 检查用户是否有 pointsProducts 列表
        if (user.getPointsProducts() == null || user.getPointsProducts().isEmpty()) {
            throw new Exception("User has no redeemed points products");
        }

        // 查找指定的 PointsProductIns
        PointsProductIns targetProductIns = null;
        for (PointsProductIns productIns : user.getPointsProducts()) {
            if (productIns.getUuid().equals(uuid)) {
                targetProductIns = productIns;
                break;
            }
        }

        if (targetProductIns == null) {
            throw new Exception("Points product not found for this user");
        }

        // 检查是否已经领取
        if (targetProductIns.getReceived() == 1) {
            throw new Exception("Points product has already been marked as received");
        }

        // 更新状态为已领取
        targetProductIns.setReceived(1);

        // 保存用户信息
        userRepository.save(user);
    }
}
