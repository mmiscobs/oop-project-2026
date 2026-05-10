public class FarmPlot {
    private Crop crop;
    private int soilQuality;
    private boolean isIrrigated;
    private FarmEvent event;

    public FarmPlot(){
        this.crop = null;
        this.soilQuality = 100;
        this.isIrrigated = false;
        this.event = null;
    }

    public void plant(Crop crop){
        this.crop = crop; 
    }

    public void tick(){
        if (this.crop==null){
            return;
        }
        if (!isIrrigated){
            soilQuality = Math.max(0, soilQuality-3);
        }
        if (soilQuality>0){
            if (this.event!=null){
                soilQuality = Math.max(0, soilQuality-2);
            }
            this.crop.grow();
        }
        isIrrigated = false;
    }

    public int getSoilQuality(){
        return this.soilQuality;
    }
    public boolean isIrrigated(){
        return isIrrigated;
    }
    public void setIrrigated(boolean irrigationStatus){
        this.isIrrigated = irrigationStatus;
    }

    public void applyEvent(FarmEvent e){
        this.crop.FarmEventHappened(e);
        this.event = e;
    }

    public int waterAmount(){
        if (this.crop == null){
            return 0;
        }
        if (this.crop.getType().equals("orchard")){
            return 30;
        } else if(this.crop.getType().equals("vegetable")){
            return 20;
        } else if (this.crop.getType().equals("grain")){
            return 12;
        }
        return 15;
    }

    public int harvest(){
        if (crop==null || !this.crop.isReady()){
            return 0;
        }
        int yield = this.crop.getYield();
        this.crop.setStage(GrowthStage.HARVESTED);
        this.soilQuality = Math.max(0, soilQuality-10);
        this.crop = null;
        return yield;
    }

    public void centralBankInterferenceOrBrokeCitizenAlert(){
        this.crop.setStage(GrowthStage.DEAD);
    }

    public FarmEvent getEvent(){
        return this.event;
    }
}
