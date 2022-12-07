package AntStrategy;

import java.util.ArrayList;

import battlecode.common.*;

public interface Path {
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
        Direction out = rc.getLocation().directionTo(new MapLocation(0, 0));
        return out;
    }
}
