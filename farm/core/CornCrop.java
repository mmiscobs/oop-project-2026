package farm.core;

public class CornCrop extends GrainCrop {
    private static final int TICKS_PER_STAGE = 2;
    private static final int CORN_YIELD = 400;
    private int tickCounter;

    public CornCrop() {
        super("Corn");
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
        return this.appropriateEventYield(CORN_YIELD);
    }

    public static int getTicksPerStage() {
        return TICKS_PER_STAGE;
    }
    public String getCropKey() {
        return "corn";
    }
}
