package AntStrategy;
import java.lang.reflect.Array;
import java.util.ArrayList;

import battlecode.common.*;

public class utility {

    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };
    
    public static MapLocation getMapCenter(RobotController rc){
        return new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
    }
    /**
     * calculates probability of enemy appearing on a given square, takes in robotInfo array to save byteCode.
     * @param rc
     * @param loc
     * @param inView 
     * @return
     */
    public static Double enemyProbability(RobotController rc, MapLocation loc, RobotInfo[] inView){
        Double P = 0.0; //assumption
        for(int i = 0; i < inView.length; i++){
        }
    }

    /**
     * gets direction adjacent to direction minus 30 degrees
     * @param Direction
     * @return
     */
    public static Direction minus30(Direction dir){
        for(int i = 0; i < directions.length; i++){
            if(directions[i] == dir){
                if (i == 0){
                    return directions[directions.length-1];
                }
                return directions[i-1];
            }
        }
        return dir;
    }
    /**
     * gets direction adjacent to direction plus 30 degrees
     * @param Direction
     * @return
     */
    public static Direction plus30(Direction dir){
        for(int i = 0; i < directions.length; i++){
            if(directions[i] == dir){
                if (i == directions.length-1){
                    return directions[0];
                }
                return directions[i+1];
            }
        }
        return dir;
    }
    public static String[] deserializeRobotLocation(int serial){ //TODO: Is placeholder
        String[] out = new String[3];
        int x = 0;
        int y = 0;
        out[0] = (Integer.toString(x));
        out[1] = (Integer.toString(y));
        out[2] = ("ARCHON");
        return out;
    }
}
