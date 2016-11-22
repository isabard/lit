import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * This class accomplishes all of the necessary steps to fetch and parse data from the external source
 * and update the database, including processing data for word clouds and areas of concentration.
 * @author scott
 *
 */
public class DataCollection {
	
	// base link to fetch data from source 
	final static String baseLink = "http://status.rilin.state.ri.us/bill_history_report.aspx?year=2016";
	// base links for bill pdfs
	final static String baseHouse = "http://webserver.rilin.state.ri.us/BillText/BillText16/HouseText16/H#.pdf";
	final static String baseSenate = "http://webserver.rilin.state.ri.us/BillText/BillText16/SenateText16/S#.pdf";
	// successful/failed status lists
	final static String[] successStatuses = {"Signed by Governor", "Effective without Governor's signature"};
	final static String[] failedStatuses = {"Vetoed by Governor"};
	
	public static void main(String[] args) {
		try {
			// connect to database
			Connection connection = getConnection();
			
			// get current bills from database
			Vector<Bill> allBills = new Vector<Bill>();
			Vector<Integer> runningIds = new Vector<Integer>();
			Vector<Integer> existingBills = new Vector<Integer>();
			getBills(connection, allBills, runningIds, existingBills);
			
			// create map of legislator names to ids
			TreeMap<String, Integer> legIds = null;
			makeLegIdMap(legIds);
			
			// add bills to database
			addUpdate(allBills, existingBills, connection, legIds);
			
			// update legislator success/fail bill counts
			updateStats(legIds, connection);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	static void getBills(Connection connection, Vector<Bill> allBills, Vector<Integer> runningIds, Vector<Integer> existingBills) {
		Properties categoryPairs = new Properties();
		try {
			// get current bills from database
			PreparedStatement pst = connection.prepareStatement("SELECT Id FROM Bills");
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				existingBills.add(rs.getInt("Id"));
			}
			
			// open properties file of category pairs
			categoryPairs.load(new FileInputStream("./etc/categories.properties"));
			int numCats = categoryPairs.size();
			int completedCats = 0;
			
			// create temporary vector for each category
			Vector<Bill> temp;

			// get bills category by category
			for (Object cat : categoryPairs.keySet()) {
				Integer category = Integer.parseInt((String) cat);
				temp = new Vector<Bill>();
				fetchCategory(category, temp);
				
				// add bill and its id to running lists
				for (Bill b : temp) {
					if (!runningIds.contains(b.getId())) {
						allBills.add(b);
						runningIds.add(b.getId());
					}
				}
				
				completedCats++;
				
				System.out.println("Finished " + completedCats + "/" + numCats + " categories.");
			}
			System.out.println("Finished getting all bills.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to get a connection to the database.
	 * @return
	 */
	static Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			return DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
																	+ "user=root&password=ojmayonnaise");
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Method to count the successful and failed bills for each legislator and update the database.
	 * @param legIds
	 * @param connection
	 */
	static void updateStats(TreeMap<String, Integer> legIds, Connection connection) {
		PreparedStatement pst;
		ResultSet rs;
		try {
			for (Integer leg : legIds.values()) {
				// get all of legislator's bills
				pst = connection.prepareStatement("SELECT Bill FROM Sponsors WHERE Sponsor = ?");
				pst.setInt(1, leg);
				rs = pst.executeQuery();
				Vector<Integer> bills = new Vector<Integer>();
				while (rs.next()) {
					bills.add(rs.getInt("Bill"));
				}
				//System.out.println("Got bills " + bills.toString());
				
				// get each bill's status
				Vector<String> statuses = new Vector<String>();
				for (Integer b : bills) {
					pst = connection.prepareStatement("SELECT Status FROM Bills WHERE Id = ?");
					pst.setInt(1, b);
					rs = pst.executeQuery();
					if (rs.next())
						statuses.add(rs.getString("Status"));
				}
				//System.out.println("Got statuses " + statuses.toString());
				
				// count successes and failures
				int success = 0;
				int fail = 0;
				
				Counter: for (String s : statuses) {
					for (String succ : successStatuses) {
						if (s.equals(succ)) {
							success++;
							continue Counter;
						}
					}
					
					for (String failed : failedStatuses) {
						if (s.equals(failed)) {
							fail++;
							continue Counter;
						}
					}
				}
				
				// update legislator success/fail counts
				pst = connection.prepareStatement("UPDATE Legislators SET PassedBills = ?, FailedBills = ? WHERE Id = ?");
				pst.setInt(1, success);;
				pst.setInt(2, fail);
				pst.setInt(3, leg);
				pst.executeUpdate();
				System.out.println("Updated Legislator " + leg + " with success/fail " + success + "/" + fail);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to create a map of legislator names to their ids.
	 * @param legIds
	 */
	static void makeLegIdMap(TreeMap<String, Integer> legIds) {
		try {
			// open properties file
			Properties legislators = new Properties();
			legislators.load(new FileInputStream("./etc/legislators.properties"));
			legIds = new TreeMap<String, Integer>();
			// iterate through all legislators and fill in
			for (Entry<Object, Object> e : legislators.entrySet()) {
				String name = (String) e.getValue();
				legIds.put(name.substring(0, name.indexOf(",")), Integer.parseInt((String) e.getKey()));
			}
			System.out.println("Made legislator-id map");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to take fetched bills and add/update the database with them.
	 * 
	 * @param allBills
	 * @param existingBills
	 * @param connection
	 * @param legIds
	 */
	static void addUpdate(Vector<Bill> allBills, Vector<Integer> existingBills, Connection connection,
							TreeMap<String, Integer> legIds) {
		// add bills to database
		PreparedStatement pst;
		ResultSet rs;
		
		SimpleDateFormat sqlDate = new SimpleDateFormat("dd/MM/yyyy");
		
		try {
			for (Bill b : allBills) {
				// temporary bad date fix
				if (b.getStartDate() == null)
					b.setStartDate("1/1/2016");
				if (b.getLastActiveDate() == null)
					b.setLastActiveDate("1/1/2016");
				
				// see if bill is new or already in db
				if (existingBills.contains(b.getId())) {
					// update bill
					pst = connection.prepareStatement("UPDATE Bills SET Title = ?, "
							+ "Committee = ?, StartDate = ?, LastActiveDate = ?, Status = ?, "
							+ "Link = ? WHERE Id = ?");
					pst.setString(1, b.getTitle());
					pst.setString(2, b.getCommittee());
					pst.setDate(3, new java.sql.Date(sqlDate.parse((b.getStartDate())).getTime()));
					pst.setDate(4, new java.sql.Date(sqlDate.parse((b.getLastActiveDate())).getTime()));
					pst.setString(5, b.getStatus());
					pst.setString(6, b.getLink());
					pst.setInt(7, b.getId());
					pst.executeUpdate();
					System.out.println("Updated bill number " + b.getId());
					
					// get current sponsors
					pst = connection.prepareStatement("SELECT Sponsor FROM Sponsors WHERE Bill = ?");
					pst.setInt(1, b.getId());
					rs = pst.executeQuery();
					Vector<Integer> currentSponsors = new Vector<Integer>();
					while (rs.next()) {
						currentSponsors.add(rs.getInt("Sponsor"));
					}
					
					// add sponsor relationships that don't exist
					for (String s : b.getSponsors()) {
						Integer leg = legIds.get(s);
						// if sponsor not found or already exists, move onto next
						if (leg == null || currentSponsors.contains(leg)) {
							continue;
						}
						else {
							pst = connection.prepareStatement("INSERT INTO Sponsors VALUES (?,?)");
							pst.setInt(1, b.getId());
							pst.setInt(2, leg);
							pst.executeUpdate();
							//System.out.println("Added Sponsor " + leg + " to " + b.getId());
						}
					}
					
					// get current categories
					pst = connection.prepareStatement("SELECT Category FROM HasCategory WHERE Bill = ?");
					pst.setInt(1, b.getId());
					rs = pst.executeQuery();
					Vector<Integer>	currentCategories = new Vector<Integer>();
					while (rs.next()) {
						currentCategories.add(rs.getInt("Category"));
					}
					
					// add category relationships that don't exist
					for (Integer i : b.getCategories()) {
						// if already exists, move onto next
						if (currentCategories.contains(i)) {
							continue;
						}
						else {
							pst = connection.prepareStatement("INSERT INTO HasCategory VALUES (?,?)");
							pst.setInt(1, b.getId());
							pst.setInt(2, i);
							pst.executeUpdate();
						}
					}
				}
				else {
					// insert bill
					pst = connection.prepareStatement("INSERT INTO Bills VALUES (?,?,?,?,?,?,?)");
					pst.setInt(1, b.getId());
					pst.setString(2, b.getTitle());
					pst.setString(3, b.getCommittee());
					pst.setDate(4, new java.sql.Date(sqlDate.parse((b.getStartDate())).getTime()));
					pst.setDate(5, new java.sql.Date(sqlDate.parse((b.getLastActiveDate())).getTime()));
					pst.setString(6, b.getStatus());
					pst.setString(7, b.getLink());
					pst.executeUpdate();
					System.out.println("Added bill number " + b.getId());
					
					// add sponsor relationships
					for (String s : b.getSponsors()) {
						Integer leg = legIds.get(s);
						// if sponsor not found, move onto next
						if (leg == null)
							continue;
						pst = connection.prepareStatement("INSERT INTO Sponsors VALUES (?,?)");
						pst.setInt(1, b.getId());
						pst.setInt(2, leg);
						pst.executeUpdate();
						//System.out.println("Added Sponsor" + leg + " to " + b.getId());
					}
					
					// add category relationships
					for (Integer i : b.getCategories()) {
						pst = connection.prepareStatement("INSERT INTO HasCategory VALUES (?,?)");
						pst.setInt(1, b.getId());
						pst.setInt(2, i);
						pst.executeUpdate();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to fetch a given category's (by id's) bills
	 * @param category
	 * @param bills
	 */
	static void fetchCategory(int category, Vector<Bill> bills) {
		// construct url with category
		String url = baseLink + "&category=" + category;
		
		try {
			// get the page's HTML
			Document doc = Jsoup.parse(Jsoup.connect(url).get().html());
			
			// if no bills exist in the category, just skip it
			if (doc.toString().contains("Total Bills: 0") && doc.toString().contains("No Bills Met this Criteria")) {
				System.out.println("Skipping " + category);
				return;
			}
			
			System.out.println("Trying " + category);
			
			// select the bill area
			Elements selected = doc.select("span");
			
			// parse the span with the bills
			doc = Jsoup.parse(selected.last().toString());
			
			// parse out the div areas
			selected = doc.select("div");
			
			// parse out bills
			Bill currentBill = null;
			for (Element e : selected) {
				// get element's text
				String text = e.text();
				
				// first, check if it's a bill number element
				if (text.contains("Senate Bill No.") || text.contains("House Bill No.") ||
						text.contains("Senate Resolution No.") || text.contains("House Resolution No.")) {
					currentBill = new Bill();
					currentBill.addCategory(category);
					bills.addElement(currentBill);
					currentBill.setId(Integer.parseInt(text.replaceAll("\\D+","")));
					
					// add link
					if (text.contains("House")) {
						currentBill.setLink(baseHouse.replace("#", Integer.toString(currentBill.getId())));
					}
					else if (text.contains("Senate")) {
						currentBill.setLink(baseSenate.replace("#", Integer.toString(currentBill.getId())));
					}
					
					// detect sub bills
					if (text.contains("SUB")) {
						String toAdd = "";
						
						// add a or b
						if (text.contains("SUB A"))
							toAdd = "A";
						else if (text.contains("SUB B"))
							toAdd = "B";
						
						// add aa if it's amended
						if (text.contains("as amended"))
							toAdd = toAdd + "aa";
						
						// add to link
						currentBill.setLink(currentBill.getLink().replace(".pdf", toAdd + ".pdf"));
					}
					
					//System.out.println("Bill " + currentBill.getId());
				}
				// then, check if it's a sponsors element
				else if ((text.length() >= 2) && text.substring(0, 2).equals("BY")) {
					String rawSponsors = text.substring(4);
					String[] sponsors = rawSponsors.split(",");
					for (String s : sponsors)
						currentBill.addSponsor(s.trim());
					
					//System.out.println("Added to billno " + currentBill.getId() + " " + currentBill.getSponsors());
				}
				// then, check if it's a title
				else if ((text.length() >= 10) && text.substring(0, 9).equals("ENTITLED,")) {
					currentBill.setTitle(text.substring(10).trim());
					
					//System.out.println("Added to billno " + currentBill.getId() + " " + currentBill.getTitle());
				}
				// then, check if it's a date
				else if ((text.length() >= 10) && text.substring(0, 10).matches("\\d{2}/\\d{2}/\\d{4}")) {
					// check if it's the start date
					if (text.contains("Introduced,")) {
						currentBill.setStartDate(text.substring(0, 10));
						
						// set the committee
						currentBill.setCommittee(text.substring(35).trim());
						
						//System.out.println("Added to billno " + currentBill.getId() + " " + currentBill.getStartDate() + " " + currentBill.getCommittee());
					}
					else {
						currentBill.setLastActiveDate(text.substring(0, 10));
						
						//System.out.println("Added to billno " + currentBill.getId() + " " + currentBill.getLastActiveDate());
					}
					
					// set the status
					currentBill.setStatus(text.substring(11).trim());
					
					//System.out.println("Added to billno " + currentBill.getId() + " " + currentBill.getStatus() );
				}
			}
			
			//for (Bill b : bills)
				//System.out.println(b.toString());
				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Data structure to intermediately hold bills and their data from fetch until database update.
	 * 
	 * @author scott
	 *
	 */
	static class Bill {
		int id;
		String title, committee, startDate, lastActiveDate, status, link;
		Vector<String> sponsors;
		Vector<Integer> categories;
		
		/**
		 * Constructor with all locals as parameters.
		 * 
		 * @param id
		 * @param title
		 * @param category
		 * @param committee
		 * @param startDate
		 * @param lastActiveDate
		 * @param status
		 * @param link
		 */
		Bill(int id, String title, String committee, String startDate, 
				String lastActiveDate, String status, String link) {
			this.id = id;
			this.title = title;
			this.categories = new Vector<Integer>();
			this.committee = committee;
			this.startDate = startDate;
			this.lastActiveDate = lastActiveDate;
			this.status = status;
			this.link = link;
			this.sponsors = new Vector<String>();
		}
		
		/**
		 * Constructor with no parameters to initialize locals to 0/null.
		 */
		Bill() {
			this.id = 0;
			this.title = null;
			this.categories = new Vector<Integer>();
			this.committee = null;
			this.startDate = null;
			this.lastActiveDate = null;
			this.status = null;
			this.link = null;
			this.sponsors = new Vector<String>();
		}
		
		/**
		 * @return the sponsors
		 */
		Vector<String> getSponsors() {
			return sponsors;
		}
		
		/**
		 * add a sponsor
		 * @param sponsor
		 */
		void addSponsor(String sponsor) {
			sponsors.add(sponsor);
		}

		/**
		 * @return the id
		 */
		int getId() {
			return id;
		}

		/**
		 * @param id the id to set
		 */
		void setId(int id) {
			this.id = id;
		}

		/**
		 * @return the title
		 */
		String getTitle() {
			return title;
		}

		/**
		 * @param title the title to set
		 */
		void setTitle(String title) {
			this.title = title;
		}

		/**
		 * @return the categories
		 */
		Vector<Integer> getCategories() {
			return categories;
		}

		/**
		 * @param category the category to set
		 */
		void addCategory(Integer category) {
			categories.add(category);
		}

		/**
		 * @return the committee
		 */
		String getCommittee() {
			return committee;
		}

		/**
		 * @param committee the committee to set
		 */
		void setCommittee(String committee) {
			this.committee = committee;
		}

		/**
		 * @return the startDate
		 */
		String getStartDate() {
			return startDate;
		}

		/**
		 * @param startDate the startDate to set
		 */
		void setStartDate(String startDate) {
			this.startDate = startDate;
		}

		/**
		 * @return the lastActiveDate
		 */
		String getLastActiveDate() {
			return lastActiveDate;
		}

		/**
		 * @param lastActiveDate the lastActiveDate to set
		 */
		void setLastActiveDate(String lastActiveDate) {
			this.lastActiveDate = lastActiveDate;
		}

		/**
		 * @return the status
		 */
		String getStatus() {
			return status;
		}

		/**
		 * @param status the status to set
		 */
		void setStatus(String status) {
			this.status = status;
		}

		/**
		 * @return the link
		 */
		String getLink() {
			return link;
		}

		/**
		 * @param link the link to set
		 */
		void setLink(String link) {
			this.link = link;
		}
		
		/**
		 * equals method to determine if two bills are the same
		 * @param other
		 * @return
		 */
		public boolean equals(Bill other) {
			return (this.id == other.getId()) && (this.title.equals(other.getTitle())) &&
					(this.categories.equals(other.getCategories())) && (this.committee.equals(other.getCommittee())) &&
					(this.startDate.equals(other.getStartDate())) && (this.lastActiveDate.equals(other.getLastActiveDate())) &&
					(this.status.equals(other.getStatus())) && (this.link.equals(other.getLink())) &&
					(this.sponsors.equals(other.getSponsors()));
		}
		
		/**
		 * toString method to show Bill's info
		 */
		public String toString() {
			return "Bill no: " + id + "\n" +
					"Titled " + title + "\n" +
					"Categories " + categories + "\n" +
					"Committee " + committee + "\n" +
					"Start and last active " + startDate + " " + lastActiveDate + "\n" +
					"Status " + status + "\n" +
					"Sponsors " + sponsors + "\n" +
					"Link " + link;
		}
	}
}
