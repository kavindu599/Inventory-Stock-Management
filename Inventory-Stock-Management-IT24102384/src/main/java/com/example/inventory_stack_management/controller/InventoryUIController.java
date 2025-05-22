package com.example.inventory_stack_management.controller;

import com.example.inventory_stack_management.dto.ItemDto;
import com.example.inventory_stack_management.service.InventoryStackService; // Or your renamed service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // For form validation
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal; // Import BigDecimal
import java.util.Optional;

@Controller
@RequestMapping("/ui/stock") // Changed base path for clarity
public class InventoryUIController {

    private final InventoryStackService inventoryService; // Renamed for clarity

    @Autowired
    public InventoryUIController(InventoryStackService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // Display Admin Dashboard / Item List (similar to image 3)
    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        model.addAttribute("items", inventoryService.getAllItems());
        // You can add other dashboard stats here later
        // model.addAttribute("totalItems", inventoryService.getAllItems().size());
        // model.addAttribute("categoriesCount", ...);
        return "admin-dashboard"; // New HTML file: admin-dashboard.html
    }

    // Show "Add New Item" form (similar to image 2)
    @GetMapping("/items/add")
    public String showAddItemForm(Model model) {
        model.addAttribute("itemDto", new ItemDto()); // For form binding
        model.addAttribute("pageTitle", "Add New Item");
        model.addAttribute("formAction", "/ui/stock/items/save");
        return "item-form"; // New HTML file: item-form.html
    }

    // Process "Add New Item" or "Update Item" form submission
    @PostMapping("/items/save")
    public String saveOrUpdateItem(@ModelAttribute ItemDto itemDto, /* @Valid ItemDto itemDto, BindingResult result, */
                                   RedirectAttributes redirectAttributes) {
        // Basic validation for now, add @Valid and BindingResult for more robust validation
        if (itemDto.getItemCode() == null || itemDto.getItemCode().trim().isEmpty() ||
                itemDto.getName() == null || itemDto.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Item Code and Name are required.");
            // If it was an update, redirect to edit form, else to add form
            // For simplicity, redirecting to dashboard for now on error, or back to a generic form
            if (isUpdating(itemDto)) { // You'd need a way to know if it's an update
                redirectAttributes.addFlashAttribute("itemDto", itemDto); // Send back DTO to repopulate form
                return "redirect:/ui/stock/items/edit/" + itemDto.getItemCode();
            }
            return "redirect:/ui/stock/items/add";
        }

        try {
            // Check if item with this code already exists to decide between add/update
            Optional<ItemDto> existingItem = inventoryService.getItemByCode(itemDto.getItemCode());
            if (isUpdating(itemDto) && existingItem.isPresent()) { // Assuming 'isUpdating' can be determined
                inventoryService.updateItem(itemDto.getItemCode(), itemDto);
                redirectAttributes.addFlashAttribute("message", "Item '" + itemDto.getName() + "' updated successfully!");
            } else if (!existingItem.isPresent()){
                inventoryService.addItem(itemDto);
                redirectAttributes.addFlashAttribute("message", "Item '" + itemDto.getName() + "' added successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Item code '" + itemDto.getItemCode() + "' already exists for a new item.");
                redirectAttributes.addFlashAttribute("itemDto", itemDto);
                return "redirect:/ui/stock/items/add";
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (isUpdating(itemDto)) {
                redirectAttributes.addFlashAttribute("itemDto", itemDto);
                return "redirect:/ui/stock/items/edit/" + itemDto.getItemCode();
            }
            redirectAttributes.addFlashAttribute("itemDto", itemDto);
            return "redirect:/ui/stock/items/add";
        }
        return "redirect:/ui/stock/dashboard";
    }

    // Helper to determine if it's an update (e.g., if a hidden field was submitted, or if originalItemCode was passed)
    // For a real update, you'd typically fetch the item by ID first and then map form fields.
    // This is a simplified check.
    private boolean isUpdating(ItemDto itemDto) {
        // A simple way: if we have an item code, and it exists in our inventory, it might be an update.
        // A better way: have a separate endpoint for update POST or a hidden field in the form.
        // For this example, we'll assume any save to an existing itemCode is an update if fetched via edit.
        // The logic in saveOrUpdateItem() tries to infer this.
        // A more robust way for edit:
        // 1. GET /ui/stock/items/edit/{itemCode} loads the item into the form.
        // 2. Form POSTs to /ui/stock/items/update/{itemCode} (or /save with original item code)
        return inventoryService.getItemByCode(itemDto.getItemCode()).isPresent();
    }


    // Show "Edit Item" form
    @GetMapping("/items/edit/{itemCode}")
    public String showEditItemForm(@PathVariable String itemCode, Model model, RedirectAttributes redirectAttributes) {
        Optional<ItemDto> itemOpt = inventoryService.getItemByCode(itemCode);
        if (itemOpt.isPresent()) {
            model.addAttribute("itemDto", itemOpt.get());
            model.addAttribute("pageTitle", "Edit Item");
            model.addAttribute("formAction", "/ui/stock/items/save"); // Could be a dedicated /update endpoint
            return "item-form"; // Reuse item-form.html
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Item with code '" + itemCode + "' not found.");
            return "redirect:/ui/stock/dashboard";
        }
    }

    // Handle "Delete Item"
    @PostMapping("/items/delete/{itemCode}")
    public String deleteItem(@PathVariable String itemCode, RedirectAttributes redirectAttributes) {
        boolean deleted = inventoryService.deleteItemByCode(itemCode);
        if (deleted) {
            redirectAttributes.addFlashAttribute("message", "Item '" + itemCode + "' deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete item '" + itemCode + "'. It might not exist.");
        }
        return "redirect:/ui/stock/dashboard";
    }

    // You can add a method for Image 4 (View Items - card style)
    @GetMapping("/items/view")
    public String showItemsCardView(Model model) {
        model.addAttribute("items", inventoryService.getAllItems());
        return "item-view-cards"; // New HTML file: item-view-cards.html
    }
}