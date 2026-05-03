package buildings.privatebuilding;

import buildings.Buildable;

public abstract class PrivateBuilding extends Buildable {
    protected boolean isBuilt;

    public abstract boolean getIsBuilt();
    public abstract int calculateProfitPerTick();
}
