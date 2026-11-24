package com.salesmanager.core.business.services.marketplace;

import java.util.List;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityService;
import com.salesmanager.core.model.marketplace.Vendor;
import com.salesmanager.core.model.merchant.MerchantStore;

/**
 * Gestion de vendedores del marketplace.
 */
public interface VendorService extends SalesManagerEntityService<Long, Vendor> {

	List<Vendor> listByStore(MerchantStore store);

	Vendor getByCode(MerchantStore store, String code);

	/** Registra un vendedor nuevo y le genera su token de integracion */
	Vendor register(Vendor vendor) throws ServiceException;

	/** Busqueda por nombre (para el buscador del panel) */
	List<Vendor> searchByName(String term);

	/** Ranking de vendedores por ingresos */
	List<Vendor> topByRevenue(int limit);

}
