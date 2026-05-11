package farm.core;

public class WheatCrop extends GrainCrop {
    private int ticksToGrow;
    private int tickCounter;

    private static final int TICKS_PER_STAGE = 1;

    public WheatCrop() {
        super("Wheat");
        this.ticksToGrow = TICKS_PER_STAGE;
        this.tickCounter = 0;
    }

    public void grow() {
        tickCounter++;
        if (tickCounter >= ticksToGrow) {
            super.grow();
            tickCounter = 0;
        }
    }

    public static int getTicksPerStage() {
        return TICKS_PER_STAGE;
    }
    public String getCropKey() {
        return "wheat";
    }
}
