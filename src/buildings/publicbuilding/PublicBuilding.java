package buildings.publicbuilding;

import java.util.Map;

import buildings.Buildable;
import city.City;

public abstract class PublicBuilding extends Buildable {
    public abstract int getMaintanenceCostPerDay();

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("maintanence", Integer.toString(getMaintanenceCostPerDay()));
        return details;
    }

    public Upgrade[] getUpgrades() {
        return new Upgrade[0];
    }

    public abstract class Upgrade {
        abstract public int getPrice();

        public String getName() {
            return this.getClass().getSimpleName();
        }

        protected boolean isBuilt = false;

        public boolean getIsBuilt() {
            return isBuilt;
        }

        public void build(City city) {
            isBuilt = city.build(this);
        }
    }
}
