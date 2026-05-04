package city;

import buildings.Buildable;
import buildings.privatebuilding.residential.ResidentialBuilding;
import buildings.privatebuilding.workplace.WorkplaceBuilding;

public class Citizen {
    private Buildable location;
    private ResidentialBuilding home;
    private WorkplaceBuilding workplace;
    private int currentHealth;

    public void setLocation(Buildable location) {
        this.location = location;
    }

    public Buildable getLocation() {
        return location;
    }

    public void setHome(ResidentialBuilding home) {
        this.home = home;
    }

    public ResidentialBuilding getHome() {
        return home;
    }

    public void setWorkplace(WorkplaceBuilding workplace) {
        this.workplace = workplace;
    }

    public WorkplaceBuilding getWorkplace() {
        return workplace;
    }

    public CitizenState getCurrentCitizenState() {
        return null;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int health) {
        this.currentHealth = health;
    }

    public int getSatisfaction() {
        return 0;
    }

    public String getCurrentThoughts() {
        return null;
    }
}
