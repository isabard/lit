package litsupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.*;
import org.apache.pdfbox.pdfparser.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class accomplishes all of the necessary steps to fetch and parse data from the external source
 * and update the database, including processing data for word clouds and areas of concentration.
 * Further, it has a servet wrapper to interface with the admindashboard jsp via AJAX requests, and the
 * actual data collection itself is a Runnable compliant class so that it can execute on its own
 * thread without the triggering method blocking until it is done.
 * @author scott
 *
 */
@WebServlet("/DataCollection")
public class DataCollection extends HttpServlet{
	// variable needed to comply with HttpServlet interface
	private static final long serialVersionUID = 1L;
	// base link to fetch data from source 
	final static String baseLink = "http://status.rilin.state.ri.us/bill_history_report.aspx?year=2016";
	// base links for bill pdfs
	final static String baseHouse = "http://webserver.rilin.state.ri.us/BillText/BillText16/HouseText16/H#.pdf";
	final static String baseSenate = "http://webserver.rilin.state.ri.us/BillText/BillText16/SenateText16/S#.pdf";
	// successful/failed status lists
	final static String[] successStatuses = {"Signed by Governor", "Effective without Governor's signature"};
	final static String[] failedStatuses = {"Vetoed by Governor", "Committee recommended measure be held for further study"};
	
	// queue of status messages that are fetched for the admindashboard
	ConcurrentLinkedQueue<String> message = new ConcurrentLinkedQueue<String>();
	// executor for the data collection to run on its own thread
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // object to check for whether or not the data collection thread has completed
    @SuppressWarnings("rawtypes")
	Future future;
	
    // constructor does not need anything for this situation
	public DataCollection() {
	}
	
	/**
	 * Method called when a get AJAX request comes through to start the data collection thread.
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Collect collection = new Collect();
		future = executor.submit(collection);
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * Method called when a post AJAX request comes through to return the message at the
	 * front of the queue or a 0 to signify a completed collection thread.
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (future.isDone()) {
			executor.shutdown();
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write("0");
		}
		else {
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			if (!message.isEmpty())
				response.getWriter().write(message.poll());
		}
	}
	
	/**
	 * Class to implement the Runnable interface around the data collection tasks so
	 * they execute in their own thread.
	 * @author scott
	 *
	 */
	public class Collect implements Runnable {	
		/**
		 * Only method required in a Runnable class.
		 */
		public void run() {
			// connect to database
			Connection connection = getConnection();
			
			try {
			
				// only proceed with a connection
				if (connection != null) {
					message.add("Got connection to database.");
					// get current bills from database
					Vector<Bill> allBills = new Vector<Bill>();
					Vector<Integer> runningIds = new Vector<Integer>();
					Vector<Integer> existingBills = new Vector<Integer>();
					int gotBills = getBills(connection, allBills, runningIds, existingBills);
					
					// only proceed if bills were actually retrieved successfully
					if (gotBills == 0) {
						// create map of legislator names to ids
						TreeMap<String, Integer> legIds = new TreeMap<String, Integer>();
						int madeMap = makeLegIdMap(legIds);
						
						// only proceed with a successfully made map
						if (madeMap == 0) {
							// add bills to database
							int wereAdded = addUpdate(allBills, existingBills, connection, legIds);
							if (wereAdded != 0) {
								message.add("Did not complete bill adding successfully.");
							}
							else {
								message.add("Completed bill adding successfully.");
							}
							
							// update legislator success/fail bill counts
							int wasUpdated = updateStats(legIds, connection);
							if (wasUpdated != 0) {
								message.add("Did not complete updating bill success/fail successfully.");
							}
							else {
								message.add("Completed updating bill success/fail and areas of concentration successfully.");
							}
						}
						else {
							message.add("Did not complete making a map of legislator names to IDs successfully.");
						}
					}
					else {
						message.add("Did not complete getting all bills successfully.");
					}
				}
			}
			// catch thrown exceptions during normal running that are not in the individual methods
			catch (Exception e) {
				message.add("1");
				executor.shutdown();
			}
			// try to close the connection
			try {
				connection.close();
			} 
			// catch in the rare instance where the connection cannot be closed
			catch (SQLException e) {
				message.add("1");
				executor.shutdown();
			}
		}
		
		/**
		 * Method to sort a wordcloud map
		 * @param map
		 * @return
		 */
		String[] getTopTen(TreeMap<String, Integer> map) {
			String[] topTen = new String[10];
			
			// iterate through map
			for (Entry<String, Integer> e : map.entrySet()) {
				// compare to current array
				for (int i = 0; i < 10; i++) {
					// if entry is empty, add this one
					if (topTen[i] == null) {
						topTen[i] = e.getKey();
						break;
					}
					// if count of current is greater than array value's, add it
					else if (e.getValue() > map.get(topTen[i])) {
						topTen[i] = e.getKey();
						break;
					}
				}
			}
			
			return topTen;
		}
		
		/**
		 * Method to sort areas of concentration
		 * @param map
		 * @return
		 */
		Integer[] getTopThree(TreeMap<Integer, Integer> map) {
			Integer[] topTen = new Integer[3];
			
			// iterate through map
			for (Entry<Integer, Integer> e : map.entrySet()) {
				// compare to current array
				for (int i = 0; i < 10; i++) {
					// if entry is empty, add this one
					if (topTen[i] == null) {
						topTen[i] = e.getKey();
						break;
					}
					// if count of current is greater than array value's, add it
					else if (e.getValue() > map.get(topTen[i])) {
						topTen[i] = e.getKey();
						break;
					}
				}
			}
			
			return topTen;
		}
		
		/**
		 * Method to iterate through all categories and fetch bils, adding them and their ids to
		 * running lists.
		 * @param connection
		 * @param allBills
		 * @param runningIds
		 * @param existingBills
		 * @return
		 */
		int getBills(Connection connection, Vector<Bill> allBills, Vector<Integer> runningIds, Vector<Integer> existingBills) {
			Properties categoryPairs = new Properties();
			try {
				// get current bills from database
				PreparedStatement pst = connection.prepareStatement("SELECT Id FROM Bills");
				ResultSet rs = pst.executeQuery();
				while (rs.next()) {
					existingBills.add(rs.getInt("Id"));
				}
				
				// open properties file of category pairs
				categoryPairs.load(getServletContext().getResourceAsStream("/WEB-INF/categories.properties"));
				//int numCats = categoryPairs.size();
				//int completedCats = 0;
				
				// create temporary vector for each category
				Vector<Bill> temp;
	
				// get bills category by category
				int fetched = 0;
				for (Object cat : categoryPairs.keySet()) {
					Integer category = Integer.parseInt((String) cat);
					temp = new Vector<Bill>();
					fetched = fetchCategory(category, temp);
					
					if (fetched == 0) {
						// add bill and its id to running lists
						for (Bill b : temp) {
							if (!runningIds.contains(b.getId())) {
								allBills.add(b);
								runningIds.add(b.getId());
							}
						}
					}
					
					//completedCats++;
					
					//System.out.println("Finished " + completedCats + "/" + numCats + " categories.");
				}
				message.add("Finished getting all bills.");
			} catch (Exception e) {
				System.out.println("Crashed during bill collection.");
				e.printStackTrace();
				return 1;
			}
			
			return 0;
		}
		
		/**
		 * Method to get a connection to the database.
		 * @return
		 */
		Connection getConnection() {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				return DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
																		+ "user=root&password=ojmayonnaise");
			} catch (Exception e) {
				return null;
			}
		}
		
		/**
		 * Method to count the successful and failed bills for each legislator and update the database,
		 * as well as update their areas of concentration and word clouds.
		 * @param legIds
		 * @param connection
		 */
		int updateStats(TreeMap<String, Integer> legIds, Connection connection) {
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
					//System.out.println("Updated Legislator " + leg + " with success/fail " + success + "/" + fail);
				
					// count areas (categories) of concentration
					TreeMap<Integer, Integer> areas = new TreeMap<Integer, Integer>();
					for (Integer b : bills) {
						pst = connection.prepareStatement("SELECT Category FROM HasCategory WHERE Bill = ?");
						pst.setInt(1, b);
						rs = pst.executeQuery();
						int cat = 0;
						while (rs.next()) {
							cat = rs.getInt("Category");
							if (areas.keySet().contains(cat)) {
								areas.put(cat, areas.get(cat) + 1);
							}
							else {
								areas.put(cat, 1);
							}
						}
					}
					
					// remove old areas from table
					pst = connection.prepareStatement("DELETE FROM Concentrations WHERE Legislator = ?");
					pst.setInt(1, leg);
					pst.executeUpdate();
					
					
					Integer[] sorted = getTopThree(areas);
					
					// add to table
					//System.out.print("Gave legislator " + leg + " areas of concentration ");
					for (int i = 0; i < 3; i++) {
						if (sorted.length >= i) {
							pst = connection.prepareStatement("INSERT INTO Concentrations VALUES (?,?)");
							pst.setInt(1, leg);
							pst.setInt(2, (int) sorted[i]);
							pst.executeUpdate();
							//System.out.print(" " + sorted[i]);
						}
						else {
							break;
						}
					}
					//System.out.println();
					
					// get word clouds of sponsored bills
					TreeMap<String, Integer> clouds = new TreeMap<String, Integer>();
					for (Integer b : bills) {
						pst = connection.prepareStatement("SELECT Word, Count FROM BillWordClouds WHERE Bill = ?");
						pst.setInt(1, b);
						rs = pst.executeQuery();
						while (rs.next()) {
							String word = rs.getString("Word");
							int count = rs.getInt("Count");
							if (clouds.containsKey(word)) {
								clouds.put(word, clouds.get(word) + count);
							}
							else {
								clouds.put(word, count);
							}
						}
					}
					
					String[] sortedClouds = getTopTen(clouds);
					
					// remove current from table
					pst = connection.prepareStatement("DELETE FROM LegWordClouds WHERE Legislator = ?");
					pst.setInt(1, leg);
					pst.executeUpdate();
					
					// add newly calculated words
					for (String s : sortedClouds) {
						pst = connection.prepareStatement("INSERT INTO LegWordClouds VALUES (?,?,?)");
						pst.setInt(1, leg);
						pst.setString(2, s);
						pst.setInt(3, clouds.get(s));
						pst.executeUpdate();
					}
					
					//System.out.println("Updated legislator " + leg);
				}
			} catch (Exception e) {
				System.out.print("Crash during legislator update.");
				e.printStackTrace();
				return 1;
			}
			
			return 0;
		}

		/**
		 * Method to create a map of legislator names to their ids.
		 * @param legIds
		 */
		int makeLegIdMap(TreeMap<String, Integer> legIds) {
			try {
				// open properties file
				Properties legislators = new Properties();
				legislators.load(getServletContext().getResourceAsStream("/WEB-INF/legislators.properties"));
				// iterate through all legislators and fill in
				for (Entry<Object, Object> e : legislators.entrySet()) {
					String name = (String) e.getValue();
					legIds.put(name.substring(0, name.indexOf(",")), Integer.parseInt((String) e.getKey()));
				}
				message.add("Made legislator-id map.");
			}
			catch (Exception e) {
				System.out.println("Crash during legislator-id map build.");
				e.printStackTrace();
				return 1;
			}
			
			return 0;
		}
		
		/**
		 * Method to take fetched bills and add/update the database with them, including
		 * constructing the word clouds of new bills.
		 * 
		 * @param allBills
		 * @param existingBills
		 * @param connection
		 * @param legIds
		 */
		int addUpdate(Vector<Bill> allBills, Vector<Integer> existingBills, Connection connection,
								TreeMap<String, Integer> legIds) {
			// add bills to database
			PreparedStatement pst;
			ResultSet rs;
			
			try {
				// get ommitted words list
				InputStream fis = getServletContext().getResourceAsStream("/WEB-INF/ommittedwordslist.treeset");
				ObjectInputStream ois = new ObjectInputStream(fis);
				@SuppressWarnings("unchecked")
				TreeSet<String> owl = (TreeSet<String>) ois.readObject();
				fis.close();
				ois.close();
				
				for (Bill b : allBills) {
					// fix rare bad dates
					if (b.getStartDate() == null)
						b.setStartDate("01/01/2016");
					if ((b.getLastActiveDate() == null) && (b.getStartDate() != null))
						b.setLastActiveDate(b.getStartDate());
					else if (b.getLastActiveDate() == null)
						b.setLastActiveDate("01/01/2016");
					
					// convert date to SQL format
					SimpleDateFormat sqlDate = new SimpleDateFormat("MM/dd/yyyy");
					
					// see if bill is new or already in db
					if (existingBills.contains(b.getId())) {
						// update bill
						pst = connection.prepareStatement("UPDATE Bills SET Title = ?, "
								+ "Committee = ?, StartDate = ?, LastActiveDate = ?, Status = ?, "
								+ "Link = ? WHERE Id = ?");
						pst.setString(1, b.getTitle());
						pst.setString(2, b.getCommittee());
						pst.setDate(3, new Date(sqlDate.parse(b.getStartDate()).getTime()));
						pst.setDate(4, new Date(sqlDate.parse(b.getLastActiveDate()).getTime()));
						pst.setString(5, b.getStatus());
						pst.setString(6, b.getLink());
						pst.setInt(7, b.getId());
						pst.executeUpdate();
						//System.out.println("Updated bill number " + b.getId());
						
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
						pst.setDate(4, new Date(sqlDate.parse(b.getStartDate()).getTime()));
						pst.setDate(5, new Date(sqlDate.parse(b.getLastActiveDate()).getTime()));
						pst.setString(6, b.getStatus());
						pst.setString(7, b.getLink());
						pst.executeUpdate();
						//System.out.println("Added bill number " + b.getId());
						
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
						
						// create word cloud from bill's text
						URL url = new URL(b.getLink());
						URLConnection uconnection = url.openConnection();
						InputStream in = uconnection.getInputStream();
						RandomAccessBuffer rab = new RandomAccessBuffer(in);
						
						PDFParser parser = new PDFParser(rab);
						parser.parse();
						COSDocument cosDoc = parser.getDocument();
						PDFTextStripper stripper = new PDFTextStripper();
						PDDocument pdDoc = new PDDocument(cosDoc);
						String text = stripper.getText(pdDoc);
						text = text.replaceAll("[^a-zA-Z\\s]", "");
						String[] words = text.split("\\s");
						pdDoc.close();
						cosDoc.close();
						rab.close();
						in.close();
						
						
						TreeMap<String, Integer> countedWords = new TreeMap<String, Integer>();
					
						for (String w : words) {
							if (w.length() >= 4 && !owl.contains(w)) {
								if (countedWords.keySet().contains(w)) {
									countedWords.put(w, countedWords.get(w) + 1);
								}
								else {
									countedWords.put(w, 1);
								}
							}
						}
						
						String sorted[] = getTopTen(countedWords);
						
						// use top ten
						for (String s : sorted) {
							pst = connection.prepareStatement("INSERT INTO BillWordClouds VALUES (?, ?, ?)");
							pst.setInt(1, b.getId());
							pst.setString(2, s);
							pst.setInt(3, countedWords.get(s));
							pst.executeUpdate();
						}
					}
				}
			} catch (Exception e) {
				System.out.println("Crash during bill add/update.");
				e.printStackTrace();
				return 1;
			}
			
			return 0;
		}
		
		/**
		 * Method to fetch a given category's (by id) bills
		 * @param category
		 * @param bills
		 */
		int fetchCategory(int category, Vector<Bill> bills) {
			// construct url with category
			String url = baseLink + "&category=" + category;
			
			try {
				// get the page's HTML
				Document doc = Jsoup.parse(Jsoup.connect(url).timeout(10*1000).get().html());
				
				// if no bills exist in the category, just skip it
				if (doc.toString().contains("Total Bills: 0") && doc.toString().contains("No Bills Met this Criteria")) {
					//System.out.println("Skipping " + category);
					return 0;
				}
				
				//System.out.println("Trying " + category);
				
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
				System.out.print("Crash during category fetch.");
				e.printStackTrace();
				return 1;
			}
			
			return 0;
		}
		
		/**
		 * Data structure to intermediately hold bills and their data from fetch until database update.
		 * 
		 * @author scott
		 *
		 */
		class Bill {
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
}
