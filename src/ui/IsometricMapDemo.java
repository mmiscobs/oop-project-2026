package ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import buildings.privatebuilding.residential.Condominium;
import buildings.privatebuilding.residential.SmallHouse;
import buildings.publicbuilding.service.healthcare.Clinic;
import buildings.publicbuilding.service.police.SmallPoliceStation;
import city.City;
import utils.Point;

public class IsometricMapDemo {
    private static final int TILE_W = 34;
    private static final int TILE_H = 16;
    private static final int COLS = 10;
    private static final int ROWS = 10;
    private static final int FRAME_W = TILE_W * (COLS + 3) * 3;
    private static final int FRAME_H = TILE_H * (ROWS * 2) * 3;

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(IsometricMapDemo::showWindow);
    }

    private static void showWindow() {
        City city = new City();
        city.grid.placeBuildingAt(new Point(1, 1), new SmallHouse());
        city.grid.placeBuildingAt(new Point(3, 3), new Condominium());
        city.grid.placeBuildingAt(new Point(5, 5), new Clinic());
        city.grid.placeBuildingAt(new Point(6, 6), new SmallPoliceStation());

        CityView view = new CityView(city);

        view.render();

        JFrame f = new JFrame("City Map Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setContentPane(view);
        f.setSize(FRAME_W, FRAME_H);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
