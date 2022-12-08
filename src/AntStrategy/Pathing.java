package AntStrategy;
import java.util.Random;

import battlecode.common.*;

public class Pathing {
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
    
/**
 * @return followingID
 * @throws GameActionException
 */
    public static int findLeaderAndMove(RobotController rc) throws GameActionException{
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
                //TODO
                rc.move(rc.getLocation().directionTo(closest.getLocation()));
                return closest.getID();
            }
            }
            rc.move(directions[rng.nextInt(directions.length)]);
            return -1;
    }
}
