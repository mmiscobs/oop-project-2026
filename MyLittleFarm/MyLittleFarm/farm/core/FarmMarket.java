package farm.core;

import java.util.HashMap;
import java.util.Map;

public class FarmMarket {
    private final Map<String, Integer> inventory  = new HashMap<>();
    private final Map<String, Integer> priceIndex = new HashMap<>();

    public FarmMarket() {}
    public void addToInventory(String cropName, int quantity, int pricePerUnit) {
        inventory.merge(cropName, quantity, Integer::sum);
        priceIndex.putIfAbsent(cropName, pricePerUnit);
    }
    public int sellCrop(String cropName, int quantity) {
        int stock = inventory.getOrDefault(cropName, 0);
        if (stock < quantity) return 0;
        int price = priceIndex.getOrDefault(cropName, 0);
        int profit = price * quantity;
        inventory.put(cropName, stock - quantity);
        return profit;
    }

    public int sellCrop(Crop crop, int quantity) {
        return sellCrop(crop.getName(), quantity);
    }

    public int getPriceOf(String cropName) {
        return priceIndex.getOrDefault(cropName, 0);
    }

    public int getPriceOf(Crop crop) {
        return getPriceOf(crop.getName());
    }

    public Map<String, Integer> getInventory()  {
        return new HashMap<>(inventory);
    }
    public Map<String, Integer> getPriceIndex() {
        return new HashMap<>(priceIndex);
    }
    public int getVisitorsCapacity() {
        return 50;
    }
}
