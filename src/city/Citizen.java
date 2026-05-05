package city;

import buildings.Buildable;
import city.IdentityGenerator.Identity;

public class Citizen {
    private Buildable location;
    private int currentHealth;
    public final Identity identity;

    public Citizen() {
        this.identity = IdentityGenerator.generator.nextIdentity();
    }

    public void setLocation(Buildable location) {
        this.location = location;
    }

    public Buildable getLocation() {
        return location;
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
