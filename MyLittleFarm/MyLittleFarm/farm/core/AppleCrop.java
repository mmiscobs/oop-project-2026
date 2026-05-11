package farm.core;

public class AppleCrop extends OrchardCrop {
    private static final int TICKS_PER_STAGE = 3;
    private static final int APPLE_YIELD = 60;
    private int tickCounter;

    public AppleCrop() {
        super("Apple");
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
        return this.appropriateEventYield(APPLE_YIELD);
    }

    public static int getTicksPerStage() {
        return TICKS_PER_STAGE;
    }
    public String getCropKey() {
        return "apple";
    }
}
