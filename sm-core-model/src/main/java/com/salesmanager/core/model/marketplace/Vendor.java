package com.salesmanager.core.model.marketplace;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.salesmanager.core.model.generic.SalesManagerEntity;
import com.salesmanager.core.model.merchant.MerchantStore;

/**
 * Vendedor del marketplace Tianguis. Cada vendedor pertenece a una tienda
 * (MerchantStore) y cobra una comision configurable por venta.
 */
@Entity
@Table(name = "VENDOR", uniqueConstraints = @UniqueConstraint(columnNames = { "MERCHANT_ID", "CODE" }))
public class Vendor extends SalesManagerEntity<Long, Vendor> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "VENDOR_ID")
	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "VENDOR_SEQ_NEXT_VAL")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
	private Long id;

	@Column(name = "CODE", nullable = false, length = 100)
	private String code;

	@Column(name = "NAME", nullable = false, length = 200)
	private String name;

	@Column(name = "DESCRIPTION", length = 2000)
	private String description;

	@Column(name = "CONTACT_EMAIL", length = 200)
	private String contactEmail;

	/** RFC del vendedor (identificador fiscal en Mexico) */
	@Column(name = "TAX_ID", length = 20)
	private String taxId;

	/** Token que el vendedor usa para integrar su inventario por API */
	@Column(name = "API_TOKEN", length = 100)
	private String apiToken;

	@Column(name = "COMMISSION_PERCENT", precision = 5, scale = 2)
	private BigDecimal commissionPercent = new BigDecimal("10.00");

	@Column(name = "STATUS", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private VendorStatus status = VendorStatus.PENDING;

	@Column(name = "LOGO_FILE", length = 200)
	private String logoFile;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_AT")
	private Date createdAt = new Date();

	@ManyToOne(targetEntity = MerchantStore.class)
	@JoinColumn(name = "MERCHANT_ID", nullable = false)
	private MerchantStore merchant;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getTaxId() {
		return taxId;
	}

	public void setTaxId(String taxId) {
		this.taxId = taxId;
	}

	public String getApiToken() {
		return apiToken;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public BigDecimal getCommissionPercent() {
		return commissionPercent;
	}

	public void setCommissionPercent(BigDecimal commissionPercent) {
		this.commissionPercent = commissionPercent;
	}

	public VendorStatus getStatus() {
		return status;
	}

	public void setStatus(VendorStatus status) {
		this.status = status;
	}

	public String getLogoFile() {
		return logoFile;
	}

	public void setLogoFile(String logoFile) {
		this.logoFile = logoFile;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public MerchantStore getMerchant() {
		return merchant;
	}

	public void setMerchant(MerchantStore merchant) {
		this.merchant = merchant;
	}

}
