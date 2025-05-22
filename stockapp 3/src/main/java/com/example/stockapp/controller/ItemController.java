package com.example.stockapp.controller;

import com.example.stockapp.model.Item;
import com.example.stockapp.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ItemController {

    @Autowired
    private ItemService service;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("items", service.getAllItems());
        return "index";
    }

    @GetMapping("/sorted")
    public String sorted(Model model) {
        model.addAttribute("items", service.getSortedItems());
        return "index";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        model.addAttribute("minDate", LocalDate.now().toString());
        return "form";
    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, Model model) {
        boolean isDuplicate = service.getAllItems().stream()
                .anyMatch(existing -> existing.getId().equalsIgnoreCase(item.getId()));

        if (isDuplicate) {
            model.addAttribute("error", "❗ Item ID already exists!");
            model.addAttribute("item", new Item()); // reset form (not treated as edit)
            model.addAttribute("minDate", LocalDate.now().toString());
            return "form";
        }

        service.addItem(item);
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable String id, Model model) {
        Item found = service.getAllItems().stream()
                .filter(i -> i.getId().equals(id))
                .findFirst().orElse(null);

        model.addAttribute("item", found);
        model.addAttribute("minDate", LocalDate.now().toString());
        return "form";
    }

    @PostMapping("/edit")
    public String updateItem(@ModelAttribute Item item) {
        service.updateItem(item);
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable String id) {
        service.deleteItem(id);
        return "redirect:/";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String urgency,
                         Model model) {
        List<Item> results = service.getAllItems();

        if (keyword != null && !keyword.isEmpty()) {
            results = results.stream()
                    .filter(i -> i.getName().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
        }

        if (urgency != null && !urgency.isEmpty()) {
            if (urgency.equalsIgnoreCase("urgent")) {
                results = results.stream().filter(Item::isUrgent).toList();
            } else if (urgency.equalsIgnoreCase("normal")) {
                results = results.stream().filter(i -> !i.isUrgent()).toList();
            }
        }

        model.addAttribute("items", results);
        return "index";
    }
}
