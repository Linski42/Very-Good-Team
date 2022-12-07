package dijkstra;

import battlecode.common.*;

public class AstarPath implements Path{
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
    public static Direction goTo(RobotController rc, MapLocation pos){
        Direction out = rc.getLocation().directionTo(pos);
        if(rc.canMove(out)){

        }
        Direction straightPath = rc.getLocation().directionTo(pos);

        return out;
    }
    public static Double squareValue(RobotController rc, MapLocation target, MapLocation current){
        /*
        pass end target into this along with current square to consider, recursively calculate
        ideal path by looking at squares next to the square we're trying to calculate
         */
        int rubbleV = rc.senseRubble(current) + (0.5 * squareValue(rc, target, current.add(current.directionTo(target))));
        

    }
}
