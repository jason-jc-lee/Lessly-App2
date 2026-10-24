package com.lessly.lessly_web.model;

import jakarta.persistence.*;
import java.time.LocalDate;
/**
 * Perishables represents the consumable item with a limited shelf life.
 * It extends Item and adds expiry tracking.
 * Expiry is only calculated once the item is opened and unopened items have no
 * expiry date but are still flagged if purchased over a year ago.
 */
@Entity
@Table(name = "perishables")
public class Perishables extends Item {
    // if its null, it means the item has never been opened
    private LocalDate openedDate;
    private int shelfLifeDays;//number of days the item remains usable after being opened

    public Perishables() {}

    public Perishables(String name, String brand, double price, LocalDate purchaseDate, LocalDate openedDate, int shelfLifeDays) {
        super(name, brand, price, purchaseDate, "Perishables");
        //shelf life must be at least 1 day
        if (shelfLifeDays <= 0) throw new IllegalArgumentException("Shelf life must be greater than zero.");
        this.openedDate = openedDate;
        this.shelfLifeDays = shelfLifeDays;
    }

    public LocalDate getOpenedDate() { return openedDate; }
    public void setOpenedDate(LocalDate openedDate) { this.openedDate = openedDate; }
    public int getShelfLifeDays() { return shelfLifeDays; }
    public void setShelfLifeDays(int shelfLifeDays) { this.shelfLifeDays = shelfLifeDays; }

    // null if the item has never been opened since expiry cannot be determined
    public LocalDate getExpiryDate() {
        if (openedDate == null) return null;
        return openedDate.plusDays(shelfLifeDays);
    }

    // an item is only expired if it has been opened and today is past the expiry date
    // unopened items are handled separately in DeclutterService via purchase date
    public boolean isExpired() {
        LocalDate expiry = getExpiryDate();
        return expiry != null && LocalDate.now().isAfter(expiry);
    }
}