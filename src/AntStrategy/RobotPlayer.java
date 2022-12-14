package AntStrategy;

import battlecode.common.*;
import dijkstra.Path;

import java.util.Map;
import java.util.Random;

import AntStrategy.Archon.build;

enum Strategy {
  LEADER,
  FOLLOWER
}
public strictfp class RobotPlayer {
    static int turnCount = 0;

    static final Random rng = new Random(6147 + turnCount);
    /** Array containing all the possible movement directions. */
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

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());
        rc.setIndicatorString("Hello world!");
        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!
            System.out.println("Age: " + turnCount + "; Location: " + rc.getLocation());
            try {
                switch (rc.getType()) {
                    case ARCHON:     runArchon(rc);  break;
                    case MINER:      runMiner(rc);   break;
                    case SOLDIER:    runSoldier(rc); break;
                    case LABORATORY: runLaboratory(rc); break;
                    case WATCHTOWER: runWatchtower(rc); break;
                    case BUILDER:    runBuilder(rc); break;
                    case SAGE:       runSage(rc); break;
                }
            } catch (GameActionException e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                Clock.yield();
            }
        }

    }


    private static void runSage(RobotController rc) throws GameActionException {
        //init{ //we only want this to be the case on first run
        final int unit = rc.readSharedArray(0); //gives an index for robot to reference //TODO: find some way to cache this
        //final String[] countAndOrder = Utility.deserializeCountAndOrder(rc.readSharedArray(28+(unit*2)));TODO: Fix
        final int unitCount = 8; //TODO: add in after deser is written
        //}

        final MapLocation thisLoc = rc.getLocation();
        final int[] eLS = Utility.deserializeRobotLocation(rc.readSharedArray(29+(unit*2))); //2 layers of deserialization
        final MapLocation targetLoc = new MapLocation(eLS[0], eLS[1]);
        final RobotType targetType = Utility.robotTypeIntValue(eLS[3]);
        final int targetVision = Utility.getActionRadiusSquared(targetType, 0);
        final int targetID = rc.readSharedArray(30+(unit*2));
        final int[] centerDe = Utility.deserializeMapLocation(rc.readSharedArray(31+(unit * 2)));
        final MapLocation centerLoc = new MapLocation(centerDe[0], centerDe[1]); //location of center for Zone creation

        final Zone zone = new Zone(rc, targetLoc, centerLoc, targetVision, unitCount);

        final int zoneNumber = zone.getZone(thisLoc);
        MapLocation desiredPos = centerLoc;

        if(rc.canSenseRobot(targetID)){
                RobotInfo targetInfo = rc.senseRobot(targetID);

                rc.attack(targetInfo.getLocation()); //TODO: Research, can this do more than one action

            if(zoneNumber == 1) { //in attack zone
                if(!rc.canSenseRobot(targetID)){ //if target down 
                    RobotInfo[] rInfo = rc.senseNearbyRobots(); //TODO: Possible Optimization
                    int minHP = 500;
                    RobotInfo ri = null;
                    for(int i = 0; i < rInfo.length; i++){
                        if(rInfo[i].getHealth() < minHP){
                            minHP = rInfo[i].getHealth();
                            ri = rInfo[i];
                        }
                    }
                    for(int i = 29; i<31; i++){ //update shared array with new targets
                        int v = Utility.serializeRobotLocation(ri);
                        

                        if(i == 30){
                            v = ri.getID();
                        }else if(i == 31){
                            MapLocation newCent = Zone.calculateCenter(thisLoc, rInfo, targetInfo);
                            int rub = 0;
                            if(rc.canSenseLocation(newCent)){
                                rub = rc.senseRubble(newCent);   
                            }
                            v = Utility.serializeMapLocation(newCent, rub);
                        }
                        rc.writeSharedArray(i+(unit*2), v); 
                    }
                }
                //TODO: Implement Sage Casting
               desiredPos = zone.getLocationInZone(2);
            }else { //recreate zones
                    //run away
                    //TODO: I want to play with this to see if it's too fidgety as is        
            }
        }else {
            switch(zoneNumber){ //if I can't find an enemy then either push up or rotate to the next zone
                case 1: desiredPos = targetLoc; break; //TODO: this needs to be a lot more complex, kiting is what wins games
                case 2: desiredPos = zone.getLocationInZone(3); break;
                case 3: desiredPos = zone.getLocationInZone(1); break;//This could be optimized
                default: desiredPos = centerLoc;
            }

        }
        rc.move(thisLoc.directionTo(desiredPos)); //TODO: Implement pathfinding
    }


    /**
     * Run a single turn for an Archon.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
 // Build Strategy
    static void runArchon(RobotController rc) throws GameActionException {
        final int ideal_miner_number = 3*rc.getArchonCount();
        final int ideal_builder_number = rc.getArchonCount();
        final int ideal_soldier_number = 4;
        final int ideal_lab_count = 2;
        final MapLocation myLocation = rc.getLocation();
        final int currentLabCount = rc.readSharedArray(2);
        final int currentMinerNumber = rc.readSharedArray(3);
        final int currentSageCount = rc.readSharedArray(30);
        final int currentBuilderNumber = rc.readSharedArray(4);
        final RobotInfo[] nearby = rc.senseNearbyRobots();
        final MapLocation mapCenter = Utility.getMapCenter(rc);

        int nearbyEnemyCount = 0;
        RobotInfo[] nearbyEnemies = new RobotInfo[nearby.length];
        for (int i = 0; i < nearby.length; i++) {
          if(nearby[i].getTeam() != rc.getTeam()){nearbyEnemyCount++;}
        }
        if(nearbyEnemyCount > 0){
            Direction eDir = myLocation.directionTo(nearbyEnemies[0].getLocation());
            build.tryBuild(rc, eDir, RobotType.SOLDIER);
            if(rc.canMove(eDir.opposite()))
                rc.move(eDir.opposite());
        }
        RobotInfo ri = null;
            final int[] s = Utility.deserializeMapLocation(rc.readSharedArray(8)); 
            final MapLocation leadPos = new MapLocation(s[0], s[1]);
        if (ideal_miner_number > currentMinerNumber) {
            rc.setIndicatorString("Trying to build a miner");
            rc.setIndicatorLine(myLocation, leadPos, 255, 0, 0);
            ri = build.tryBuild(rc, myLocation.directionTo(leadPos), RobotType.MINER);
            rc.writeSharedArray(3, currentMinerNumber + 1);

        }else if(currentBuilderNumber < ideal_builder_number){
            rc.setIndicatorString("Trying to build a builder");
            //TODO: Fix this and make it work for labs instead of builders
            ri = build.tryBuild(rc, myLocation.directionTo(mapCenter).opposite(), RobotType.BUILDER);
            if(ri != null){
                rc.writeSharedArray(4, currentBuilderNumber+ 1);
            }
        }
         else {
            rc.setIndicatorString("Trying to build a sage");
            ri = build.tryBuild(rc, myLocation.directionTo(mapCenter), RobotType.SAGE);
        }

        RobotInfo lowest = nearby[0];
        for (int i = 0; i < nearby.length; i++) {
            if(nearby[i].health<lowest.health){
                lowest = nearby[i];
            }

        }
        if(rc.canRepair(lowest.getLocation())){
            rc.repair(lowest.getLocation());
        }

    }

    static void runMiner(RobotController rc) throws GameActionException {
        final MapLocation myLocation = rc.getLocation();
        final MapLocation[] leadInSight = rc.senseNearbyLocationsWithLead(100);
        final MapLocation[] goldInSight = rc.senseNearbyLocationsWithLead(100);
        if(leadInSight.length > 0 || goldInSight.length > 0){
        final MapLocation[] rList = (goldInSight.length > 0 ? goldInSight : leadInSight);
        MapLocation nearestResource = rList[0];
        for (int i = 0; i < (rList.length); i++) {
            if(myLocation.distanceSquaredTo(rList[i]) < myLocation.distanceSquaredTo(nearestResource))
                nearestResource = rList[i];
        }

        Direction dir = myLocation.directionTo(nearestResource);
        rc.setIndicatorLine(myLocation, nearestResource, 255, 0, 0);
        if(rList.length > 1){
            rc.writeSharedArray(8, Utility.serializeMapLocation(rList[1], 0));//write
        }else{
            rc.writeSharedArray(8, Utility.serializeMapLocation(rList[0], 0));//write
        }

        if(rc.canMove(dir) && !myLocation.isAdjacentTo(nearestResource)){
            rc.move(dir);
        }else if(myLocation.isAdjacentTo(nearestResource)){

            rc.setIndicatorString("mining at: (" + nearestResource.x + ", " + nearestResource.y + ")");
            rc.setIndicatorDot(nearestResource, 255, 255, 0);
            while (rc.canMineGold(nearestResource)) {
                rc.mineGold(nearestResource);
            }
            while (rc.canMineLead(nearestResource)) {
                rc.mineLead(nearestResource);
            }
        }
    }else{
            //TODO: Adjacency matrix to see if piece of lead is full
        int n = 0;
        try{
            n = rc.readSharedArray(8);
        }catch(GameActionException e){
        }

        Direction dir = directions[rng.nextInt(directions.length)];
        if(n != 0){
            final int[] s = Utility.deserializeMapLocation(n); 
            MapLocation leadPos = new MapLocation(s[0], s[1]);
            rc.setIndicatorString("nothing nearby, sharedArray is: " + s[0] + ", "+ s[1]);
            rc.setIndicatorDot(leadPos, 0, 255, 255);

            // if nothing at target position then rewrite the position in the array    
            if(rc.canSenseLocation(leadPos) && !(rc.senseGold(leadPos) > 0 || rc.senseLead(leadPos) > 0)){
                rc.setIndicatorString("nothing at target, sharedarray is: (" + leadPos.x + ", " + leadPos.y + ")" );
                rc.writeSharedArray(8, 0);
            }else{
                dir = myLocation.directionTo(leadPos); //this is new
            }
        }else{
          dir = myLocation.directionTo(Utility.getMapCenter(rc));
        }
            if(rc.canMove(dir)) {
                rc.move(dir);
            }else{
                rc.setIndicatorString("moving randomly");
                rc.move(directions[rng.nextInt(directions.length)]);
            }
        }
        }

    static void runSoldier(RobotController rc) throws GameActionException { //TODO: rewrite sage code for soldier
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }

        // Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("I moved!");
        }
    }
    private static void runBuilder(RobotController rc) throws GameActionException { //builders should update economic conditions in shared
        //String builderOrder = Utility.deserializeCountAndOrder(rc.readSharedArray(10));//TODO: Fix
        final int ideal_lab_count = 3;
        String builderOrder = "BUILD";
        int labCount = 0; 
        try{
            labCount = rc.readSharedArray(2);
        }catch(GameActionException e) {
            e.printStackTrace();
        }
        if(builderOrder == "BUILD"){
            MapLocation mapCenter = Utility.getMapCenter(rc);             
            Direction d = rc.getLocation().directionTo(mapCenter).opposite();
            final RobotInfo[] nearbyList = rc.senseNearbyRobots();
            RobotInfo t = null;
            for (int i = 0; i < nearbyList.length; i++) {
                if(nearbyList[i].getType() == RobotType.LABORATORY && nearbyList[i].getHealth() < 100){
                    if(rc.canRepair(nearbyList[i].getLocation())){
                        t = nearbyList[i];
                    }
                }
            }
            if(t != null){
                while(rc.canRepair(t.getLocation())){
                    rc.repair(t.getLocation());
                }
            }
            if(!rc.canMove(d)){

                if(!rc.onTheMap(rc.adjacentLocation(d))){
                rc.setIndicatorString("cannot move in direction");
                if(labCount < ideal_lab_count){
                    while(!rc.canBuildRobot(RobotType.LABORATORY, d))
                        d = d.rotateLeft();
                    RobotInfo ri = build.tryBuild(rc, d, RobotType.LABORATORY);
                    
                    if(ri != null)
                        rc.writeSharedArray(2, labCount+1);
                }else{
                }
            }
            }else{
                rc.move(d);
            }

        }
     }

    private static void runWatchtower(RobotController rc) {

    }

    private static void runLaboratory(RobotController rc) throws GameActionException {
        if(rc.canTransmute()){//TODO: Add more conditions or read from archon in shared array
            rc.transmute();
        }
    }
}