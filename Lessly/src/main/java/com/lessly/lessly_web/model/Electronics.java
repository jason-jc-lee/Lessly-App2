package com.lessly.lessly_web.model;

import jakarta.persistence.*;
import java.time.LocalDate;
/**
 * Electronics represents an electronic item in the user's inventory.
 * It extends Item to inherit common fields like name, brand, price, and usage tracking.
 * Model and warranty are electronics-specific fields stored in a separate database table
 * linked to the main items table by id (JOINED inheritance strategy).
 */
@Entity
@Table(name = "electronics")
public class Electronics extends Item {

    private String model;
    private int warrantyMonths;
    //reconstruct objects from the database since the database layer needs to create a blank object first before filling the fields using setters
    public Electronics() {}

    public Electronics(String name, String brand, double price, LocalDate purchaseDate, String model, int warrantyMonths) {
        // model and warranty are validated here since they are specific to electronics
        super(name, brand, price, purchaseDate, "Electronics");
        if (model == null || model.isBlank()) throw new IllegalArgumentException("Model cannot be empty.");
        // warranty of 0 is allowed since some electronics have no warranty
        if (warrantyMonths < 0) throw new IllegalArgumentException("Warranty months cannot be negative.");
        this.model = model;
        this.warrantyMonths = warrantyMonths;
    }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }
}