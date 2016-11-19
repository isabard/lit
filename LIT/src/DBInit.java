import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Map.Entry;
import java.util.Properties;
/**
 * This class's main method will initialize the database and its tables.
 * 
 * @author scott
 *
 */
public class DBInit {

	public static void main(String[] args) {
		
		try {
			// connect to database
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
					+ "user=root&password=ojmayonnaise");
			
			// create table for bills
			PreparedStatement pst = connection.prepareStatement("CREATE TABLE Bills (" +
																"Id int," +
																"Title varchar(2083)," +
																"Category varchar(128)," +
																"Committee varchar(128)," +
																"StartDate date," +
																"LastActiveDate date," +
																"Status varchar(64)," +
																"Link varchar(2083)," +
																"PRIMARY KEY (Id))");
			pst.executeUpdate();
			
			// create table for legislators
			pst = connection.prepareStatement("CREATE TABLE Legislators (" +
												"Id int," +
												"Name varchar(128)," +
												"PassedBills int," +
												"FailedBills int," +
												"PRIMARY KEY (Id))");
			pst.executeUpdate();
			
			// populate legislator table
			Properties legislators = new Properties();
			legislators.load(new FileInputStream("./etc/legislators.properties"));
			for (Entry<Object, Object> leg : legislators.entrySet()) {
				pst = connection.prepareStatement("INSERT INTO Legislators VALUES (?, ?, 0, 0)");
				pst.setInt(1, Integer.parseInt((String) leg.getKey()));
				pst.setString(2, (String) leg.getValue());
				pst.executeUpdate();	
			}
			
			// create table for sponsors
			pst = connection.prepareStatement("CREATE TABLE Sponsors (" +
											  "Bill int REFERENCES Bills(Id)," +
											  "Sponsor int REFERENCES Legislators(Id))");
			pst.executeUpdate();
			
			// close connection
			pst.close();
			connection.close();
			
			System.out.println("Successfully initialized db");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}

}
