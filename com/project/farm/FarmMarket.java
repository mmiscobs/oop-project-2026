import java.util.Map;

import com.project.buildings.privatebuilding.workplace.commercial.CommercialBuilding;

public class FarmMarket extends CommercialBuilding {
    private Map<String, Integer> inventory;

    public int sellCrop(Crop crop, int quantity) {
        int profit = 0;
        for (String s : this.inventory.keySet()) {
            if (crop.getName().equals(s) && inventory.get(s) >= quantity) {
                profit = crop.getMarketprice() * quantity;
                this.inventory.put(s, this.inventory.get(s) - quantity);
            }
        }
        return profit;
    }

    public int getPriceOf(Crop crop) {
        return crop.getMarketprice();
    }

    @Override
    public int getVisitorsCapacity() {
        return 10;
    }

    @Override
    public int getWorkersCapacity() {
        return 10;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public int getPrice() {
        return 15000;
    }
}
