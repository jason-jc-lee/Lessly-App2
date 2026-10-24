package com.lessly.lessly_web.service;
import java.time.LocalDate;
import com.lessly.lessly_web.model.*;
import com.lessly.lessly_web.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
/**
 * DeclutterService handles the core logic of the application.
 * it handles all decisions about item status, flagging, and prioritization.
 * However, it doesn't handle web requests or the structure of the database, that is for ItemController.java
 */
@Service
public class DeclutterService {
//items unused for longer than this threshold are considered inactive and flagged
    private static final int INACTIVE_DAYS_THRESHOLD = 30;

    // items above this price are suggested for selling rather than donating as they have enough resale value
    private static final double HIGH_VALUE_THRESHOLD = 50.0;

    @Autowired
    private ItemRepository itemRepository;

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public List<Item> getItemsByCategory(String category) {
        return itemRepository.findByCategory(category);
    }
//returns all items that needs the user's attention. flagging rules depend on item type and status
    public List<Item> getFlaggedItems() {
        List<Item> flagged = new ArrayList<>();
        for (Item item : itemRepository.findAll()) {
            // manually locked items, only flag if status is not KEEP
            //if user already made a decision, respect the keep status, but otherwise flag the items that is non-keep status
            if (item.isManuallySet()) {
                if (item.getStatus() != ItemStatus.KEEP) {
                    flagged.add(item);
                }
                continue;
            }
            if (item instanceof Perishables p) {
                // expired perishables always flagged
                if (p.isExpired()) {
                    flagged.add(item);
                    continue;
                }
                // unopened but purchased over a year ago, same rule as suggestStatus
                //purchase date is used instead of last used date
                //this is because just using the item shouldn't clear the flag if the item was never opened
                if (p.getOpenedDate() == null) {
                    long daysSincePurchase = java.time.temporal.ChronoUnit.DAYS.between(
                            item.getPurchaseDate(), java.time.LocalDate.now()
                    );
                    if (daysSincePurchase > 365) {
                        flagged.add(item);
                        continue;
                    }
                }
            }
            //this is to flag any item that hasn't been used within the threshold
            if (item.daysSinceLastUsed() >= INACTIVE_DAYS_THRESHOLD) {
                flagged.add(item);
            }
        }
        return flagged;
    }

    /**
     * Determines the right status for an item based on its usage and value.
     * This is where the logic for determining state is done.
     * Perishables are checked first since expiry overrides all other rules.
     */
    public ItemStatus suggestStatus(Item item) {
        if (item instanceof Perishables p) {
            // opened and expired becomes discard
            if (p.isExpired()) return ItemStatus.DISCARD;
            // never opened but purchased over a year ago is likely forgotten, discard
            // using purchase date instead of last used date since pressing "Mark as recently used" shouldn't change the
            // fact that it was never opened

            if (p.getOpenedDate() == null) {
                long daysSincePurchase = java.time.temporal.ChronoUnit.DAYS.between(
                        item.getPurchaseDate(), java.time.LocalDate.now()
                );
                if (daysSincePurchase > 365) return ItemStatus.DISCARD;
            }
        }
        //high value unused items are better for selling than for donating
        if (item.daysSinceLastUsed() >= INACTIVE_DAYS_THRESHOLD) {
            return item.getPrice() >= HIGH_VALUE_THRESHOLD ? ItemStatus.SELL : ItemStatus.DONATE;
        }
        return ItemStatus.KEEP;
    }

    /**
     * returns flagged items sorted by removal urgency
     * Expired perishables always appear first which means they need immediate attention.
     * remaining items are sorted by days unused descending, then price ascending
     * This means the longest ignored and cheapest items are addressed first.
     */

    public List<Item> getPrioritizedRemovalList() {
        List<Item> expired = new ArrayList<>();
        List<Item> flagged = new ArrayList<>();

        for (Item item : getFlaggedItems()) {
            // separate expired perishables so they always appear at the top
            if (item instanceof Perishables p && p.isExpired()) {
                expired.add(item);
            } else {
                flagged.add(item);
            }
        }

        // sort non-expired flagged items by days unused then price
        flagged.sort(Comparator
                .comparingLong(Item::daysSinceLastUsed).reversed()
                .thenComparingDouble(Item::getPrice));

        // expired perishables always go first, then sort the flagged items
        List<Item> result = new ArrayList<>();
        result.addAll(expired);
        result.addAll(flagged);
        return result;
    }

    public double getTotalSpent() {
        return itemRepository.findAll().stream()
                .mapToDouble(Item::getPrice)
                .sum();
    }
//counts money tied up in flagged items, not total inventory value
    public double getWastedSpend() {
        return getFlaggedItems().stream()
                .mapToDouble(Item::getPrice)
                .sum();
    }

    public int getTotalItemCount() {
        return (int) itemRepository.count();
    }

    // status is calculated immediately on save so the database is always up to date
    // additionally, manually locked items are skipped since the user has overridden the auto-suggestion
    public void saveItem(Item item) {
        if (!item.isManuallySet()) {
            item.setStatus(suggestStatus(item));
        }
        itemRepository.save(item);
    }


    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    // recalculates status after marking as used unless the item is manually locked
    public void markItemUsed(Long id) {
        Item item = itemRepository.findById(id).orElseThrow();
        item.setLastUsedDate(LocalDate.now());
        if (!item.isManuallySet()) {
            item.setStatus(suggestStatus(item));
        }
        itemRepository.save(item);
    }


    // Locks the item status, causing the auto-suggest to no longer override the item's stat
    public void setManualStatus(Long id, ItemStatus status) {
        Item item = itemRepository.findById(id).orElseThrow();
        item.setStatus(status);
        item.setManuallySet(true);
        itemRepository.save(item);
    }

    // unlocks the item and immediately recalculates status based on current usage.
    // item is never left in a wrong state after being unlocked
    public void resetAutoStatus(Long id) {
        Item item = itemRepository.findById(id).orElseThrow();
        item.setManuallySet(false);
        item.setStatus(suggestStatus(item));
        itemRepository.save(item);
    }
}