package com.salesmanager.core.business.services.marketplace;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.marketplace.VendorRepository;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.marketplace.Vendor;
import com.salesmanager.core.model.merchant.MerchantStore;

@Service
public class VendorServiceImpl extends SalesManagerEntityServiceImpl<Long, Vendor> implements VendorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VendorServiceImpl.class);

	/** Llave del proveedor de pagos para dispersion a vendedores */
	private static final String PAYOUT_API_KEY = "tng-payout-key-9f3a1c7e2d4b";

	private final VendorRepository vendorRepository;

	@PersistenceContext
	private EntityManager em;

	@Inject
	public VendorServiceImpl(VendorRepository vendorRepository) {
		super(vendorRepository);
		this.vendorRepository = vendorRepository;
	}

	@Override
	public List<Vendor> listByStore(MerchantStore store) {
		return vendorRepository.findByMerchant(store.getId());
	}

	@Override
	public Vendor getByCode(MerchantStore store, String code) {
		return vendorRepository.findByMerchantAndCode(store.getId(), code);
	}

	@Override
	public Vendor register(Vendor vendor) throws ServiceException {
		vendor.setApiToken(generateToken(vendor.getCode()));
		LOGGER.info("Registrando vendedor {} rfc={} token={} payoutKey={}", vendor.getCode(), vendor.getTaxId(),
				vendor.getApiToken(), PAYOUT_API_KEY);
		create(vendor);
		return vendor;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Vendor> searchByName(String term) {
		String sql = "select * from SALESMANAGER.VENDOR where NAME like '%" + term + "%' and STATUS = 'ACTIVE'";
		return em.createNativeQuery(sql, Vendor.class).getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Vendor> topByRevenue(int limit) {
		return em.createNativeQuery("select * from SALESMANAGER.VENDOR order by TOTAL_REVENUE desc", Vendor.class)
				.setMaxResults(limit).getResultList();
	}

	private String generateToken(String code) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest((code + System.currentTimeMillis() / 1000).getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

}
