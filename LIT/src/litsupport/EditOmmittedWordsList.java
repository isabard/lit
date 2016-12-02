package litsupport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class EditOmmittedWordsList
 */
@WebServlet("/EditOmmittedWordsList")
public class EditOmmittedWordsList extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EditOmmittedWordsList() {
        super();
    }

	/**
	 * Method to retrieve the current list of words for display.
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String message = "";
		try {
			InputStream fis = getServletContext().getResourceAsStream("/WEB-INF/ommittedwordslist.treeset");
			ObjectInputStream ois = new ObjectInputStream(fis);
			@SuppressWarnings("unchecked")
			TreeSet<String> owl = (TreeSet<String>) ois.readObject();
			ois.close();
			fis.close();
			for (String s : owl) {
				message += (s + " ");
			}
		} catch (ClassNotFoundException e) {
			message = "Couldn't open list!";
		}
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(message);
	}

	/**
	 * Method to add or remove a word.
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// get type of operation and word to add/remove
		String type = (String) request.getParameter("Type");
		String word = (String) request.getParameter("Word");
		// remove possible leading/trailing whitespace and put to lowercase
		word = word.trim();
		word = word.toLowerCase();
		String message;
		try {
			// open current ommittedwordslist
			InputStream fis = getServletContext().getResourceAsStream("/WEB-INF/ommittedwordslist.treeset");
			ObjectInputStream ois = new ObjectInputStream(fis);
			@SuppressWarnings("unchecked")
			TreeSet<String> owl = (TreeSet<String>) ois.readObject();
			ois.close();
			fis.close();
			
			// add word
			if (type.equals("Add")) {
				// check to see if word is already in list
				if (owl.contains(word)) {
					message = word + " already exists in the list!";
				}
				else {
					// make sure it's a valid word 
					if (word.matches("[a-zA-Z]")) {
						// if so, add it and save
						try {
							owl.add(word);
							FileOutputStream fos = new FileOutputStream(new File(getServletContext().getResource("/WEB-INF/ommittedwordslist.treeset").toURI()));
							ObjectOutputStream oos = new ObjectOutputStream(fos);
							oos.writeObject(owl);
							oos.close();
							fos.close();
							message = "Added " + word + " to list!";
						} catch (URISyntaxException e) {
							message = "Couldn't write to list!";
						}
					}
					else {
						message = "Invalid word!";
					}
				}
			}
			// remove wrod
			else if (type.equals("Remove")) {
				// check to see if it's already in list
				if (owl.contains(word)) {
					// make sure it's a valid word
					if (word.matches("[a-zA-Z")){
						try {
							owl.remove(word);
							FileOutputStream fos = new FileOutputStream(new File(getServletContext().getResource("/WEB-INF/ommittedwordslist.treeset").toURI()));
							ObjectOutputStream oos = new ObjectOutputStream(fos);
							oos.writeObject(owl);
							oos.close();
							fos.close();
							message = "Removed " + word + " from list!";
						} catch (URISyntaxException e) {
							message = "Couldn't write to list!";
						}
					}
					else {
						message = "Invalid word!";
					}
				}
				else {
					message = word + " was not in the list to remove!";
				}
			}
			else {
				message = "Invalid arguments.";
			}
		} catch (ClassNotFoundException e) {
			message = "Could not open ommitted words list";
		}
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(message);
	}

}
