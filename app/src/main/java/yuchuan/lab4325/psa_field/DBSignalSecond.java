package yuchuan.lab4325.psa_field;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DBSignalSecond
{	

		
	
		public static int[] ReadSignalTable(int TableId) {
			String FormatTime=null;
			int GrRemainTime=-1;
			int[] timeInfo=new int[4];
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
					timeInfo[0]=rs.getInt("Cyclesecond");
					timeInfo[1]=rs.getInt("s2");
//					timeInfo[2]=rs.getInt("g1");
//					timeInfo[3]=rs.getInt("r1");
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

