package buildings.publicbuilding.service.police;

import buildings.publicbuilding.service.PublicServiceBuilding;

import city.City;
import utils.SerializedBlob;

public abstract class PoliceStation extends PublicServiceBuilding {
    public PoliceStation() {
        super();
    }

    protected PoliceStation(SerializedBlob blob, City city) {
        super(blob, city);
    }

    public abstract int getCrimeReduction(int x, int y);

    @Override
    public Class<? extends PublicServiceBuilding> getPublicServiceTypeClass() {
        return PoliceStation.class;
    }
}
