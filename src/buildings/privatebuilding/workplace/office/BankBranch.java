package buildings.privatebuilding.workplace.office;

import buildings.Buildable;

import city.City;
import utils.SerializedBlob;

public class BankBranch extends OfficeBuilding {
    public BankBranch() {
        super();
    }

    protected BankBranch(SerializedBlob blob, City city) {
        super(blob, city);
    }

    static {
        Buildable.registry.put(BankBranch.class, BankBranch::new);
        Buildable.blobRegistry.put(BankBranch.class, BankBranch::new);
    }

    public int getPrice() {
        return 1000;
    }

    public int getWorkersCapacity() {
        return 30;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getLength() {
        return 1;
    }
}
