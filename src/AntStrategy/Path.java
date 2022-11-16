package AntStrategy;
import java.util.Random;

import battlecode.common.*;

public class Path {
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
    static final Random rng = new Random(501051);
    

    public static Direction follow(RobotController rc, int followingID){
        try{
            MapLocation pos = rc.senseRobot(followingID).getLocation();
            return rc.getLocation().directionTo(pos);
        }catch(GameActionException e){}

        RobotInfo ri[] = rc.senseNearbyRobots(20, Team.A);
            if(ri.length  >= 1){

            double n = 21.0;
            RobotInfo closest = null;
            for(int i = 0; i < ri.length; i++){
                double dist = Math.sqrt((ri[i].getLocation().x*ri[i].getLocation().x) + (ri[i].getLocation().y*ri[i].getLocation().y));
                if(dist < n){
                    n = dist;
                    closest = ri[i];
                }
                int id = closest.getID();
                return rc.getLocation().directionTo(closest.getLocation());
            }
            }
        return directions[rng.nextInt(directions.length)];
    }
    /*
     * transforms x and y coods to a cardinal direction
     * so like sign()
     */
    public void greedyPath(){

    }
}
