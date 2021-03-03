package com.api.demo.mongorepositories.applicationpackage.changeorders;

import com.api.demo.mongorepositories.BasicEntity;
import com.api.demo.mongorepositories.DocumentStates;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "changeorders")
public class ChangeOrder extends BasicEntity {
    @Indexed()
    private String purchaseAgreementId;
    @Indexed
    private String leadId;
    @Indexed()
    private String customerCode;
    @Indexed()
    private String documentCode;

    private int changeOrderNumber;
    private String date;
    private float subTotal;
    private float total;
    private JSONObject taxBreakdown;
    private float tax;
    private String taxType; // CUSTOM, EXEMPT, AVALARA
    private List<JSONObject> items;
    // @Indexed
    // private boolean locked;
    // @Indexed
    // private boolean submitted;
    @Indexed
    private int monthFinalized;
    @Indexed
    private int yearFinalized;

    @Indexed
    private String status;

    // escrow doc variables
    private String escrowNumber;
    private float previousContractPrice;
    private float revisedContractPrice;
    private String paymentMethod;

    public ChangeOrder() {
        this.purchaseAgreementId = "";
        this.customerCode = "";
        this.documentCode = "";

        this.changeOrderNumber = 0;
        this.date = "";
        this.subTotal = 0;
        this.tax = 0;
        this.total = 0;
        this.taxBreakdown = new JSONObject();
        this.taxType = "AVALARA";
        this.items = new ArrayList<>();
        // this.locked = false;
        // this.submitted = false;
        this.monthFinalized = 0;
        this.yearFinalized = 0;
        // this.status = DocumentStates.IN_PROGRESS.name();
        this.status = DocumentStates.IN_PROGRESS.name();

        this.escrowNumber = "";
        this.previousContractPrice = 0;
        this.revisedContractPrice = 0;
        this.paymentMethod = "";
    }

    public static String typeString() {
        return "changeOrder";
    }

    public boolean isChangeOrderInProgress() {
        return this.status.equals(DocumentStates.IN_PROGRESS.name());
    }

    public boolean submitChangeOrder() {
        if(this.status.equals(DocumentStates.IN_PROGRESS.name())) {
            this.status = DocumentStates.SUBMITTED.name();
            return true;
        } else {
            return false;
        }
    }

    public boolean unSubmitChangeOrder() {
        if(this.status.equals(DocumentStates.SUBMITTED.name())) {
            this.status = DocumentStates.IN_PROGRESS.name();
            return true;
        } else {
            return false;
        }
    }

    public boolean isChangeOrderSubmitted() {
        return this.status.equals(DocumentStates.SUBMITTED.name());
    }

    public boolean executeChangeOrder() {
        if(this.status.equals(DocumentStates.SUBMITTED.name())) {
            this.status = DocumentStates.EXECUTED.name();
            return true;
        } else {
            return false;
        }
    }

    public boolean isChangeOrderExecuted() {
        return this.status.equals(DocumentStates.EXECUTED.name());
    }

    public boolean closeChangeOrder() {
        if(this.status.equals(DocumentStates.EXECUTED.name())) {
            this.status = DocumentStates.CLOSED.name();
            return true;
        } else {
            return false;
        }
    }

    public boolean isChangeOrderClosed() {
        return this.status.equals(DocumentStates.CLOSED.name());
    }
}
