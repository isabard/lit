<%@page import="java.sql.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Legislator Profile</title>

<!-- Style to format sponsored bills -->
<style type="text/css">
form, table {
     display:inline;
     margin:0px;
     padding:0px;
}
</style>

</head>
<body>
	<h1><u>Legislator Profile Page</u></h1>

	<% 
		// Make sure there is a billno to acquire
		if (request.getParameter("legislator") == null)
			out.print("No legislator number was passed!");
		else {
			// if so, get it
			int leg = Integer.parseInt(request.getParameter("legislator"));

			//connect to db
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
					+ "user=root&password=ojmayonnaise");
			
			// get legislator's attributes
			PreparedStatement pst = connection.prepareStatement("SELECT Name, PassedBills, FailedBills FROM Legislators WHERE Id = ?");
			pst.setInt(1, leg);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				out.println("<b>Name:</b> " + rs.getString("Name") + "<br/>");
				double passed = rs.getInt("PassedBills");
				double failed = rs.getInt("FailedBills");
				int avg = (int) (100 * passed / (failed + passed));
				out.println("<b>Bill success rate:</b> " + avg + "%<br/>");
			}
			
			// get legislator concentrations
			pst = connection.prepareStatement
					("SELECT c.Name FROM Concentrations AS co INNER JOIN Categories AS c ON c.Id = co.Category WHERE co.Legislator = ?");
			pst.setInt(1, leg);
			rs = pst.executeQuery();
			boolean first = true;
			out.print("<b>Primary areas of concentration:</b> ");
			while(rs.next())
			{
				if (!first) {
					out.print(", ");
				}
				else {
					first = false;
				}
				out.print(rs.getString("Name"));
			}
			
			// get legislator word cloud
			pst = connection.prepareStatement("SELECT Word FROM LegWordClouds WHERE Legislator = ?");
			pst.setInt(1, leg);
			rs = pst.executeQuery();
			first = true;
			out.print("<br>" + "<b>Word cloud:</b> ");
			while(rs.next())
			{
				if (!first) {
					out.print(", ");
				}
				else {
					first = false;
				}
				out.print(rs.getString("Word"));
			}
			
			out.println("<br>" + "<b>Bills sponsored:</b>");
			
			out.print("<div style='overflow:auto;width:800px;height:250px;'>");
	
			// get bills by legislator
			pst = connection.prepareStatement("Select Bill FROM Sponsors WHERE Sponsor = ?");
			pst.setInt(1, leg);
			rs = pst.executeQuery();
			int count = 0;
			while (rs.next()) {
				if ((count % 10) == 0) {
					out.print("<br/>");
				}
				count++;
				String billno = Integer.toString(rs.getInt("Bill"));
				out.print("<form action=\"billprofile.jsp\" method=\"post\">" +
						  "<button type=\"submit\" name=\"billno\" value=\"" + billno +
						  "\" class=\"btn-link\">Bill " + billno +"</button>" +
						  "</form>");
			}
			out.print("</div>");
		}
	%>
</body>
</html>