package city;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import buildings.Buildable;
import buildings.privatebuilding.residential.ResidentialBuilding;
import buildings.privatebuilding.workplace.WorkplaceBuilding;
import buildings.privatebuilding.workplace.commercial.CommercialBuilding;
import buildings.publicbuilding.service.PublicServiceBuilding;
import buildings.publicbuilding.service.healthcare.HealthcareBuilding;
import buildings.publicbuilding.transportation.PublicTransportation;
import city.IdentityGenerator.Identity;
import utils.Point;

public class Citizen {
    public Buildable location;
    public ResidentialBuilding home;
    public WorkplaceBuilding work;
    private int currentHealth = 100;
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
        updateHealth();
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

    private void updateHealth() {
        if (location instanceof PublicTransportation road) {
            currentHealth -= (int) Math.ceilDiv(road.computeNoiseLevel(), 5);
        } else {
            currentHealth += (int) Math.ceilDiv(100 - currentHealth, 10);
        }
        currentHealth = Math.clamp(currentHealth, 0, 100);
        if (location == null)
            return;
        Point locationPoint = city.grid.getBuildingOrigin(location);
        int healthCoverage = PublicServiceBuilding.getFieldFunctionForPublicServiceType(city, HealthcareBuilding.class)
                .apply(locationPoint).intValue();
        currentHealth += (int) Math.ceilDiv(Math.max(currentHealth, healthCoverage) - currentHealth, 4);
        currentHealth = Math.clamp(currentHealth, 0, 100);
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
        updateCurrentThoughtsSeed();
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
        Integer[] satisfactionContributors = new Integer[] {
                currentHealth,
                work == null ? 50 : 100,
                home == null ? 10 : 100,
                location == null ? 100 : (int) (Math.pow((100 - location.getCrimeRate()) / 100.0, 2) * 100)
        };

        return List.of(satisfactionContributors).stream().reduce(0, Integer::sum) / satisfactionContributors.length;
    }

    private int currentThoughtsSeed = new Random().nextInt();

    private void updateCurrentThoughtsSeed() {
        this.currentThoughtsSeed = new Random().nextInt();
    }

    public Map<String, String> getDetailedInfo() {
        HashMap<String, String> details = new HashMap<>();

        details.put("Name", identity.fullName());
        details.put("Birthyear", Integer.toString(identity.birthYear()));
        details.put("Current thoughts", getCurrentThoughts());
        details.put("Health", Integer.toString(getCurrentHealth()));
        details.put("Satisfaction", Integer.toString(getSatisfaction()));
        details.put("Current state", getCurrentStateDescription());
        if (work == null)
            details.put("Unemployed", "yes");
        if (home == null)
            details.put("Homeless", "yes");

        return details;
    }

    public String getCurrentStateDescription() {
        return switch (state) {
            case CitizenState.Residing r -> home == null ? "Homeless" : home == location ? "Residing" : "Going home";
            case CitizenState.Working w -> work == location ? "Working" : "Going to work";
            case CitizenState.Shopping(CommercialBuilding shop) -> shop == location ? "Shopping" : "Going to shop";
        };
    }

    public String getCurrentThoughts() {
        Supplier<Boolean> coinFlip = () -> new Random(currentThoughtsSeed).nextDouble(0, 1) > 0.5;
        if (work == null && coinFlip.get())
            return "If only I could have a job...";
        if (home == null && coinFlip.get())
            return "I wish I had a nice apartment...";
        if (location.getCrimeRate() > 75 && coinFlip.get())
            return "It is dangerous to be here!";
        if (location.getCrimeRate() > 25 && coinFlip.get())
            return "Would be nice to have more police around...";
        if (currentHealth > 90 && coinFlip.get())
            return "I feel rejuvenated!";
        if (currentHealth < 40 && coinFlip.get())
            return "Couldn't last much longer with such health...";
        if (location instanceof PublicTransportation road && road.getCongestion() > 50)
            return "The traffic is terrible...";
        if (location instanceof PublicTransportation road && road.computeNoiseLevel() > 50)
            return "How much hum I could stand?!";
        if (location instanceof PublicTransportation road && road.computeNoiseLevel() < 5)
            return "Silent bliss...";
        if (location instanceof PublicTransportation road && road.getCongestion() < 5)
            return "So nice when no one is driving around you!";
        return "Nothing in particular";
    }
}
