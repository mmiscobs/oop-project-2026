package city;

import java.util.ArrayList;
import java.util.List;

import buildings.Buildable;
import loans.Loan;
import utils.Point;

public class City {
    private int money;
    public CityGrid grid;
    private ArrayList<Citizen> citizens;
    private ArrayList<Loan> loans;

    public City(int sizeX, int sizeY) {
        this.grid = new CityGrid(this, sizeX, sizeY);
    }

    public void build(Buildable building, Point place) {
        if (building.getPrice() > money)
            return;
        money -= building.getPrice();
        grid.placeBuildingAt(place, building);
    }

    public int getMoney() {
        return money;
    }

    public List<Citizen> getCitizens() {
        return List.copyOf(citizens);
    }

    public void takeOutLoan(Loan loan) {
        this.loans.add(loan);
    }
}
