package yuchuan.lab4325.psa_field;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by root on 10/19/15.
 */
public class DBconnection {

    public static Connection getConnection()
    {
        Connection con=null;
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            con = (Connection) DriverManager.getConnection(
//	    		  "jdbc:mysql://128.235.162.244:3306/apha_live","apha","aphauser"
                    "jdbc:mysql://128.235.162.244:3306/bmw", "app", "adam2009"
            );
		  System.out.println("Success connect Mysql server!");
        }catch( SQLException ee)
        {
            ee.printStackTrace();
        }
        catch (Exception eeea)
        {
            System.out.println("Error loading Mysql Driver!");
            eeea.printStackTrace();
        }
        return con;
    }
}
