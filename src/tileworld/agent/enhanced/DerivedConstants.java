/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.agent.enhanced;

import tileworld.Parameters;

/**
 *
 * @author Xu Meng
 */
public class DerivedConstants {
    
    // config 1
//    private final static double TILE_LIFE_TIME_CONSERVATIVENESS = 0.30;
//    private final static double HOLE_LIFE_TIME_CONSERVATIVENESS = 0.30;
//    private final static double OBSTACLE_LIFE_TIME_CONSERVATIVENESS = 0.70;
//    private final static double CONSERVATIVENESS = 0.9975;
//    public static double FUEL_SAFETY_MARGIN = 1.5;        
    
    // config 3
//    private final static double TILE_LIFE_TIME_CONSERVATIVENESS = 0.30;
//    private final static double HOLE_LIFE_TIME_CONSERVATIVENESS = 0.30;
//    private final static double OBSTACLE_LIFE_TIME_CONSERVATIVENESS = 0.70;
//    private final static double CONSERVATIVENESS = 0.9925;
//    public static double FUEL_SAFETY_MARGIN = 1.5;    
    
    //config 2
    private final static double TILE_LIFE_TIME_CONSERVATIVENESS = 0.30;
    private final static double HOLE_LIFE_TIME_CONSERVATIVENESS = 0.30;
    private final static double OBSTACLE_LIFE_TIME_CONSERVATIVENESS = 0.70;
    private final static double CONSERVATIVENESS = 0.925;
    public static double FUEL_SAFETY_MARGIN = 1.5;
    
    public final static int LIFE_TIME = Parameters.lifeTime;
    public final static int EXPECTED_LIFE_TIME_OF_TILE = (int) Math.floor((Parameters.lifeTime * TILE_LIFE_TIME_CONSERVATIVENESS));
    public final static int EXPECTED_LIFE_TIME_OF_HOLE = (int) Math.floor((Parameters.lifeTime * HOLE_LIFE_TIME_CONSERVATIVENESS));
    public final static int EXPECTED_LIFE_TIME_OF_OBSTACLE = (int) Math.floor((Parameters.lifeTime * OBSTACLE_LIFE_TIME_CONSERVATIVENESS));
    public final static int EXPECTED_LIFE_TIME_OF_EMPTY_CELLS = calculateExpectedLifeTimeOfEmptyCells();
    public final static int EXPECTED_NUM_OF_EMPTY_CELLS_WITHOUT_OBJECT = calculateNumOfEmptyCellsWithoutObject();
    public static final int AVERAGE_STEP_TO_TILE = (int)(-1 + Math.sqrt(2*Math.log(1-CONSERVATIVENESS)/Math.log(1-Parameters.tileMean*Parameters.lifeTime/(Parameters.xDimension*Parameters.yDimension))))/2;
    public static final int AVERAGE_STEP_TO_HOLE = (int)(-1 + Math.sqrt(2*Math.log(1-CONSERVATIVENESS)/Math.log(1-Parameters.holeMean*Parameters.lifeTime/(Parameters.xDimension*Parameters.yDimension))))/2;
    public static final int AVERAGE_STEP_TO_OBSTACLE = (int)(-1 + Math.sqrt(2*Math.log(1-CONSERVATIVENESS)/Math.log(1-Parameters.obstacleMean*Parameters.lifeTime/(Parameters.xDimension*Parameters.yDimension))))/2;  
    
    private static int calculateExpectedLifeTimeOfEmptyCells(){
        double densityOfTile = Parameters.tileMean/(Parameters.xDimension * Parameters.yDimension - Parameters.lifeTime*(Parameters.tileMean+Parameters.holeMean+Parameters.obstacleMean));
        double densityOfHole = Parameters.holeMean/(Parameters.xDimension * Parameters.yDimension - Parameters.lifeTime*(Parameters.tileMean+Parameters.holeMean+Parameters.obstacleMean));
        double densityOfObstacle = Parameters.obstacleMean/(Parameters.xDimension * Parameters.yDimension - Parameters.lifeTime*(Parameters.tileMean+Parameters.holeMean+Parameters.obstacleMean));
        return (int) Math.floor(Math.log(CONSERVATIVENESS) / Math.log((1-densityOfTile) * (1-densityOfHole) * (1-densityOfObstacle)));
    }
    
    private static int calculateNumOfEmptyCellsWithoutObject(){
        double densityOfTile = Parameters.lifeTime * Parameters.tileMean/(Parameters.xDimension * Parameters.yDimension);
        double densityOfHole = Parameters.lifeTime * Parameters.holeMean/(Parameters.xDimension * Parameters.yDimension);
        double densityOfObstacle = Parameters.lifeTime * Parameters.obstacleMean/(Parameters.xDimension * Parameters.yDimension);
        return (int) Math.floor(Math.log(CONSERVATIVENESS) / Math.log((1-densityOfTile) * (1-densityOfHole) * (1-densityOfObstacle)));
    }
    
    public static void printConstants(){
        System.out.println("EXPECTED_LIFE_TIME_OF_TILE="+EXPECTED_LIFE_TIME_OF_TILE);
        System.out.println("EXPECTED_LIFE_TIME_OF_HOLE="+EXPECTED_LIFE_TIME_OF_HOLE);
        System.out.println("EXPECTED_LIFE_TIME_OF_OBSTACLE="+EXPECTED_LIFE_TIME_OF_OBSTACLE);
        System.out.println("EXPECTED_LIFE_TIME_OF_EMPTY_CELLS="+EXPECTED_LIFE_TIME_OF_EMPTY_CELLS);
        System.out.println("EXPECTED_NUM_OF_EMPTY_CELLS_WITHOUT_OBJECT="+EXPECTED_NUM_OF_EMPTY_CELLS_WITHOUT_OBJECT);
        System.out.println("AVERAGE_STEP_TO_TILE="+AVERAGE_STEP_TO_TILE);
        System.out.println("AVERAGE_STEP_TO_HOLE="+AVERAGE_STEP_TO_HOLE);
        System.out.println("AVERAGE_STEP_TO_OBSTACLE="+AVERAGE_STEP_TO_OBSTACLE);
    }
    
}
