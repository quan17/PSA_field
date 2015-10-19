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
    public static int[] ReadPosTable(int TableId) {

        int[] timeInfo=new int[2];
        try
        {
            Connection con=DBconnection.getConnection();
            PreparedStatement psta;
            if(TableId>2&&TableId<7)
                psta = con.prepareStatement("select Cyclesecond,s2,g1,r1 from (select id,Cyclesecond,s2,g1,r1,Time_Stamp from lstatus_newjersey_sc1401_new order by id DESC limit 0,1)aa");
            else
                psta = con.prepareStatement("select Cyclesecond,s2,g1,r1 from (select id,Cyclesecond,s2,g1,r1,Time_Stamp from lstatus_newjersey_sc"+TableId+"_new order by id DESC limit 0,1)aa");

//				System.out.println("successState");


            ResultSet rs = psta.executeQuery();

            if(rs.next())
            {
                timeInfo[0]=rs.getInt("g1");
                timeInfo[1]=rs.getInt("r1");
//					System.out.println("CS: "+timeInfo[0]);
            }

            rs.close();
            psta.close();
            con.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return timeInfo;
    }



}
