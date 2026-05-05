package city;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import buildings.Buildable;
import buildings.privatebuilding.residential.ResidentialBuilding;
import buildings.privatebuilding.workplace.WorkplaceBuilding;
import buildings.privatebuilding.workplace.commercial.CommercialBuilding;
import city.IdentityGenerator.Identity;

public class Citizen {
    public Buildable location;
    public ResidentialBuilding home;
    public WorkplaceBuilding work;
    private int currentHealth;
    public final Identity identity;
    public int lastStateUpdateTick = 0;
    private City city;

    public Citizen(City city, ResidentialBuilding home, WorkplaceBuilding work) {
        this.identity = IdentityGenerator.generator.nextIdentity();
        this.city = city;
        this.home = home;
        this.work = work;
        this.location = home;
    }

    private void setLocation(Buildable loc) {
        if (loc == null)
            return;
        if (loc == location)
            return;
        location.removeVisitor(this);
        loc.addVisitor(this);
        if (loc.getVisitors().contains(this))
            location = loc;
    }

    public CitizenState state = new CitizenState.Residing();

    public void runSimulationTick(int tick) {
        runStateUpdate(tick);
        switch (state) {
            case CitizenState.Residing r -> {
                if (home != null)
                    setLocation(getNextLocationToGetTo(home));
            }
            case CitizenState.Working w -> {
                if (work != null)
                    setLocation(getNextLocationToGetTo(work));
            }
            case CitizenState.Shopping(CommercialBuilding shop) -> {
                setLocation(getNextLocationToGetTo(shop));
            }
        }
    }

    private Buildable getNextLocationToGetTo(Buildable target) {
        if (target == null)
            return null;
        return city.grid.getNextStepFromTo(location, target);
    }

    private void runStateUpdate(int tick) {
        if (tick - lastStateUpdateTick > new Random().nextInt(5, 15))
            lastStateUpdateTick = tick;
        else
            return;
        outer: switch (new Random().nextInt(0, 3)) {
            case 0: {
                List<Buildable> shuffled = new ArrayList<>(city.builtBuildings());
                Collections.shuffle(shuffled);
                for (Buildable building : shuffled) {
                    if (building instanceof CommercialBuilding) {
                        CommercialBuilding commercialBuilding = (CommercialBuilding) building;
                        state = new CitizenState.Shopping(commercialBuilding);
                        break outer;
                    }
                }
            }
            case 1: {
                state = new CitizenState.Residing();
                break;
            }
            default: {
                state = new CitizenState.Working();
                break;
            }
        }
    }

    public Buildable getLocation() {
        return location;
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
