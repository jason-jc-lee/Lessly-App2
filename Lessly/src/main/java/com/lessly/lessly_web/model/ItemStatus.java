package com.lessly.lessly_web.model;
/**
 * Represents the recommended action for an item in the user's inventory.
 * Statuses are assigned by DeclutterService.java based on usage and price.
 * Can be set manually by the user through the UI. Enums prevents invalid statuses from being assigned.
 */
public enum ItemStatus {
    KEEP, //means item is actively used, no action needed
    DONATE, //means the item is unused for a while and costs under $50
    SELL, //unused for a while and costs $50+
    DISCARD, //expired or too old to use, for perishables
    UNDECIDED //default state, when item is added
}