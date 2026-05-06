package city;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import buildings.Buildable;
import buildings.Buildable.BuildableRef;
import buildings.privatebuilding.residential.ResidentialBuilding;
import buildings.privatebuilding.workplace.WorkplaceBuilding;
import buildings.privatebuilding.workplace.commercial.CommercialBuilding;
import buildings.publicbuilding.service.PublicServiceBuilding;
import buildings.publicbuilding.service.healthcare.HealthcareBuilding;
import buildings.publicbuilding.transportation.PublicTransportation;
import city.IdentityGenerator.Identity;
import utils.Point;
import utils.SerializedBlob;

public class Citizen {
    public final String id;
    public BuildableRef<Buildable> location;
    public BuildableRef<ResidentialBuilding> home;
    public BuildableRef<WorkplaceBuilding> work;
    private int currentHealth = 100;
    public final Identity identity;
    public int lastStateUpdateTick = 0;
    private City city;
    private int currentThoughtsSeed = new Random().nextInt();
    public CitizenState state = new CitizenState.Residing();

    public void evict() {
        this.home = new BuildableRef<ResidentialBuilding>((Buildable) null, city);
    }

    public Citizen(City city, ResidentialBuilding home, WorkplaceBuilding work) {
        this.id = UUID.randomUUID().toString();
        this.identity = IdentityGenerator.generator.nextIdentity();
        this.city = city;
        this.home = new BuildableRef<>(home, city);
        this.work = new BuildableRef<>(work, city);
        this.location = new BuildableRef<>(home, city);
    }

    private static WeakHashMap<String, Citizen> cache = new WeakHashMap<>();

    private Citizen(SerializedBlob blob, City city) {
        this.city = city;
        this.id = blob.map().get("id").string();
        lastStateUpdateTick = blob.map().get("lastStateUpdateTick").intValue();
        currentHealth = blob.map().get("currentHealth").intValue();
        location = new BuildableRef<>(blob.map().get("location"), city);
        work = new BuildableRef<>(blob.map().get("work"), city);
        home = new BuildableRef<>(blob.map().get("home"), city);
        identity = new Identity(blob.map().get("identity"));
        state = CitizenState.fromBlob(blob.map().get("state"), city);
        currentThoughtsSeed = blob.map().get("currentThoughtsSeed").intValue();
        cache.put(id, this);
    }

    public SerializedBlob toBlob() {
        return SerializedBlob.fromMap(Map.of(
                "id", SerializedBlob.string(id),
                "identity", identity.toBlob(),
                "home", home.toBlob(),
                "work", work.toBlob(),
                "location", location.toBlob(),
                "state", state.toBlob(),
                "lastStateUpdateTick", SerializedBlob.intValue(lastStateUpdateTick),
                "currentHealth", SerializedBlob.intValue(currentHealth),
                "currentThoughtsSeed", SerializedBlob.intValue(currentThoughtsSeed)));
    }

    public static Citizen fromBlob(SerializedBlob blob, City city) {
        String id = blob.map().get("id").string();
        return cache.getOrDefault(id, new Citizen(blob, city));
    }

    private void setLocation(Buildable loc) {
        if (loc == null)
            return;
        if (loc == location.get())
            return;
        location.get().removeVisitor(this);
        loc.addVisitor(this);
        if (loc.getVisitors().contains(this))
            location = new BuildableRef<>(loc, city);
    }

    public void runSimulationTick(int tick) {
        runStateUpdate(tick);
        updateHealth();
        switch (state) {
            case CitizenState.Residing r -> {
                if (home.get() != null)
                    setLocation(getNextLocationToGetTo(home.get()));
            }
            case CitizenState.Working w -> {
                if (work.get() != null)
                    setLocation(getNextLocationToGetTo(work.get()));
            }
            case CitizenState.Shopping(BuildableRef<CommercialBuilding> shop) -> {
                setLocation(getNextLocationToGetTo(shop.get()));
            }
        }
    }

    private void updateHealth() {
        if (location.get() instanceof PublicTransportation road) {
            currentHealth -= (int) Math.ceilDiv(road.computeNoiseLevel(), 5);
        } else {
            currentHealth += (int) Math.ceilDiv(100 - currentHealth, 10);
        }
        currentHealth = Math.clamp(currentHealth, 0, 100);
        if (location.get() == null)
            return;
        Point locationPoint = city.grid.getBuildingOrigin(location.get());
        int healthCoverage = PublicServiceBuilding.getFieldFunctionForPublicServiceType(city, HealthcareBuilding.class)
                .apply(locationPoint).intValue();
        currentHealth += (int) Math.ceilDiv(Math.max(currentHealth, healthCoverage) - currentHealth, 4);
        currentHealth = Math.clamp(currentHealth, 0, 100);
    }

    private Buildable getNextLocationToGetTo(Buildable target) {
        if (target == null)
            return null;
        return city.grid.getNextStepFromTo(location.get(), target);
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
                        state = new CitizenState.Shopping(new BuildableRef<>(commercialBuilding, city));
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
        return location.get();
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
                location.get() == null ? 100 : (int) (Math.pow((100 - location.get().getCrimeRate()) / 100.0, 2) * 100)
        };

        return List.of(satisfactionContributors).stream().reduce(0, Integer::sum) / satisfactionContributors.length;
    }

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
            case CitizenState.Residing r ->
                home == null ? "Homeless" : home.get() == location.get() ? "Residing" : "Going home";
            case CitizenState.Working w -> work.get() == location.get() ? "Working" : "Going to work";
            case CitizenState.Shopping(BuildableRef<CommercialBuilding> shop) ->
                shop.get() == location.get() ? "Shopping" : "Going to shop";
        };
    }

    public String getCurrentThoughts() {
        Supplier<Boolean> coinFlip = () -> new Random(currentThoughtsSeed).nextDouble(0, 1) > 0.5;
        if (work.get() == null && coinFlip.get())
            return "If only I could have a job...";
        if (home.get() == null && coinFlip.get())
            return "I wish I had a nice apartment...";
        if (location.get().getCrimeRate() > 75 && coinFlip.get())
            return "It is dangerous to be here!";
        if (location.get().getCrimeRate() > 25 && coinFlip.get())
            return "Would be nice to have more police around...";
        if (currentHealth > 90 && coinFlip.get())
            return "I feel rejuvenated!";
        if (currentHealth < 40 && coinFlip.get())
            return "Couldn't last much longer with such health...";
        if (location.get() instanceof PublicTransportation road && road.getCongestion() > 50)
            return "The traffic is terrible...";
        if (location.get() instanceof PublicTransportation road && road.computeNoiseLevel() > 50)
            return "How much hum I could stand?!";
        if (location.get() instanceof PublicTransportation road && road.computeNoiseLevel() < 5)
            return "Silent bliss...";
        if (location.get() instanceof PublicTransportation road && road.getCongestion() < 5)
            return "So nice when no one is driving around you!";
        return "Nothing in particular";
    }
}
