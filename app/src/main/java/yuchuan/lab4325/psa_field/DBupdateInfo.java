package yuchuan.lab4325.psa_field;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by root on 10/14/15.
 */
public class DBupdateInfo {
    public static void updateDB(double lat,double lon,double dis, double speed, double acc, int status, int remainTime, int minSpeed, int maxSpeed){
        try
        {
            Connection con=DBconnection.getConnection();
            PreparedStatement psta;
            psta = con.prepareStatement("insert into field_data (`distance`, `speed`, `acc`, `signal`, `remaintime`,`minspeed`,`maxspeed`,) values ("
                    +dis+","+speed+","+acc+","+status+","+remainTime+","+minSpeed+","+maxSpeed+");");
//            ResultSet rs =
              psta.executeUpdate();
//            if(rs.next())
//                System.out.println("Success insert!"+rs.getString(1));
//            ;
//            rs.close();
            psta.close();
            con.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }


}
