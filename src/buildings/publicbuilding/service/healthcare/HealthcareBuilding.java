package buildings.publicbuilding.service.healthcare;

import buildings.publicbuilding.service.PublicServiceBuilding;

import city.City;
import utils.SerializedBlob;

public abstract class HealthcareBuilding extends PublicServiceBuilding {
    public HealthcareBuilding() {
        super();
    }

    protected HealthcareBuilding(SerializedBlob blob, City city) {
        super(blob, city);
    }

    public abstract int getHealthIncrease(int x, int y);

    @Override
    public Class<? extends PublicServiceBuilding> getPublicServiceTypeClass() {
        return HealthcareBuilding.class;
    }
}
