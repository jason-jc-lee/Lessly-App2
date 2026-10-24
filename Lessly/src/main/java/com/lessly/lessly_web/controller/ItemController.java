package com.lessly.lessly_web.controller;
import com.lessly.lessly_web.ai.GeminiAIAdvisor;
import com.lessly.lessly_web.model.*;
import com.lessly.lessly_web.service.DeclutterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.lessly.lessly_web.model.ItemStatus;
import java.time.LocalDate;
import java.util.List;


/**
 * ItemController handles all incoming web requests and routes them to the correct page. It does not contain any logic that makes the decisions as those are passed on.
 * This separation ensures that changes to the UI don't affect core logic and vice versa.
 */

@Controller
public class ItemController {

    @Autowired
    private DeclutterService declutterService;

    // home page: shows inventory
    @GetMapping("/")
    public String home(Model model) {
        List<Item> items = declutterService.getAllItems();
        List<Item> priorityList = declutterService.getPrioritizedRemovalList();

        model.addAttribute("items", items);

        model.addAttribute("totalItems", declutterService.getTotalItemCount());
        model.addAttribute("totalSpent", declutterService.getTotalSpent());
        model.addAttribute("flaggedItems", declutterService.getFlaggedItems());
        return "index";
    }

    @Autowired
    private GeminiAIAdvisor aiAdvisor;
    //private MockAIAdvisor aiAdvisor;
    // You may swap to MockAIAdvisor for testing or submission without an API key by uncommenting that line.

//report page: computes flagged items, removal priorities, and ai summary
    @GetMapping("/report")
    public String report(Model model) {
        List<Item> flagged = declutterService.getFlaggedItems();
        double wastedSpend = declutterService.getWastedSpend();
        int totalItems = declutterService.getTotalItemCount();
        double totalSpent = declutterService.getTotalSpent();
        List<Item> removalList = declutterService.getPrioritizedRemovalList();

        // get top 3 item names for AI
        List<String> removalNames = removalList.stream()
                .limit(3)
                .map(Item::getName)
                .collect(java.util.stream.Collectors.toList());

        String aiSummary = aiAdvisor.generateSummary(flagged.size(), wastedSpend, totalItems, totalSpent, removalNames);

        model.addAttribute("flaggedItems", flagged);
        model.addAttribute("removalList", removalList);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("wastedSpend", wastedSpend);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("aiSummary", aiSummary);
        return "report";
    }

    // add clothing, redirects home upon success, remains on page upon error
    @PostMapping("/add/clothing")
    public String addClothing(
            @RequestParam String name,
            @RequestParam String brand,
            @RequestParam double price,
            @RequestParam String purchaseDate,
            @RequestParam String size,
            @RequestParam String color,
            Model model) {
        try {
            Clothing item = new Clothing(name, brand, price, LocalDate.parse(purchaseDate), size, color);
            declutterService.saveItem(item);
        } catch (Exception e) {
            //return validation error to the add page instead of crashing
            model.addAttribute("error", e.getMessage());
            return "add";
        }
        return "redirect:/";
    }

    // add electronics,same logic as clothing
    @PostMapping("/add/electronics")
    public String addElectronics(
            @RequestParam String name,
            @RequestParam String brand,
            @RequestParam double price,
            @RequestParam String purchaseDate,
            @RequestParam String model2,
            @RequestParam int warrantyMonths,
            Model model) {
        try {
            Electronics item = new Electronics(name, brand, price, LocalDate.parse(purchaseDate), model2, warrantyMonths);
            declutterService.saveItem(item);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "add";
        }
        return "redirect:/";
    }

    // Add perishables, the openedDate property is optional as certain items may not be opened yet
    @PostMapping("/add/perishables")
    public String addPerishables(
            @RequestParam String name,
            @RequestParam String brand,
            @RequestParam double price,
            @RequestParam String purchaseDate,
            @RequestParam(required = false) String openedDate,
            @RequestParam int shelfLifeDays,
            Model model) {
        try {
            //openeddate is nullable, if not provided, expiry will depend on whether the item hasnt been used in a year
            LocalDate opened = (openedDate != null && !openedDate.isBlank()) ? LocalDate.parse(openedDate) : null;
            Perishables item = new Perishables(name, brand, price, LocalDate.parse(purchaseDate), opened, shelfLifeDays);
            declutterService.saveItem(item);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "add";
        }
        return "redirect:/";
    }

    // mark item as used and will recalculate its status
    @PostMapping("/markused/{id}")
    public String markUsed(@PathVariable Long id) {
        declutterService.markItemUsed(id);
        return "redirect:/";
    }

    // permanently removes item from database
    @PostMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id) {
        declutterService.deleteItem(id);
        return "redirect:/";
    }

    //manually locks an item's status, causing the auto-suggestion and auto updating to no longer override
    @PostMapping("/setstatus/{id}")
    public String setStatus(@PathVariable Long id, @RequestParam String status) {
        declutterService.setManualStatus(id, ItemStatus.valueOf(status));
        return "redirect:/";
    }
//unlocks an item's status  and recalculates
    @PostMapping("/resetstatus/{id}")
    public String resetStatus(@PathVariable Long id) {
        declutterService.resetAutoStatus(id);
        return "redirect:/";
    }

    // associated with the add item page
    @GetMapping("/add")
    public String addPage() {
        return "add";
    }
}