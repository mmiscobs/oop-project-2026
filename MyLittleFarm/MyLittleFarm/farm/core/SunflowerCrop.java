package farm.core;

public class SunflowerCrop extends OrchardCrop {
    private static final int TICKS_PER_STAGE = 1;
    private static final int SUNFLOWER_YIELD = 80;
    private int tickCounter;

    public SunflowerCrop() {
        super("Sunflower");
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
        return this.appropriateEventYield(SUNFLOWER_YIELD);
    }

    public static int getTicksPerStage() {
        return TICKS_PER_STAGE;
    }
    public String getCropKey() {
        return "sunflower";
    }
}
