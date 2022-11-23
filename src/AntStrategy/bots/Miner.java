package AntStrategy.bots;

import battlecode.common.*;

public class Miner {
    
    static void runMiner(RobotController rc) throws GameActionException {
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

        Direction dir = Path.follow(rc, followID);
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("I moved!");
        }
    }
}
