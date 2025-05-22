package com.example.stockapp.util;

import com.example.stockapp.model.Item;

import java.util.ArrayList;
import java.util.List;

public class MergeSort {

    public static List<Item> sort(List<Item> items) {
        if (items.size() <= 1) return items;

        int mid = items.size() / 2;
        List<Item> left = sort(items.subList(0, mid));
        List<Item> right = sort(items.subList(mid, items.size()));

        return merge(left, right);
    }

    private static List<Item> merge(List<Item> left, List<Item> right) {
        List<Item> result = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            if (left.get(i).getExpiryDate().isBefore(right.get(j).getExpiryDate())) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }

        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));

        return result;
    }
}
