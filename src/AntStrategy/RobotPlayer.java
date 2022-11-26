package AntStrategy;

import battlecode.common.*;

import java.util.Map;
import java.util.Random;

enum Strategy {
  LEADER,
  FOLLOWER
}
public strictfp class RobotPlayer {
    static int turnCount = 0;

    static final Random rng = new Random(6147);
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
        final String[] countAndOrder = utility.deserializeCountAndOrder(rc.readSharedArray(28+(unit*2)));
        final int unitCount = 8; //TODO: add in after deser is written
        //}

        final MapLocation thisLoc = rc.getLocation();
        final String[] eLS = utility.deserializeRobotLocation(rc.readSharedArray(29+(unit*2))); //2 layers of deserialization
        final MapLocation targetLoc = new MapLocation(Integer.parseInt(eLS[0]), Integer.parseInt(eLS[1]));
        final RobotType targetType = RobotType.valueOf(eLS[3]);
        final int targetVision = targetType.actionRadiusSquared(); //TODO: Not sure why this isn't working
        final int targetID = rc.readSharedArray(30+(unit*2));
        final MapLocation centerLoc = utility.deserializeMapLocation(rc.readSharedArray(31+(unit * 2))); //location of center for Zone creation

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
                    RobotInfo ri;
                    for(int i = 0; i < rInfo.length; i++){
                        if(rInfo[i].getHealth() < minHP){
                            minHP = rInfo[i].getHealth();
                            ri = rInfo[i];
                        }
                    }
                    for(int i = 29; i<31; i++){ //update shared array with new targets
                        int v = utility.serializeRobotLocation(ri);
                        if(i == 30){
                            v = ri.getID();
                        }else if(i == 31){
                            v = utility.serializeMapLocation(Zone.calculateCenter(thisLoc, rInfo, targetInfo));
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
        rc.move(Path.goto(rc, desiredPos)); //TODO: Implement pathfinding
    }


    /**
     * Run a single turn for an Archon.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
 // Build Strategy
    int income = 0;
    int miner_number = 0;
    int soldier_number = 0;
    int idle_miner_number = 0;
    static void runArchon(RobotController rc) throws GameActionException {
        /*
         * we should have some logic for pumping out defenders of archon
         */
        if (rc.getRoundNum() < 50) {
            rc.setIndicatorString("Trying to build a miner");
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                rc.buildRobot(RobotType.MINER, dir);
            }
        } else {
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
            }
        }
    }

    /**
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runMiner(RobotController rc, Strategy strat) throws GameActionException {
        int followID = -1;
        // Try to mine on squares around us.
        for (int dx = -1; dx <= 1; dx++) {
            MapLocation me = rc.getLocation();
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation)) {
                    rc.mineLead(mineLocation);
                }

            }

        }
        Direction dir = directions[rng.nextInt(directions.length)];

        if(strat == Strategy.FOLLOWER){
            try{
                MapLocation pos = rc.senseRobot(followID).getLocation();
                dir = rc.getLocation().directionTo(pos);
            }catch(GameActionException e){
                try{
                followID = Path.findLeaderAndMove(rc); //finds leader and moves rc because 
                }catch(GameActionException ee){ }
            }
            if (rc.canMove(dir)) {
                rc.move(dir);
                System.out.println("I moved!");
            }
            /**
             * directs the miners around itself
             */
        }else if(strat == Strategy.LEADER){
            MapLocation resourcePos = ArrayTools.deserialize(rc.readSharedArray(0));
            
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
    private static void runBuilder(RobotController rc) { //builders should update economic conditions in shared
        String builderOrder = utility.deserializeCountAndOrder(rc.readSharedArray(10));//TODO: Fix

        if(builderOrder == "BUILD"){
            MapLocation targetLocation = utility.deserializeMapLocation(rc.readSharedArray(11));
            if(targetLocation.isAdjacentTo(rc.getLocation())){
                Direction dir = rc.getLocation().directionTo(targetLocation);
                if(rc.canBuildRobot(RobotType.LABORATORY, dir)){
                    rc.buildRobot(RobotType.LABORATORY, dir);
                    int read = 0; 
                    try{
                        read = rc.readSharedArray(2);
                    }catch(GameActionException e) {
                        e.printStackTrace();
                    }
                    rc.writeSharedArray(2, read+1);
                }
            }
        }
    }

    private static void runWatchtower(RobotController rc) {

    }

    private static void runLaboratory(RobotController rc) {

    }
}
