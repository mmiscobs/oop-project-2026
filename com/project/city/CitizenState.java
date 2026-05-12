package com.project.city;

import java.util.Map;

import com.project.buildings.Buildable.BuildableRef;
import com.project.buildings.privatebuilding.workplace.commercial.CommercialBuilding;
import com.project.utils.SerializedBlob;

public sealed interface CitizenState permits CitizenState.Residing, CitizenState.Working, CitizenState.Shopping {
    record Residing() implements CitizenState {
    };

    record Working() implements CitizenState {
    };

    record Shopping(BuildableRef<CommercialBuilding> shop) implements CitizenState {
        public SerializedBlob toBlob(SerializedBlob.Factory Factory) {
            return Factory
                    .fromMap(Map.of("type", Factory.string(getClass().getSimpleName()), "shop", shop.toBlob(Factory)));
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

    public default SerializedBlob toBlob(SerializedBlob.Factory Factory) {
        return Factory.fromMap(Map.of("type", Factory.string(getClass().getSimpleName())));
    }
}
