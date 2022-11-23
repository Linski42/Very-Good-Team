package AntStrategy;

import battlecode.common.*;

public class build {
    private int idealNumberMiners = 0;
    private int idealNumberLaboratories = 0;
    private int idealNumberSoldiers = 0;
    private int idealNumberSages = 0;
    enum Strategy {
      LEADER,
      FOLLOWER
    }
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
    private 
    public boolean build(RobotController rc){
        int lead = rc.getTeamLeadAmount();
        int gold = rc.getTeamGoldAmount();
        int round = rc.getRoundNum();
        // calculate greatest unit need 

        
        

        return false;
    }

    public boolean buildEarlygame(RobotController rc){
        int round = rc.getRoundNum();
        //Direction to center, oft has resources
        Direction cDir = rc.getLocation().directionTo(utility.getMapCenter(rc));         
        //start by sending out miners to locations adjacent to center
        if(round < 5){
            
        }

        return false;
    }
    public boolean tryBuild(Direction dir, RobotController rc, RobotType type, Strategy strat) throws GameActionException{
        int lead = rc.getTeamLeadAmount(rc.getTeam());
        int gold = rc.getTeamGoldAmount(rc.getTeam());
        if (lead < 40 && gold < 20)
            { //Can't build anything
                return false;
            }
        MapLocation adjLocation = rc.adjacentLocation(dir);
        if(rc.canBuildRobot(type, dir))
            if(!rc.isLocationOccupied(adjLocation)){
                rc.buildRobot(type, dir);
                RobotInfo newRobot = rc.senseRobotAtLocation(adjLocation);
                //writes shared with strategy for new robot
                rc.writeSharedArray(0, utility.getAvailableLeader(rc).getID());
            }else{
                return false;
            }
        return true;
    }
}
