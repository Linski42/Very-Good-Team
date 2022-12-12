package AntStrategy.Archon;

import AntStrategy.Utility;
import battlecode.common.*;

public class build {
    private int idealNumberMiners = 0;
    private int idealNumberLaboratories = 0;
    private int idealNumberSoldiers = 0;
    private int idealNumberSages = 0;
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
    public static  boolean buildEarlygame(RobotController rc){
        int round = rc.getRoundNum();
        //Direction to center, oft has resources
        Direction cDir = rc.getLocation().directionTo(Utility.getMapCenter(rc));         
        //start by sending out miners to locations adjacent to center
        if(round < 5){
            
        }

        return false;
    }
    public static RobotInfo tryBuild(RobotController rc, Direction dir, RobotType type) throws GameActionException{
        int lead = rc.getTeamLeadAmount(rc.getTeam());
        int gold = rc.getTeamGoldAmount(rc.getTeam());
        RobotInfo newRobot = null;
        MapLocation adjLocation = rc.adjacentLocation(dir);
        if(rc.canBuildRobot(type, dir))
            if(!rc.isLocationOccupied(adjLocation)){
                rc.buildRobot(type, dir);
                newRobot = rc.senseRobotAtLocation(adjLocation);
            }else{
                return null;
            }
        return newRobot;
    }
}
