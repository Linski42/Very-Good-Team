package AntStrategy;

import battlecode.common.*;

import java.util.LinkedList;
import java.util.Map;
import java.util.Random;


import dijkstra.*;
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
            Dijkstra20 dijik = new Dijkstra20(rc);
            try {
                switch (rc.getType()) {
                    case ARCHON:     runArchon(rc, dijik);  break;
                    case MINER:      runMiner(rc, dijik);   break;
                    case SOLDIER:    runSoldier(rc, dijik); break;
                    case LABORATORY: runLaboratory(rc); break;
                    case WATCHTOWER: runWatchtower(rc); break;
                    case BUILDER:    runBuilder(rc, dijik); break;
                    case SAGE:       runSage(rc, dijik); break;
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


    private static void runSage(RobotController rc, Dijkstra dijik) throws GameActionException {
        //init{ //we only want this to be the case on first run
        final int[] idIndexN = Utility.getSageIndex(rc); //gives an index for robot to reference //TODO: find some way to cache this
        final int idIndex = idIndexN[0];
        final int unit = idIndexN[1];
        if(idIndex == -1){
           RobotInfo[] rInfo = rc.senseNearbyRobots(); //TODO: Possible Optimization
           for (int i = 0; i < rInfo.length; i++) {
                if(rInfo[i].getTeam() != rc.getTeam() && rc.canAttack(rInfo[i].getLocation())){
                    rc.attack(rInfo[i].getLocation());
                }
           }
        }
        final int unitCount = rc.readSharedArray(15 + (unit * 15)); 
        final MapLocation thisLoc = rc.getLocation();
        int[] eLS = new int[2];  //2 layers of deserialization
        try{
            eLS = Utility.deserializeRobotLocation(rc.readSharedArray(16+(unit*15)));
        }catch(GameActionException e){

        }
        int targetID = 0;
        try{
            targetID = rc.readSharedArray(17+(unit*15));
        }catch(GameActionException e){

        }
        int[] centerDe = new int[]{rc.getMapWidth()/2, rc.getMapHeight()/2};
        try{
            centerDe = Utility.deserializeMapLocation(rc.readSharedArray(18+(unit * 15)));
        }catch(GameActionException e){ }

        final MapLocation targetLoc = new MapLocation(eLS[0], eLS[1]);
        final RobotType targetType = Utility.robotTypeIntValue(eLS[3]);
        final int targetVision = Utility.getActionRadiusSquared(targetType, 0);
        final MapLocation centerLoc = new MapLocation(centerDe[0], centerDe[1]); //location of center for Zone creation

        Zone zone = new Zone(rc, targetLoc, centerLoc, targetVision, unitCount);

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
                    int sPos = 15 + (15*unit); //TODO: Make sure all of this works
                    rc.writeSharedArray(sPos+1, Utility.serializeRobotLocation(ri));
                    rc.writeSharedArray(sPos+2, ri.getID());
                    MapLocation newCent = Zone.calculateCenter(thisLoc, rInfo, targetInfo);
                    int rub = 0;
                    if(rc.canSenseLocation(newCent)){
                        rub = rc.senseRubble(newCent);   
                    }
                        rc.writeSharedArray(sPos+3, Utility.serializeMapLocation(newCent, rub));
                    }
                //TODO: Implement Sage Casting
               desiredPos = zone.getLocationInZone(2);
            }else { //recreate zones
                final RobotInfo ti = rc.senseRobot(targetID);
                rc.attack(ti.getLocation()); 
                RobotInfo[] rInfo = rc.senseNearbyRobots();
                int minHP = 500;
                RobotInfo info = null;
                zone = new Zone(rc, ti.getLocation(), thisLoc, 6, unitCount);
                for(int i = 0; i < rInfo.length; i++){
                    if(rInfo[i].getHealth() < minHP){
                        minHP = rInfo[i].getHealth();
                        info = rInfo[i];
                    }
                }
                MapLocation newCent = Zone.calculateCenter(thisLoc, rInfo, ti);
                rc.writeSharedArray(18+(15*unit),Utility.serializeMapLocation(newCent, 0));
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
        if(rc.canMove(thisLoc.directionTo(desiredPos))){
            rc.move(thisLoc.directionTo(desiredPos));
        }else{
            rc.move(dijik.getBestDirection(desiredPos, thisLoc.directionTo(desiredPos)));
        }
    }


    /**
     * Run a single turn for an Archon.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
 // Build Strategy
    static void runArchon(RobotController rc, Dijkstra dijik) throws GameActionException {
        final int ideal_miner_number = build.getIdealNumMiners(rc);
        final int ideal_builder_number = build.getIdealNumBuilders(rc);
        final int ideal_soldier_number = build.getIdealNumSoldiers(rc);
        final MapLocation myLocation = rc.getLocation();
        final int currentMinerNumber = rc.readSharedArray(3);
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
            ri = build.buildSage(rc, myLocation.directionTo(mapCenter));
            //buildSage
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

    static void runMiner(RobotController rc, Dijkstra dijik) throws GameActionException {//TODO: Teach some miners to go to center map at the beginning of game
        if(rc.getHealth() <= 15){
            rc.writeSharedArray(3, rc.readSharedArray(3)-1);
        }
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

        if(!myLocation.isAdjacentTo(nearestResource)){

            if(rc.canMove(dir)){
                rc.move(dir);
            }else{
                dir = dijik.getBestDirection(nearestResource, dir);
                rc.move(dir);
            }
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
                dir = rc.canMove(dir) ? dir : dijik.getBestDirection(leadPos, dir);
            }
        }else{
          dir = myLocation.directionTo(Utility.getMapCenter(rc));
          dir = rc.canMove(dir) ? dir : dijik.getBestDirection(Utility.getMapCenter(rc), dir);
        }
            if(rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }

    static void runSoldier(RobotController rc, Dijkstra dijik) throws GameActionException { //TODO: rewrite sage code for soldier
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
    private static void runBuilder(RobotController rc, Dijkstra dijik) throws GameActionException { //builders should update economic conditions in shared
        int labCount = 0; 
        try{
            labCount = rc.readSharedArray(2);
        }catch(GameActionException e) {
            e.printStackTrace();
        }
        final MapLocation myLocation = rc.getLocation();
        final int ideal_lab_number = build.getIdealNumLabs(rc);
        final int currentLabNumber = rc.readSharedArray(2);
        if(ideal_lab_number > labCount){
            MapLocation mapCenter = Utility.getMapCenter(rc);             
            Direction d = rc.getLocation().directionTo(mapCenter).opposite(); //TODO: Maybe get a better position
            final RobotInfo[] nearbyList = rc.senseNearbyRobots();
            RobotInfo t = null;
            LinkedList<MapLocation> blockedLocations = new LinkedList<MapLocation>();
            for (int i = 0; i < nearbyList.length; i++) {
                if(nearbyList[i].getType() == RobotType.LABORATORY && nearbyList[i].getHealth() < 100){
                    if(rc.canRepair(nearbyList[i].getLocation())){
                        t = nearbyList[i];
                    }
                }
                if(nearbyList[i].getLocation().isAdjacentTo(myLocation)){
                    blockedLocations.add(nearbyList[i].getLocation());
                }
            }
            if(t != null){
                while(rc.canRepair(t.getLocation())){
                    rc.repair(t.getLocation());
                }
            }

            if(blockedLocations.size() > 4){
                rc.move(dijik.getBestDirection(new MapLocation(0, 0), myLocation.directionTo(blockedLocations.get(0))));
            }

            if(!rc.canMove(d)){
                if(!rc.onTheMap(rc.adjacentLocation(d))){
                rc.setIndicatorString("cannot move in direction");
                if(labCount < ideal_lab_number){
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
            while(rc.canTransmute() && rc.isActionReady()){
                rc.transmute();
                rc.setIndicatorString(String.valueOf(rc.getTransmutationRate()));
        }
    }
}
