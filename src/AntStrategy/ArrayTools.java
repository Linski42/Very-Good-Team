package AntStrategy;
import battlecode.common.*;

public class ArrayTools {

    public int serialize(MapLocation pos){
        if(pos.x > 99 || pos.y > 99){
            System.out.println("broken on serialize");
        }
        String str = (String.valueOf(pos.x) + String.valueOf(pos.y));
        return Integer.parseInt(str);
    }

    public static MapLocation deserialize(int n){
        if(n < 1){
            return new MapLocation(0, 0);

        }
        String x = String.valueOf(n).substring(3);
        String y = String.valueOf(n).substring(0, 6);
        
        return new MapLocation(Integer.parseInt(x), Integer.parseInt(y));
    }
}
