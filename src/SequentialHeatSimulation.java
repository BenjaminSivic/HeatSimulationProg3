import javax.swing.*;
import java.awt.*;
import java.util.Random;
public class SequentialHeatSimulation {
    // Grid dimensions, when there is no final here the window can sometimes crash when resizing it with the mouse.
    public static final int width=100;
    public static final int height=100;
    // Window dimensions
    public static int WindowWidth=800;
    public static int WindowHeight=600;
    public static final int HeatSRCNumber=8;
    public static final double SourceTemperature=100.0;
    public static final double Threshold=0.01;

    public double[][] grid;
    public double[][] newGrid;
    public boolean stable=false;

    public SequentialHeatSimulation() {
        grid=new double[height][width];
        newGrid=new double[height][width];
        initializeGrid();
    }
    public void initializeGrid() {
        Random random=new Random(17);
        //The grid array is already initialised at 0.0
        // Place random heat sources
        for (int i=0; i<HeatSRCNumber; i++) {
            int x=random.nextInt(width-2)+1; // Can cause visual glitching if the bound is width-1 on certain seeds
            int y=random.nextInt(height-2)+1; // Same as above
            grid[y][x]=SourceTemperature;
        }
    }
    public void updateGrid() {
        stable=true;
        for (int i=1; i<height-1; i++) {
            for (int j=1; j<width-1; j++) {
                if (grid[i][j]!=SourceTemperature) {
                    newGrid[i][j]=(grid[i-1][j]+grid[i+1][j]+grid[i][j-1]+grid[i][j+1])/4.0;
                    if (Math.abs(newGrid[i][j]-grid[i][j])>Threshold) {
                        stable=false;
                    }
                } else {
                    newGrid[i][j]=grid[i][j];
                }
            }
        }
        // Swap grids
        double[][] temp=grid;
        grid=newGrid;
        newGrid=temp;
    }
    public boolean isStable() {
        return stable;
    }
    public double[][] getGrid() {
        return grid;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SequentialHeatSimulation simulation=new SequentialHeatSimulation();
            HeatMapPanel panel=new HeatMapPanel(simulation);

            JFrame frame=new JFrame("Sequential Heat Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(WindowWidth, WindowHeight);
            frame.add(panel);
            frame.setVisible(true);
            // For a smoother simulation set the delay on the timer to 100, it is 10 for testing purposes.
            new Timer(10, e -> {
                simulation.updateGrid();
                panel.repaint();
                if (simulation.isStable()) {
                    ((Timer) e.getSource()).stop();
                    System.out.println("Test done");
                }
            }).start();
        });
    }
}
class HeatMapPanel extends JPanel {
    public final SequentialHeatSimulation simulation;
    public HeatMapPanel(SequentialHeatSimulation simulation) {
        this.simulation=simulation;
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        double[][] grid=simulation.getGrid();
        int cellWidth=getWidth()/grid[0].length;
        int cellHeight=getHeight()/grid.length;

        for (int i=0; i<grid.length; i++) {
            for (int j=0; j<grid[0].length; j++) {
                g.setColor(getColorForTemperature(grid[i][j]));
                g.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
            }
        }
    }
    public Color getColorForTemperature(double temperature) {
        // Map temperature gradient: blue (low) to red (high)
        int red=(int) Math.min(255, temperature/SequentialHeatSimulation.SourceTemperature*255);
        int blue=255-red;
        return new Color(red,0,blue);
    }
}