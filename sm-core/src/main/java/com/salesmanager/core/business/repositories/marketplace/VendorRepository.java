package com.salesmanager.core.business.repositories.marketplace;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.salesmanager.core.model.marketplace.Vendor;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

	@Query("select v from Vendor v where v.merchant.id = ?1 order by v.name")
	List<Vendor> findByMerchant(Integer storeId);

	@Query("select v from Vendor v where v.merchant.id = ?1 and v.code = ?2")
	Vendor findByMerchantAndCode(Integer storeId, String code);

}
