package city;

import buildings.Buildable.BuildableRef;
import buildings.privatebuilding.workplace.commercial.CommercialBuilding;
import utils.SerializedBlob;

public sealed interface CitizenState permits CitizenState.Residing, CitizenState.Working, CitizenState.Shopping {
    record Residing() implements CitizenState {
    };

    record Working() implements CitizenState {
    };

    record Shopping(BuildableRef<CommercialBuilding> shop) implements CitizenState {

    };

    public static CitizenState fromBlob(SerializedBlob blob, City city) {
        return switch (blob.map().get("type").string()) {
            case "Residing" -> new Residing();
            case "Working" -> new Working();
            case "Shopping" -> new Shopping(new BuildableRef<>(blob, city));
            default -> null;
        };
    }
}
