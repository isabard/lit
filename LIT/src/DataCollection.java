import java.io.IOException;
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

	public static void main(String[] args) {
		Vector<Bill> test = new Vector<Bill>();
		fetchCategory(150, test);

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
				if (text.contains("Senate Bill No.") || text.contains("House Bill No.")) {
					currentBill = new Bill();
					currentBill.setCategory(Integer.toString(category));
					bills.addElement(currentBill);
					currentBill.setId(Integer.parseInt(text.substring(text.lastIndexOf(" ") + 1)));
					
					//System.out.println("Bill " + currentBill.getId());
				}
				// then, check if it's a sponsors element
				else if ((text.length() >= 2) && text.substring(0, 2).equals("BY")) {
					String rawSponsors = text.substring(4);
					String[] sponsors = rawSponsors.split(",");
					for (String s : sponsors)
						currentBill.addSponsor(s);
					
					//System.out.println("Added to billno " + currentBill.getId() + " " + currentBill.getSponsors());
				}
				// then, check if it's a title
				else if ((text.length() >= 10) && text.substring(0, 9).equals("ENTITLED,")) {
					currentBill.setTitle(text.substring(10));
					
					//System.out.println("Added to billno " + currentBill.getId() + " " + currentBill.getTitle());
				}
				// then, check if it's a date
				else if ((text.length() >= 10) && text.substring(0, 10).matches("\\d{2}/\\d{2}/\\d{4}")) {
					// check if it's the start date
					if (text.contains("Introduced,")) {
						currentBill.setStartDate(text.substring(0, 10));
						
						// set the committee
						currentBill.setCommittee(text.substring(35));
						
						//System.out.println("Added to billno " + currentBill.getId() + " " + currentBill.getStartDate() + " " + currentBill.getCommittee());
					}
					else {
						currentBill.setLastActiveDate(text.substring(0, 10));
						
						//System.out.println("Added to billno " + currentBill.getId() + " " + currentBill.getLastActiveDate());
					}
					
					// set the status
					currentBill.setStatus(text.substring(11));
					
					//System.out.println("Added to billno " + currentBill.getId() + " " + currentBill.getStatus() );
				}
			}
			
			// get links to bill contents
			selected = doc.select("a[href]");
			
			int billIndex = 0;
			for (int i = 0; i < selected.size(); i ++) {
				if (selected.get(i).text().length() == 4) {
					bills.get(billIndex).setLink(selected.get(i).attr("abs:href"));
					billIndex++;
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
		String title, category, committee, startDate, lastActiveDate, status, link;
		Vector<String> sponsors;
		
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
		Bill(int id, String title, String category, String committee, String startDate, 
				String lastActiveDate, String status, String link) {
			this.id = id;
			this.title = title;
			this.category = category;
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
			this.category = null;
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
		 * @return the category
		 */
		String getCategory() {
			return category;
		}

		/**
		 * @param category the category to set
		 */
		void setCategory(String category) {
			this.category = category;
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
		
		public String toString() {
			return "Bill no: " + id + "\n" +
					"Titled " + title + "\n" +
					"Category " + category + "\n" +
					"Committee " + committee + "\n" +
					"Start and last active " + startDate + " " + lastActiveDate + "\n" +
					"Status " + status + "\n" +
					"Sponsors " + sponsors + "\n" +
					"Link " + link;
		}
	}
}
