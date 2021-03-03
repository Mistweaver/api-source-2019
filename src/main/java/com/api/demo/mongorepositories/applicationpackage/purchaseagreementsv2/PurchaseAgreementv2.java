package com.api.demo.mongorepositories.applicationpackage.purchaseagreementsv2;

import com.api.demo.mongorepositories.DocumentStates;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;

@Getter
@Setter
public class PurchaseAgreementv2 {
	@Indexed
	private String leadId;
	@Indexed
	private String crmLeadId;
	@Indexed
	private String locationId;
	@Indexed
	private String customerCode;
	@Indexed
	private String documentCode;
	@Indexed
	private String salesPersonId;

	@Indexed
	private String status;

	// key dates
	private String modelSelectionDate;
	private String promotionSelectionHalf;
	private String submissionDate;
	private String executionDate;
	private String closedDate;



	// buyer details
	@Indexed
	private String buyer1;
	private String buyer2;
	private String date;
	@Indexed
	private String contractRevisedFrom;
	private String contractRevisedFromDate;
	private String phone;
	private String cell;
	@Indexed
	private String emailAddress;
	private String emailAddress2;
	private String mailingStreet;
	private String mailingCity;
	private String mailingState;
	private String mailingZip;
	private String mailingCountry;
	private String deliveryStreet;
	private String deliveryCity;
	@Indexed
	private String deliveryState;
	private String deliveryZip;
	private String deliveryCountry;

	// Price Data details
	// This information is set when a customer selects a model

	private String serialNumber;
	private boolean newModel;

	private int numberOfUnits;


	public PurchaseAgreementv2() {
		this.status = DocumentStates.IN_PROGRESS.name();
	}




	/*public void selectModel(PriceDataDTO _priceDataDTO) {
		this.priceDataDTO = _priceDataDTO;
		// this.modelSelectionDate = new Calendar().getTime().;
		// set hitch size here
	}*/





	public boolean submitAgreement() {
		if(this.status.equals(DocumentStates.IN_PROGRESS.name())) {
			this.status = DocumentStates.SUBMITTED.name();
			return true;
		} else {
			return false;
		}
	}

	public boolean unSubmitAgreement() {
		if(this.status.equals(DocumentStates.SUBMITTED.name())) {
			this.status = DocumentStates.IN_PROGRESS.name();
			return true;
		} else {
			return false;
		}
	}

	public boolean executeAgreement() {
		if(this.status.equals(DocumentStates.SUBMITTED.name())) {
			this.status = DocumentStates.EXECUTED.name();
			return true;
		} else {
			return false;
		}
	}

	public boolean closeAgreement() {
		if(this.status.equals(DocumentStates.EXECUTED.name())) {
			this.status = DocumentStates.CLOSED.name();
			return true;
		} else {
			return false;
		}
	}
}
