package AntStrategy.Archon;

import java.util.LinkedList;

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
        Direction nD = dir;
        MapLocation adjLocation = rc.adjacentLocation(dir);
        while(rc.isLocationOccupied(adjLocation)){
            nD.rotateLeft();
            adjLocation = rc.adjacentLocation(nD);
        }

        if(rc.canBuildRobot(type, dir)){
            rc.buildRobot(type, dir);
            newRobot = rc.senseRobotAtLocation(adjLocation);
        } else{
            return null;
        }
        return newRobot;
    }
    public final int requestsChannel = 0;
    public final static int[] unitPos = new int[]{30, 45, 60}; //leader of each u
    public static RobotInfo buildSage(RobotController rc, Direction directionTo) throws GameActionException {
        //34 - 44
        int targetUnit = 1;
        int[] units = new int[3];
        for (int i = 0; i < unitPos.length; i++) {
        try{
            units[i] = rc.readSharedArray(unitPos[i]);
        }catch(GameActionException z){
            units[i] = 0;
        }
        }
        boolean moreThanFive = false;
        int mostUnits = 0;
        int leastUnits = 0;
        int unitWithLU = 0;
        int unitWithMU = 0;
        for (int i = 0; i < units.length; i++) {
            if(units[i]< 6){
                moreThanFive = false;
            }
            if(units[i]>mostUnits && (units[i] < 10 || i == 3)){
                mostUnits = units[i];
                unitWithMU = i;
            }
            if(units[i] < leastUnits && units[i] > 0){
                leastUnits = units[i];
                unitWithLU = i;
            }
        }
            if(moreThanFive){
                targetUnit = unitWithMU+1;
            }else{
                targetUnit = unitWithLU+1;
            }

            final int UAP = 15 + (targetUnit*15); //unit array position
            int j = UAP + 3;
            int val = -1;
              //I'm pretty sure it isn't full be default? RIght? //TODO: Make sure no overflow
            while (val != -1) {//finds an open index
                j++;
                try{
                    val = rc.readSharedArray(j);
                }catch(GameActionException e){
                    val = -1;
                }
            }
            
            RobotInfo ri = tryBuild(rc, directionTo, RobotType.SAGE);

            if(ri != null){
                rc.writeSharedArray(j, ri.ID);
                rc.writeSharedArray(UAP, rc.readSharedArray(UAP)+1);
            }else{
                rc.setIndicatorString("not creating Sage");
            }
            
            
        /*
         * should check for units that need more sages
         * then set one of the sharedarray slots to the robot id of this new robot
         * and create new unit info if unit is missing
         * change index slot 0 to a requests channel 

         * all should have more than 5 units
         * if not then pick whichever one has the most but not maximum
         * write into shared
         */
        return null;       
    }

    
    public static int getIdealNumMiners(RobotController rc){
        int x = (rc.getMapHeight() + rc.getMapWidth() + (rc.getRoundNum() / 10)) / 10;//maybe tweak to 15?
        //every 100 turns we want one more miner
        //if map is 60 x 60 then we'd get a total of  12 miners:)
        return x;
    }

    public static int getIdealNumLabs(RobotController rc) {
        final int mapSize = (rc.getMapHeight() + rc.getMapWidth())/2;
        final int leadAmount = rc.getTeamLeadAmount(rc.getTeam());
        //I think this works, if map is like 60 x 60 then we'd get 60 as our map size which feels pretty reasonable

        return ((mapSize + (rc.getRoundNum()/20)+(leadAmount/300)) / 20);
        //20 units map size = 1 more lab
        //every 200 turns we want one more lab
        //if our reserve surpasses 300 then we're clearly not using it effectively
    }
    public static int getIdealNumSoldiers(RobotController rc) {
        int x =(rc.getMapHeight() + rc.getMapWidth()) / 10;
        return x;
    }
    public static int getIdealNumBuilders(RobotController rc) {
        return 1 + (getIdealNumLabs(rc)/2);
    }
}
