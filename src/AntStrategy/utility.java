package AntStrategy;
import java.lang.reflect.Array;
import java.util.ArrayList;

import battlecode.common.*;

public class utility {

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
    
    public static MapLocation getMapCenter(RobotController rc){
        return new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
    }
    /**
     * calculates probability of enemy appearing on a given square, takes in robotInfo array to save byteCode.
     * @param rc
     * @param loc
     * @param inView 
     * @return
     */
    public static Double enemyProbability(RobotController rc, MapLocation loc, RobotInfo[] inView){
        Double P = 0.0; //assumption
        return P;
    }

    /**
     * gets direction adjacent to direction minus 30 degrees
     * @param Direction
     * @return
     */
    public static Direction minus30(Direction dir){
        for(int i = 0; i < directions.length; i++){
            if(directions[i] == dir){
                if (i == 0){
                    return directions[directions.length-1];
                }
                return directions[i-1];
            }
        }
        return dir;
    }
    /**
     * gets direction adjacent to direction plus 30 degrees
     * @param Direction
     * @return
     */
    public static Direction plus30(Direction dir){
        for(int i = 0; i < directions.length; i++){
            if(directions[i] == dir){
                if (i == directions.length-1){
                    return directions[0];
                }
                return directions[i+1];
            }
        }
        return dir;
    }
    public static int[] deserializeRobotLocation(int serial){ 
        String str = Integer.toBinaryString(serial);
        String xS = str.substring(1, 7);
        String yS = str.substring(6, 12);
        String type = str.substring(12);
        System.out.println(str);
        System.out.println(xS);
        System.out.println(yS);
        int y = 0;
        int x = 0;
        int typeN = 0;
        for (int i = 0; i < 6; i++) {
            if(i< 4 && type.charAt(i) == '1') {
                typeN = typeN + (int)Math.pow(2, 14-i);
            }
            if(xS.charAt(i) == '1') {
                x = x + (int)Math.pow(2, 14-i);
            }
            if(yS.charAt(i) == '1') {
                y = y + (int)Math.pow(2, 14-i);
            }
        }
        return new int[]{x, y, typeN};
    }
    //0-5 for x(int)
    // 6-11 for y(int)
    //12-14 for Type"
    public static int serializeRobotLocation(RobotInfo ri) {
        String binS = Integer.toBinaryString(ri.getLocation().x);
        for (int i = binS.length(); i < 6; i++) //this would be one line in python
            binS = "0" + binS;

        String yS = Integer.toBinaryString(ri.getLocation().y);
        for (int i = yS.length(); i < 6; i++)
            yS = "0" + yS;

        String vNumber = "111"; //noType for some reason => 111
        switch(ri.getType()){
            case ARCHON: vNumber = "000";  break;
            case MINER: vNumber = "001";  break;
            case BUILDER: vNumber = "010";  break;
            case SOLDIER: vNumber = "011";  break;
            case SAGE: vNumber = "100";  break;
            case LABORATORY: vNumber = "101";  break;
            case WATCHTOWER: vNumber = "110";  break;
        }
        System.out.println("y: " + yS);
        System.out.println("x: " + binS);
        System.out.println("type: " + vNumber);
        binS = "0" + binS + yS + vNumber;
        int out = 0;
        for (int i = 0; i < 14; i++) {
            if(binS.charAt(i) == '1') {
                out = out + (int)Math.pow(2, 14-i);
            }
        }
        System.out.println(out);
        return out;
    }
}
