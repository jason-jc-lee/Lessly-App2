package com.lessly.lessly_web.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Clothing represents a clothing item in the user's inventory.
 * Extends item to inherit common fields like name, brand, price, and usage.
 * Size and color are specific fields stored in a separate table.
 * Linked to the main items table
 */

@Entity
@Table(name = "clothing")
public class Clothing extends Item {

    private String size;
    private String color;
//reconstruct objects from the database since the database layer needs to create a blank object first before filling the fields using setters

    public Clothing() {}

    public Clothing(String name, String brand, double price, LocalDate purchaseDate, String size, String color) {
        super(name, brand, price, purchaseDate, "Clothing");
        // Size and color are validated here rather than in Item
        //this is because they are specific to clothing and no where else
        if (size == null || size.isBlank()) throw new IllegalArgumentException("Size cannot be empty.");
        if (color == null || color.isBlank()) throw new IllegalArgumentException("Color cannot be empty.");
        this.size = size;
        this.color = color;
    }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}