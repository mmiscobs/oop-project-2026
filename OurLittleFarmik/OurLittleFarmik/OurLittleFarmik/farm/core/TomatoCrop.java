package farm.core;

public class TomatoCrop extends VegetableCrop {
    private static final int TICKS_PER_STAGE = 1;
    private static final int TOMATO_YIELD = 200;
    private int tickCounter;

    public TomatoCrop() {
        super("Tomato");
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
        return this.appropriateEventYield(TOMATO_YIELD);
    }

    public static int getTicksPerStage() {
        return TICKS_PER_STAGE;
    }
    public String getCropKey() {
        return "tomato";
    }
}
