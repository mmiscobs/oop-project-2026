package buildings.publicbuilding.service.police;

import buildings.publicbuilding.service.PublicServiceBuilding;

public abstract class PoliceStation extends PublicServiceBuilding {
    public abstract int getCrimeReduction(int x, int y);
}
