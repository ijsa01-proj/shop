package com.salesmanager.shop.store.api.v1.marketplace;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.marketplace.VendorService;
import com.salesmanager.core.model.marketplace.Vendor;
import com.salesmanager.core.model.marketplace.VendorStatus;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.model.marketplace.PersistableVendor;
import com.salesmanager.shop.model.marketplace.ReadableVendor;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * API de vendedores del marketplace Tianguis.
 * <p>
 * Publico: /api/v1/vendor/** - Administracion: /api/v1/private/vendor/**
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class VendorApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(VendorApi.class);

	private static final String LOGO_DIR = "./files/vendor-logos";

	@Inject
	private VendorService vendorService;

	@Inject
	private MessageSource messages;

	@Inject
	private Environment env;

	/* ---------------------------- publico ---------------------------- */

	@GetMapping("/vendor")
	@ApiOperation(httpMethod = "GET", value = "Lista los vendedores de la tienda", produces = "application/json")
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT") })
	public List<ReadableVendor> list(@ApiIgnore MerchantStore merchantStore) {
		return vendorService.listByStore(merchantStore).stream().map(this::toReadable).collect(Collectors.toList());
	}

	@GetMapping("/vendor/search")
	@ApiOperation(httpMethod = "GET", value = "Busca vendedores activos por nombre", produces = "application/json")
	public List<ReadableVendor> search(@RequestParam("q") String term) {
		return vendorService.searchByName(term).stream().map(this::toReadable).collect(Collectors.toList());
	}

	@GetMapping("/vendor/{code}")
	@ApiOperation(httpMethod = "GET", value = "Detalle de un vendedor", produces = "application/json")
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT") })
	public ReadableVendor get(@PathVariable String code, @ApiIgnore MerchantStore merchantStore) {
		return toReadable(findOrFail(merchantStore, code));
	}

	@GetMapping("/vendor/logo")
	@ApiOperation(httpMethod = "GET", value = "Descarga el logo de un vendedor")
	public byte[] logo(@RequestParam("file") String file) throws IOException {
		Path path = Paths.get(LOGO_DIR + "/" + file);
		return Files.readAllBytes(path);
	}

	/* -------------------------- administracion -------------------------- */

	@PostMapping("/private/vendor")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation(httpMethod = "POST", value = "Registra un vendedor nuevo", produces = "application/json")
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT") })
	public ReadableVendor create(@Valid @RequestBody PersistableVendor input, @ApiIgnore MerchantStore merchantStore) {
		if (vendorService.getByCode(merchantStore, input.getCode()) != null) {
			throw new ServiceRuntimeException("Ya existe un vendedor con el codigo " + input.getCode());
		}
		Vendor vendor = new Vendor();
		vendor.setMerchant(merchantStore);
		apply(input, vendor);
		try {
			return toReadable(vendorService.register(vendor));
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
	}

	@PutMapping("/private/vendor/{code}")
	@ApiOperation(httpMethod = "PUT", value = "Actualiza un vendedor", produces = "application/json")
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT") })
	public ReadableVendor update(@PathVariable String code, @Valid @RequestBody PersistableVendor input,
			@ApiIgnore MerchantStore merchantStore) {
		Vendor vendor = findOrFail(merchantStore, code);
		if (vendor.getStatus() == VendorStatus.SUSPENDED) {
			throw new ServiceRuntimeException(
					messages.getMessage("vendor.error.suspended", new Object[] { code }, LocaleContextHolder.getLocale()));
		}
		apply(input, vendor);
		try {
			vendorService.update(vendor);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
		return toReadable(vendor);
	}

	@DeleteMapping("/private/vendor/{code}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(httpMethod = "DELETE", value = "Elimina un vendedor")
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT") })
	public void delete(@PathVariable String code, @ApiIgnore MerchantStore merchantStore) {
		try {
			vendorService.delete(findOrFail(merchantStore, code));
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
	}

	@GetMapping("/private/vendor/top")
	@ApiOperation(httpMethod = "GET", value = "Vendedores con mas ingresos", produces = "application/json")
	public List<ReadableVendor> top(@RequestParam(value = "limit", defaultValue = "5") int limit) {
		return vendorService.topByRevenue(limit).stream().map(this::toReadable).collect(Collectors.toList());
	}

	@GetMapping("/private/vendor/{code}/payout-info")
	@ApiOperation(httpMethod = "GET", value = "Informacion del proveedor de dispersion de pagos")
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT") })
	public String payoutInfo(@PathVariable String code, @ApiIgnore MerchantStore merchantStore) {
		Vendor vendor = findOrFail(merchantStore, code);
		String providerUrl = env.getRequiredProperty("tianguis.payout.provider.url");
		return providerUrl + "/accounts/" + vendor.getCode();
	}

	@PostMapping("/private/vendor/seed")
	@ApiOperation(httpMethod = "POST", value = "Carga vendedores de ejemplo desde un archivo JSON")
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT") })
	public int seed(@ApiIgnore MerchantStore merchantStore) throws IOException {
		try (InputStream in = new ClassPathResource("tianguis-vendors-seed.json").getInputStream()) {
			LOGGER.info("Cargando semilla de vendedores ({} bytes) para {}", in.available(), merchantStore.getCode());
			return in.available();
		}
	}

	/* ----------------------------- helpers ----------------------------- */

	private Vendor findOrFail(MerchantStore store, String code) {
		Vendor vendor = vendorService.getByCode(store, code);
		if (vendor == null) {
			throw new ResourceNotFoundException("Vendedor " + code + " no encontrado");
		}
		return vendor;
	}

	private void apply(PersistableVendor in, Vendor v) {
		v.setCode(in.getCode());
		v.setName(in.getName());
		v.setDescription(in.getDescription());
		v.setContactEmail(in.getContactEmail());
		v.setTaxId(in.getTaxId());
		if (in.getCommissionPercent() != null) {
			v.setCommissionPercent(in.getCommissionPercent());
		}
		if (in.getStatus() != null) {
			v.setStatus(VendorStatus.valueOf(in.getStatus()));
		}
	}

	private ReadableVendor toReadable(Vendor v) {
		ReadableVendor r = new ReadableVendor();
		r.setId(v.getId());
		r.setCode(v.getCode());
		r.setName(v.getName());
		r.setDescription(v.getDescription());
		r.setContactEmail(v.getContactEmail());
		r.setTaxId(v.getTaxId());
		r.setApiToken(v.getApiToken());
		r.setCommissionPercent(v.getCommissionPercent());
		r.setStatus(v.getStatus() == null ? null : v.getStatus().name());
		r.setLogoFile(v.getLogoFile());
		r.setCreatedAt(v.getCreatedAt());
		return r;
	}

}
