package simulation;

public enum GameSpeed {
    Stopped(Integer.MAX_VALUE),
    Slow(1000),
    Normal(500),
    Fast(200);

    public final int msBetweenTicks;

    GameSpeed(int msBetweenTicks) {
        this.msBetweenTicks = msBetweenTicks;
    }
}
