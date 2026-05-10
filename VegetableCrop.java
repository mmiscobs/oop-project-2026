public class VegetableCrop extends Crop{
    private static final int DEFAULT_YIELD = 10;
    public VegetableCrop(String name, int marketPrice){
        super(name, marketPrice, "vegetable");
    }
    public VegetableCrop(String name){
        super(name, "vegetable");
    }

    public int getYield() {
        return this.appropriateEventYield(DEFAULT_YIELD);
    }

    public String getType(){return "vegetable";}
    
}
