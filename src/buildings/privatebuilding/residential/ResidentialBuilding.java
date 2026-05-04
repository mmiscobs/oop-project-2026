package buildings.privatebuilding.residential;

import buildings.privatebuilding.PrivateBuilding;

public abstract class ResidentialBuilding extends PrivateBuilding {
    public abstract int getCapacity();

    public static int calculateDemand() {
        return 0;
    }

    public int getResidents() {
        return 0;
    }

    public void addResident() {
    }
}
