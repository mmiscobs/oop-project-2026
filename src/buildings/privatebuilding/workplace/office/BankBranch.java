package buildings.privatebuilding.workplace.office;

import buildings.Buildable;

public class BankBranch extends OfficeBuilding {
    static {
        Buildable.registry.put(BankBranch.class, BankBranch::new);
    }

    public int getPrice() {
        return 1000;
    }

    public void setCrimeRate(int crimeRateReduction) {
    }

    public int getWorkersCapacity() {
        return 30;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getLength() {
        return 1;
    }
}
