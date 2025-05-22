package com.example.inventory_stack_management.controller;

import com.example.inventory_stack_management.dto.ItemDto;
import com.example.inventory_stack_management.service.InventoryStackService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; 
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/ui/stock") 
public class InventoryUIController {

    private final InventoryStackService inventoryService; 

    @Autowired
    public InventoryUIController(InventoryStackService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        model.addAttribute("items", inventoryService.getAllItems());
        
        return "admin-dashboard";
    }

    @GetMapping("/items/add")
    public String showAddItemForm(Model model) {
        model.addAttribute("itemDto", new ItemDto()); // For form binding
        model.addAttribute("pageTitle", "Add New Item");
        model.addAttribute("formAction", "/ui/stock/items/save");
        return "item-form"; // New HTML file: item-form.html
    }


    @PostMapping("/items/save")
    public String saveOrUpdateItem(@ModelAttribute ItemDto itemDto,
                                   RedirectAttributes redirectAttributes) {
        // Basic validation for now, add @Valid and BindingResult for more robust validation
        if (itemDto.getItemCode() == null || itemDto.getItemCode().trim().isEmpty() ||
                itemDto.getName() == null || itemDto.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Item Code and Name are required.");
           
            if (isUpdating(itemDto)) { 
                redirectAttributes.addFlashAttribute("itemDto", itemDto); 
                return "redirect:/ui/stock/items/edit/" + itemDto.getItemCode();
            }
            return "redirect:/ui/stock/items/add";
        }

        try {
        
            Optional<ItemDto> existingItem = inventoryService.getItemByCode(itemDto.getItemCode());
            if (isUpdating(itemDto) && existingItem.isPresent()) {
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

    private boolean isUpdating(ItemDto itemDto) {
        return inventoryService.getItemByCode(itemDto.getItemCode()).isPresent();
    }


    // Show "Edit Item" form
    @GetMapping("/items/edit/{itemCode}")
    public String showEditItemForm(@PathVariable String itemCode, Model model, RedirectAttributes redirectAttributes) {
        Optional<ItemDto> itemOpt = inventoryService.getItemByCode(itemCode);
        if (itemOpt.isPresent()) {
            model.addAttribute("itemDto", itemOpt.get());
            model.addAttribute("pageTitle", "Edit Item");
            model.addAttribute("formAction", "/ui/stock/items/save");
            return "item-form";
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

    
    @GetMapping("/items/view")
    public String showItemsCardView(Model model) {
        model.addAttribute("items", inventoryService.getAllItems());
        return "item-view-cards";
    }
}
