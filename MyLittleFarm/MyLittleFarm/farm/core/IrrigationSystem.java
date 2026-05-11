package farm.core;

public class IrrigationSystem {
    private int waterLevel;
    private FarmPlot[] coverage;
    private static final int COST_FOR_REFILL = 50;

    public IrrigationSystem(FarmPlot[] plots){
        this.coverage = plots;
        this.waterLevel = 100;
    }

    public void irrigateAll(){
        for (int i = 0; i<coverage.length;i++){
            int waterAmount = this.coverage[i].waterAmount();
            if (waterLevel<waterAmount){
                if (!refill()){
                    return;
                }
            }
            waterLevel-=waterAmount;
            this.coverage[i].setIrrigated(true);
        }
    }

    public void irrigateOne(FarmPlot p){
        int waterAmount = p.waterAmount();
        if (waterLevel<waterAmount){
            if (!this.refill()){
                return;
            }
        }
        waterLevel -= waterAmount;
        p.setIrrigated(true);
    }

    public boolean refill(){
        this.waterLevel = 100;
        return true;
    }

    public int getWaterLevel(){
        return this.waterLevel;
    }
}
