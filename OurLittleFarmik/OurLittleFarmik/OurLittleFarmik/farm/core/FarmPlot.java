package farm.core;

public class FarmPlot {
    private Crop crop;
    private int soilQuality;
    private boolean isIrrigated;
    private FarmEvent event;
    private int daysSinceMature;
    private boolean cropJustVanished;

    public FarmPlot(){
        this.crop = null;
        this.soilQuality = 100;
        this.isIrrigated = false;
        this.event = null;
        this.daysSinceMature = 0;
        this.cropJustVanished = false;
    }

    public void plant(Crop crop){
        this.crop = crop;
        this.daysSinceMature = 0;
        this.cropJustVanished = false;
    }

    public void tick(){
        this.cropJustVanished = false;
        if (this.crop == null){
            return;
        }

        if (this.crop.getStage() == GrowthStage.DEAD){
            this.crop = null;
            this.daysSinceMature = 0;
            this.cropJustVanished = true;
            isIrrigated = false;
            return;
        }

        if (!isIrrigated){
            soilQuality = Math.max(0, soilQuality-3);
        }
        if (soilQuality > 0){
            if (this.event != null){
                soilQuality = Math.max(0, soilQuality-2);
            }
            this.crop.grow();
        }
        isIrrigated = false;

        if (this.crop.getStage() == GrowthStage.MATURE){
            daysSinceMature++;
            if (daysSinceMature >= 7){
                this.crop.setStage(GrowthStage.DEAD);
                this.daysSinceMature = 0;
            }
        } else {
            daysSinceMature = 0;
        }
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


    public Crop getCrop(){
        return this.crop;
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

    public boolean didCropJustVanish(){
        return this.cropJustVanished;
    }
}
