package AntStrategy;
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
}
