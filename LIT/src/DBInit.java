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
			PreparedStatement pst;
			/*
			// create table for bills
			pst = connection.prepareStatement("CREATE TABLE Bills (" +
																"Id int," +
																"Title varchar(2083)," +
																"Committee varchar(128)," +
																"StartDate date," +
																"LastActiveDate date," +
																"Status varchar(128)," +
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
			**/
			// populate legislator table
			Properties legislators = new Properties();
			legislators.load(new FileInputStream("./etc/legislators.properties"));
			for (Entry<Object, Object> leg : legislators.entrySet()) {
				pst = connection.prepareStatement("INSERT INTO Legislators VALUES (?, ?, 0, 0)");
				pst.setInt(1, Integer.parseInt((String) leg.getKey()));
				pst.setString(2, (String) leg.getValue());
				pst.executeUpdate();	
			}
			/*
			// create table for categories
			pst = connection.prepareStatement("CREATE TABLE Categories (" +
												"Id int," +
												"Name varchar(128)," +
												"PRIMARY KEY (Id))");
			pst.executeUpdate();
			
			// populate categories
			Properties categories = new Properties();
			categories.load(new FileInputStream("./etc/categories.properties"));
			for (Entry<Object, Object> cat : categories.entrySet()) {
				pst = connection.prepareStatement("INSERT INTO Categories VALUES (?, ?)");
				pst.setInt(1, Integer.parseInt((String) cat.getKey()));
				pst.setString(2, (String) cat.getValue());
				pst.executeUpdate();
			}
			
			// create table for category relationships
			pst = connection.prepareStatement("CREATE TABLE HasCategory (" +
											  "Bill int REFERENCES Bills(Id)," +
											  "Category int REFERENCES Categories(Id))");
			pst.executeUpdate();
			
			// create table for sponsors
			pst = connection.prepareStatement("CREATE TABLE Sponsors (" +
											  "Bill int REFERENCES Bills(Id)," +
											  "Sponsor int REFERENCES Legislators(Id))");
			pst.executeUpdate();
			
			// create table for areas of concentration
			pst = connection.prepareStatement("CREATE TABLE Concentrations (" +
											  "Legislator int REFERENCES Legislators(Id)," +
											  "Category int REFERENCES Categories(Id))");
			pst.executeUpdate();
			
			// create table for bill word clouds
			pst = connection.prepareStatement("CREATE TABLE BillWordClouds (" +
											  "Bill int REFERENCES Bills(Id)," +
											  "Word varchar(32)," +
											  "Count int)");
			pst.executeUpdate();
			
			// create table for legislator word clouds
			pst = connection.prepareStatement("CREATE TABLE LegWordClouds (" +
											  "Legislator int REFERENCES Legislators(Id)," +
											  "Word varchar(32)," +
											  "Count int)");
			pst.executeUpdate();
			
			// close connection
			pst.close();
			connection.close();
			reparedStatement**/
			connection.close();
			System.out.println("Successfully initialized db");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}

}
