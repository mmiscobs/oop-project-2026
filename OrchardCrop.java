public class OrchardCrop extends Crop{
    private final static int DEFAULT_YIELD = 50;
    public OrchardCrop(String name, int marketPrice){
        super(name, marketPrice, "orchard");
    }
    public OrchardCrop(String name){
        super(name, "orchard");
    }

    public int getYield() {
        return this.appropriateEventYield(DEFAULT_YIELD); 
    }

    public String getType(){return "orchard";}
    
}
