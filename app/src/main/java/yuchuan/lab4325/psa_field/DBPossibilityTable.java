package yuchuan.lab4325.psa_field;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by root on 10/14/15.
 */
public class DBPossibilityTable {
    public static int[] ReadPosTable(int TableId, int pos) {
        int[] timeInfo=new int[3];
        try
        {
            Connection con=DBconnection.getConnection();

            PreparedStatement psta;

            if(TableId>1402&&TableId<1407)
            {
                timeInfo[0] = -1;
                timeInfo[1] = -1;
                timeInfo[2] = -1;
            }
            else
            {
//                psta = con.prepareStatement("select "+TableId+"_Length, "+TableId+"_GE, "+TableId+"_RE from Possibility_EndTime where TIMESTAMPDIFF(MINUTE,createtime,NOW())<30;");
                psta = con.prepareStatement("select "+TableId+"_Length, "+TableId+"_pos_"+pos+"_GE, "+TableId+"_pos_"+pos+"_RE from Possibility_all_EndTime where TIMESTAMPDIFF(MINUTE,createtime,NOW())<30;");
                ResultSet rs = psta.executeQuery();

                if(rs.next())
                {
                    timeInfo[0]=rs.getInt(2);
                    timeInfo[1]=rs.getInt(3);
                    timeInfo[2]=rs.getInt(1);
                }
                rs.close();
                psta.close();
            }


            con.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return timeInfo;
    }



}
