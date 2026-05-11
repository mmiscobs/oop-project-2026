package farm.core;

public class PotatoCrop extends VegetableCrop {
    private static final int TICKS_PER_STAGE = 2;
    private static final int POTATO_YIELD = 180;
    private int tickCounter;

    public PotatoCrop() {
        super("Potato");
        this.tickCounter = 0;
    }

    public void grow() {
        tickCounter++;
        if (tickCounter >= TICKS_PER_STAGE) {
            super.grow();
            tickCounter = 0;
        }
    }

    public int getYield() {
        return this.appropriateEventYield(POTATO_YIELD);
    }

    public static int getTicksPerStage() {
        return TICKS_PER_STAGE;
    }
    public String getCropKey() {
        return "potato";
    }
}
