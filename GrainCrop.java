public class GrainCrop extends Crop{
    private static final int DEFAULT_YIELD = 300;
    public GrainCrop(String name, int marketPrice) {
        super(name, marketPrice, "grain");
    }
    public GrainCrop(String name){
        super(name, "grain");
    }

    public int getYield() {
        return this.appropriateEventYield(DEFAULT_YIELD);
    }

    public String getType(){return "grain";}
}
