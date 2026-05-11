package farm.core;

import java.util.Map;

public abstract class Crop {

    private static final Map<String, Integer> DEFAULT_MARKETPRICES_FOR_TYPES = Map.of(
    "orchard", 30,
    "vegetable", 15,
    "grain", 5); 

    private GrowthStage stage;
    private int marketPrice;
    private String name;
    private String cropType;
    private FarmEvent fEvent;

    public Crop(String name, int marketPrice, String cropType){
        this.name = name;
        this.marketPrice = marketPrice;
        this.cropType = cropType;
        this.stage = GrowthStage.SEED;
        this.fEvent = null;
    }

    public Crop(String name, String cropType){
        this.name = name;
        this.cropType = cropType;
        this.marketPrice = DEFAULT_MARKETPRICES_FOR_TYPES.get(cropType);
        this.stage = GrowthStage.SEED;
        this.fEvent = null;
    }

    public String getName(){
        return this.name;
    }
    public GrowthStage getStage(){
        return this.stage;
    }

    public void setStage(GrowthStage gs){
        this.stage = gs;
    }
    
    public void FarmEventHappened(FarmEvent e){
        this.fEvent = e;
    }

    public FarmEvent getFEvent(){
        return this.fEvent;
    }

    public void grow() {
        if (stage == GrowthStage.MATURE) {
            return;
        }

        int state = stage.ordinal();
        this.stage = GrowthStage.values()[state + 1];
    }

    public boolean isReady() {
        return stage == GrowthStage.MATURE;
    }

    public abstract int getYield();
    public abstract String getType();

    public int getMarketprice(){
        return this.marketPrice;
    }

    protected int appropriateEventYield(int usualYield){
        if (this.fEvent == null) return usualYield;
        if (this.fEvent.equals(FarmEvent.FRUITFUL_HARVEST)){
            return usualYield*2;
        }else if (this.fEvent.equals(FarmEvent.DROUGHT)){
            return 0;
        }else if (this.fEvent.equals(FarmEvent.BIRD_ATTACK)){
            return usualYield/2;
        }else if (this.fEvent.equals(FarmEvent.PEST)){
            return (usualYield*3)/4;
        }
        return usualYield;
    }

}
