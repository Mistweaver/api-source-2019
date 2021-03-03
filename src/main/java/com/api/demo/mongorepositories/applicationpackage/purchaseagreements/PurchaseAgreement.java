package com.api.demo.mongorepositories.applicationpackage.purchaseagreements;

import com.api.demo.mongorepositories.BasicEntity;
import com.api.demo.mongorepositories.DocumentStates;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@ToString
@Document(collection="purchaseagreement")
public class PurchaseAgreement extends BasicEntity {

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

    // private boolean submitted;
    // private boolean locked;
    @Indexed
    private String status;



    // private boolean lenderPaid;
    private boolean taxExempt;
    // private boolean outOfState;

    // agreement filing information
    @Indexed
    private int monthFinalized;
    @Indexed
    private int yearFinalized;

    @Indexed
    private String salesPersonId;

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

    private boolean addressValid;

    private String salesPerson;

    private String modelSelectionDate;
    private String promotionSelectionHalf;

    private String make;
    private String model;
    private String manufacturer;
    private String modelType;
    private String year;
    private String bedrooms;
    private String baths;
    private String dens;

    private String serialNumber;
    private boolean newModel;
    private String floorSize;
    private String hitchSize;
    private float approximateSquareFeet;

    // purchase agreement details
    private float retailPrice;
    private float factoryDirectDiscountAmount;
    private float factoryDirectPrice;
    private float factoryTotalCost;

    private int numberOfUnits;

    private float addendumAUpgrades;

    private String featuredHomePromo;
    private float featuredHomePromoAmount;

    private String managerOrClearanceDiscountSelection;
    private float managerOrClearanceAmount;

    private float preferredPaymentAmount;

    private float vipMultiUnitDiscountAmount;

    private float subTotal2;

    private float standardFreightChargeAmount;

    private float factoryTrimOutAmount;

    private float purchaseOfACAmount;

    private float setupChargesAmount;

    private float lotExpenseAmount;

    private String openField1;
    private float openField1Amount;

    private float extendedServiceContractAmount;

    private String documentOrHomePrepFee;
    private float documentOrHomePrepFeeAmount;

    private String titleFee;
    private float titleFeeAmount;

    private float subTotal3;

    private float factoryFreight;  // freight input by Accounting provided by factory invoice
    private float factoryInvoice;  // invoice amount input by Accounting from factory invoice

    private float taxesAmount;
    private boolean useCustomTaxableAmount;
    private float customTaxableAmount;

    private String disclaimer;

    private float total;
    private float downPayment;
    private float additionalPaymentAsAgreed;
    private float unpaidBalance;


    private String noticeOfConstructionAndFinalPayment;
    private String noticeOfConstructionAndFinalPaymentText;
    private String noticeOfCompletion;
    private String balancePaidInFullDate;

    private String notes;

    // addendum stuff
    private JSONObject addendumA;

    // color selections
    private JSONObject colorSelections;

    private JSONObject applianceSelections;

    // taxes
    private JSONObject taxBreakdown;

    // wind zone
    private int windZone;

    // Shipping Directions
    private String shippingContactName;
    private String shippingContactDayPhone;
    private String shippingContactEveningPhone;
    private String shippingContactMobilePhone;
    private String shippingDirections;
    private String shippingDirectionsMapFileUri;

    // escrow doc variables
    private String escrowDocDate;
    private String escrowNumber;
    private String closeOrTerminationDate;
    private float additionalDeposit1;
    private String dateOfAdditionalDeposit1;
    private float getAdditionalDeposit2;
    private String dateOfAdditionalDeposit2;
    private float getAdditionalDeposit13;
    private String dateOfAdditionalDeposit3;

    private String buyerElectionChoice;

    // wire transfer notice variables
    private String wireTransferDate;
    private float wireAmount;
    private String wireExpectedDate;
    private String acEscrow;
    private String specialNotes;
    private String notificationReceivedDate;
    private String naz;
    private String escrowCompany;
    private String escrowCompanyNotificationDate;







    public PurchaseAgreement() {

        this.leadId = "";
        this.crmLeadId = "";
        this.locationId = "";
        this.customerCode = "";
        this.documentCode = "";
        this.status = DocumentStates.IN_PROGRESS.name();
        // this.lenderPaid = false;
        this.taxExempt = false;
        // this.outOfState = false;
        this.monthFinalized = -1;
        this.yearFinalized =  -1;
        this.salesPersonId = "";

        this.buyer1 = "";
        this.buyer2 = "";
        this.date = "";
        this.contractRevisedFrom = "";
        this.contractRevisedFromDate = "";

        this.phone = "";
        this.cell = "";
        this.emailAddress = "";
        this.emailAddress2 = "";

        this.mailingStreet = "";
        this.mailingCity = "";
        this.mailingState = "";
        this.mailingZip = "";
        this.mailingCountry = "";

        this.deliveryStreet = "";
        this.deliveryCity = "";
        this.deliveryState = "";
        this.deliveryZip = "";
        this.deliveryCountry = "";
        this.addressValid = false;

        this.salesPerson = "";

        this.modelSelectionDate = "";
        this.promotionSelectionHalf = "";
        this.make = "";
        this.model = "";
        this.manufacturer = "";
        this.modelType = "";
        this.year = "";
        this.bedrooms = "";
        this.baths = "";
        this.dens = "";

        this.serialNumber = "";
        this.newModel = false;
        this.floorSize = "";
        this.hitchSize = "";
        this.approximateSquareFeet = 0;

        // purchase agreement details
        this.retailPrice = 0;
        this.factoryDirectDiscountAmount = 0;
        this.factoryDirectPrice = 0;
        this.factoryTotalCost = 0;

        this.numberOfUnits = 1;

        this.addendumAUpgrades = 0;

        this.featuredHomePromo = "";
        this.featuredHomePromoAmount = 0;

        this.managerOrClearanceDiscountSelection = "";
        this.managerOrClearanceAmount = 0;

        this.preferredPaymentAmount = 0;
        this.vipMultiUnitDiscountAmount = 0;
        this.subTotal2 = 0;
        this.standardFreightChargeAmount = 0;
        this.factoryTrimOutAmount = 0;
        this.purchaseOfACAmount = 0;
        this.setupChargesAmount = 0;
        this.lotExpenseAmount = 0;

        this.openField1 = "";
        this.openField1Amount = 0;

        this.extendedServiceContractAmount = 0;

        this.documentOrHomePrepFee = "";
        this.documentOrHomePrepFeeAmount = 0;

        this.titleFee = "";
        this.titleFeeAmount = 0;

        this.subTotal3 = 0;

        this.factoryFreight = 0;
        this.factoryInvoice = 0;

        this.taxesAmount = 0;
        this.useCustomTaxableAmount = false;
        this.customTaxableAmount = 0;

        this.disclaimer = "";

        this.total = 0;
        this.downPayment = 0;
        this.additionalPaymentAsAgreed = 0;
        this.unpaidBalance = 0;


        this.noticeOfConstructionAndFinalPayment = "";
        this.noticeOfConstructionAndFinalPaymentText = "";
        this.noticeOfCompletion = "";
        this.balancePaidInFullDate = "";

        this.notes = "";

        // addendum stuff
        this.addendumA = new JSONObject();
        this.addendumA.put("notes", "");
        this.addendumA.put("total", 0);
        JSONArray items = new JSONArray();
        this.addendumA.put("items", items);

        // color selections
        this.colorSelections = new JSONObject();

        this.applianceSelections = new JSONObject();
        // taxes
        this.taxBreakdown = new JSONObject();

        // wind zone
        this.windZone = 0;

        // Shipping Directions
        this.shippingContactName = "";
        this.shippingContactDayPhone = "";
        this.shippingContactEveningPhone = "";
        this.shippingContactMobilePhone = "";
        this.shippingDirections = "";
        this.shippingDirectionsMapFileUri = "";

        // escrow doc variables
        this.escrowDocDate = "";
        this.escrowNumber = "";
        this.closeOrTerminationDate = "";
        this.additionalDeposit1 = 0;
        this.dateOfAdditionalDeposit1 = "";
        this.getAdditionalDeposit2 = 0;
        this.dateOfAdditionalDeposit2 = "";
        this.getAdditionalDeposit13 = 0;
        this.dateOfAdditionalDeposit3 = "";

        this.buyerElectionChoice = "";

        // wire transfer notice variables
        this.wireTransferDate = "";
        this.wireAmount = 0;
        this.wireExpectedDate = "";
        this.acEscrow = "";
        this.specialNotes = "";
        this.notificationReceivedDate = "";
        this.naz = "";
        this.escrowCompany = "";
        this.escrowCompanyNotificationDate = "";

    }

    public static String typeString() {
        return "purchaseAgreement";
    }

    public boolean isAgreementInProgress() {
        return this.status.equals(DocumentStates.IN_PROGRESS.name());
    }

    public void initializeRevisedAgreementState() {
        this.status = DocumentStates.IN_PROGRESS.name();
    }

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

    public boolean isAgreementSubmitted() {
        return this.status.equals(DocumentStates.SUBMITTED.name());
    }

    public boolean executeAgreement() {
        if(this.status.equals(DocumentStates.SUBMITTED.name())) {
            this.status = DocumentStates.EXECUTED.name();
            return true;
        } else {
            return false;
        }
    }

    public boolean isAgreementExecuted() {
        return this.status.equals(DocumentStates.EXECUTED.name());
    }

    public boolean closeAgreement() {
        if(this.status.equals(DocumentStates.EXECUTED.name())) {
            this.status = DocumentStates.CLOSED.name();
            return true;
        } else {
            return false;
        }
    }

    public boolean canAgreementBeRevised() {
        if(this.status.equals(DocumentStates.CLOSED.name()) || this.status.equals(DocumentStates.EXECUTED.name())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAgreementClosed() {
        return this.status.equals(DocumentStates.CLOSED.name());
    }

}
