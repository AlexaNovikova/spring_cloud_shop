package ru.otus.inventory.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.otus.inventory.service.model.entity.ReservedProduct;
import ru.otus.inventory.service.model.entity.ReservedProductId;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservedProductRepository extends JpaRepository<ReservedProduct, ReservedProductId> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ReservedProduct rp WHERE rp.id.orderId = :orderId")
    void deleteAllByOrderId(Integer orderId);

    @Query("SELECT rp FROM ReservedProduct rp WHERE rp.id.orderId = :orderId")
    List<ReservedProduct> findAllByOrderId(Integer orderId);

    @Query("SELECT rp FROM ReservedProduct rp WHERE rp.id.productId in :productIds")
    List<ReservedProduct> findAllByProductIds(Collection<Integer> productIds);

}
