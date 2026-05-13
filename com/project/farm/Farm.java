import java.util.ArrayList;

import com.project.buildings.privatebuilding.workplace.WorkplaceBuilding;

public class Farm extends WorkplaceBuilding {
    private FarmPlot[] plots;
    private IrrigationSystem irrigation;
    private ArrayList<FarmEvent> farmEvents;

    public Farm(int numberOfPlots) {
        this.plots = new FarmPlot[numberOfPlots];
        for (int i = 0; i < plots.length; i++) {
            this.plots[i] = new FarmPlot();
        }
        this.irrigation = new IrrigationSystem(this.plots);
        this.farmEvents = new ArrayList<>();
    }

    public void addPlot(FarmPlot p) {
        FarmPlot[] temp = new FarmPlot[this.plots.length + 1];
        for (int i = 0; i < this.plots.length; i++) {
            temp[i] = this.plots[i];
        }
        temp[temp.length - 1] = p;
        this.plots = temp;
    }

    public void tick() {
        this.irrigation.irrigateAll();
        for (int i = 0; i < plots.length; i++) {
            plots[i].tick();
        }
    }

    public void activeEvents() {
        for (int i = 0; i < this.plots.length; i++) {
            if (this.plots[i].getEvent() != null) {
                this.farmEvents.add(this.plots[i].getEvent());
            }
        }
    }

    public ArrayList<FarmEvent> getActiveEvents() {
        return new ArrayList<FarmEvent>(this.farmEvents);
    }

    @Override
    public int getWorkersCapacity() {
        return 10;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public int getPrice() {
        return 10000;
    }
}
