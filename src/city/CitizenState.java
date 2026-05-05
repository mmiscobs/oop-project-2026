package city;

import buildings.privatebuilding.workplace.commercial.CommercialBuilding;

public sealed interface CitizenState permits CitizenState.Residing, CitizenState.Working, CitizenState.Shopping {
    record Residing() implements CitizenState {
    };

    record Working() implements CitizenState {
    };

    record Shopping(CommercialBuilding shop) implements CitizenState {
    };
}
