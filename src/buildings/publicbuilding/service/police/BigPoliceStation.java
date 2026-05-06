package buildings.publicbuilding.service.police;

import java.util.Map;

import buildings.Buildable;
import buildings.publicbuilding.PublicBuilding;

import city.City;
import utils.SerializedBlob;

public class BigPoliceStation extends PoliceStation {
    public BigPoliceStation() {
        super();
    }

    protected BigPoliceStation(SerializedBlob blob, City city) {
        super(blob, city);
        this.helipad = new Helipad(blob.map().get("helipad"));
    }

    public SerializedBlob toBlob() {
        return super.toBlob().extendMap(Map.of("helipad", helipad.toBlob()));
    }

    static {
        Buildable.registry.put(BigPoliceStation.class, BigPoliceStation::new);
        Buildable.blobRegistry.put(BigPoliceStation.class, BigPoliceStation::new);
    }

    public Upgrade[] getUpgrades() {
        return new Upgrade[] { this.helipad };
    }

    private Helipad helipad = new Helipad();

    class Helipad extends PublicBuilding.Upgrade {
        Helipad() {
        }

        Helipad(SerializedBlob blob) {
            super(blob);
        }

        public int getPrice() {
            return 3000;
        }
    }

    public int getPrice() {
        return 25000;
    }

    public int getMaintanenceCostPerDay() {
        return 1000;
    }

    public int getRange() {
        return 8 + (helipad.getIsBuilt() ? 2 : 0);
    }

    public int getCrimeReduction(int x, int y) {
        return 3;
    }

    @Override
    public int getWidth() {
        return 2;
    }

    @Override
    public int getLength() {
        return 2;
    }

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("has helipad", helipad.getIsBuilt() ? "yes" : "no");
        return details;
    }
}
