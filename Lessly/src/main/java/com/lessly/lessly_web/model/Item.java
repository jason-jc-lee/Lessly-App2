package com.lessly.lessly_web.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
/**
 * Item is the abstract base class for all inventory items.
 * It uses inheritance so each subclass gets its own database table
 * This means it contains its unique fields, and shared fields live here on this items table.
 * This avoids data duplication while keeping the OOP hierarchy able to be extended.
 */
@Entity
@Table(name = "items")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //this manually set bool when set to true, will not override the item's status as the user has did this
    //the app will respect this
    private boolean manuallySet = false;
    private String name;
    private String brand;
    private double price;
    private LocalDate purchaseDate;
    private LocalDate lastUsedDate;

    // stored as a string in the database so it remains readable
    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    private String category;
    //reconstruct objects from the database since the database layer needs to create a blank object first before filling the fields using setters
    public Item() {}

    public Item(String name, String brand, double price, LocalDate purchaseDate, String category) {
        // validate all fields here so invalid items are never sent to the database
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be empty.");
        if (brand == null || brand.isBlank()) throw new IllegalArgumentException("Brand cannot be empty.");
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative.");
        if (purchaseDate == null || purchaseDate.isAfter(LocalDate.now())) throw new IllegalArgumentException("Purchase date cannot be in the future.");

        this.name = name;
        this.brand = brand;
        this.price = price;
        this.purchaseDate = purchaseDate;
        // lastUsedDate starts as purchaseDate
        this.lastUsedDate = purchaseDate;
        //all items start as undecided until calculations are done
        this.status = ItemStatus.UNDECIDED;
        this.category = category;
    }
// updates last used date to today, which is called when user presses "Mark as just used"
    public void markUsed() {
        this.lastUsedDate = LocalDate.now();
    }

    // calculates how long ago the item was last used
    //used to determine inactive items
    public long daysSinceLastUsed() {
        return ChronoUnit.DAYS.between(lastUsedDate, LocalDate.now());
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public LocalDate getLastUsedDate() { return lastUsedDate; }
    public void setLastUsedDate(LocalDate lastUsedDate) { this.lastUsedDate = lastUsedDate; }
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isManuallySet() { return manuallySet; }
    public void setManuallySet(boolean manuallySet) { this.manuallySet = manuallySet; }
}