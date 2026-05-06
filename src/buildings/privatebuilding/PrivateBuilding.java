package buildings.privatebuilding;

import java.util.Map;

import buildings.Buildable;
import buildings.privatebuilding.residential.ResidentialBuilding;
import buildings.privatebuilding.workplace.commercial.CommercialBuilding;
import buildings.privatebuilding.workplace.industrial.IndustrialBuilding;
import buildings.privatebuilding.workplace.office.OfficeBuilding;
import city.City;

import utils.SerializedBlob;

public abstract class PrivateBuilding extends Buildable {
    public PrivateBuilding() {
        super();
    }

    protected PrivateBuilding(SerializedBlob blob, City city) {
        super(blob, city);
        isBuilt = blob.map().get("isBuilt").booleanValue();
    }

    protected boolean isBuilt = false;

    public boolean getIsBuilt() {
        return this.isBuilt;
    }

    public void build() {
        this.isBuilt = true;
    }

    public abstract int calculateProfitPerTick();

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("is built", isBuilt ? "yes" : "no");
        details.put("current tax revenue", Integer.toString(calculateProfitPerTick()));
        return details;
    }

    public static int calculateDemand(PrivateBuilding building, City city) {
        return switch (building) {
            case ResidentialBuilding r -> ResidentialBuilding.calculateDemand(city);
            case IndustrialBuilding r -> IndustrialBuilding.calculateDemand(city);
            case CommercialBuilding r -> CommercialBuilding.calculateDemand(city);
            case OfficeBuilding r -> OfficeBuilding.calculateDemand(city);
            default -> 0;
        };
    }
}
