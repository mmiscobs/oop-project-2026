package city;

import java.util.Map;

import buildings.Buildable.BuildableRef;
import buildings.privatebuilding.workplace.commercial.CommercialBuilding;
import utils.SerializedBlob;

public sealed interface CitizenState permits CitizenState.Residing, CitizenState.Working, CitizenState.Shopping {
    record Residing() implements CitizenState {
    };

    record Working() implements CitizenState {
    };

    record Shopping(BuildableRef<CommercialBuilding> shop) implements CitizenState {
        public SerializedBlob toBlob() {
            return SerializedBlob
                    .fromMap(Map.of("type", SerializedBlob.string(getClass().getSimpleName()), "shop", shop.toBlob()));
        }
    };

    public static CitizenState fromBlob(SerializedBlob blob, City city) {
        return switch (blob.map().get("type").string()) {
            case "Residing" -> new Residing();
            case "Working" -> new Working();
            case "Shopping" -> new Shopping(new BuildableRef<>(blob.map().get("shop"), city));
            default -> null;
        };
    }

    public default SerializedBlob toBlob() {
        return SerializedBlob.fromMap(Map.of("type", SerializedBlob.string(getClass().getSimpleName())));
    }
}
