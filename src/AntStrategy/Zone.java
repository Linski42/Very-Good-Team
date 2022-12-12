package AntStrategy;

import java.util.Map;

import battlecode.common.*;

public class Zone {
    private RobotController rc;
    private MapLocation enemyLoc, centerLoc;
    private MapLocation ontw, twth, thon; //Vectors for (one, two), (two, three), (three, one)
    /**
     * Relative zones around centerpoint scaled for target enemy, so that we can cycle sages from z1 to z2 to z3
     * @param rc
     * @param enemyLoc
     * @param centerLoc
     */
    public Zone(RobotController rc, MapLocation enemyLoc, MapLocation centerLoc, int enemyVision, int unitSize){
        this.rc = rc;
        this.enemyLoc = enemyLoc;
        this.centerLoc = centerLoc;
        Direction dir = centerLoc.directionTo(enemyLoc);
        Direction ontwDir = Utility.plus30(dir);
        Direction twthDir = dir.opposite();
        Direction thonDir = Utility.minus30(dir);
        ontw = centerLoc.add(ontwDir);
        twth = centerLoc.add(twthDir);
        thon = centerLoc.add(thonDir);
        for(int i = 0; i < unitSize*3; i++){  //TODO: Make more effective unitSize zone scale algorithm and make it based on enemy vis
            ontw = ontw.add(ontwDir);
            twth = twth.add(twthDir);
            thon = thon.add(thonDir);
        }
        rc.setIndicatorLine(ontw, centerLoc, 255, 0, 0); //TODO: Remove these
        rc.setIndicatorLine(twth, centerLoc, 0, 255, 0);
        rc.setIndicatorLine(thon, centerLoc, 0, 0, 255);
        
    }

    /*
 *                        @@@
 *                        @@@
 *                        @@@
 *                        @@@
 *                        @@@
 *                        @@@
 *             zone 3     @@@   zone 2
 *                        @@@
 *                        @@@
 *                       @@@@@
 *                    @@@@@ @@@
 *                  @@@@@    @@@@
 *                 @@@@       @@@@
 *               @@@@@          @@@
 *              @@@@             @@@
 *            @@@@    zone 1      @@@
 *           @@@                   @@@@
 *          @@@                     @@@@@
 *        @@@                         @@@@@
 *       @@                             @@@
 *      @@                               @@@
 *   @@@@                                 @@@
 *  @@@                                   @@@
 * 
 */
    /**
     * Gets a location in one of the zones detailed above, used to facilitate rotation.
     */
    public MapLocation getLocationInZone(int zone) throws GameActionException{
        MapLocation m1, m2;
        switch(zone){
            case 2: m1 = twth; m2 = ontw; break;
            case 3: m1 = thon; m2 = twth; break;
            default: m1 = ontw; m2 = thon;
        }
        MapLocation centroid = new MapLocation((m1.x+m2.x+centerLoc.x)/3, (m1.y+m2.y+centerLoc.y)/3);

        if(rc.canSenseLocation(centroid)){
            while(rc.canSenseRobotAtLocation(centroid)){
                centroid = centroid.add(centroid.directionTo(centerLoc)); //TODO: This is shitty
            }
        }
        return centroid; //TODO: Pathing
    }
    /**
     * returns a zone number based on some location, returns zone 3 by default.
     * @param robotLoc
     * @return Zone
     */
    public int getZone(MapLocation loc){//TODO: Make bytecode efficient
        MapLocation[] arr = {thon, ontw, twth, thon};
        int min = 500;
        int zone = -1;
        for(int i = 0; i < arr.length-1; i++){
            MapLocation centroid = new MapLocation((arr[i].x+arr[i+1].x+centerLoc.x)/3, (arr[i].y+arr[i+1].y+centerLoc.y)/3);
            int l = loc.distanceSquaredTo(centroid);
            if(l < min){
                min = l;
                zone = i;
            }
        }
        return zone;
    }
    public static MapLocation calculateCenter(MapLocation robotLocation, RobotInfo[] rInfo, RobotInfo target){//TODO: This just generally needs improvement
        /*
         make it so that when calculating this we switch target based on what position we can get into that's far from enemies
         */
        final int dist = (int) Math.sqrt(robotLocation.distanceSquaredTo(target.getLocation()));
        Direction d = target.getLocation().directionTo(robotLocation);
        MapLocation out = robotLocation;
        for (int i = dist; i <= 8; i++) {//TODO: I actually need to do some non-trivial math to calculate what this point should be oh no
            out = out.add(d);
        }
        return out;
        //for(int i = dist; i<=34; i++){
            //centerLoc.subtract(dir);
        //}
    }
}
