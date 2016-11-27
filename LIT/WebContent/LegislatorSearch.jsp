<%@ page import = "java.lang.*" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ page import = "java.sql.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Legislator Search Page</title>
</head>
<body>
	<div align="center">
	<h1><u>Legislator Name Search Page</u></h1>

	<%
	// get user selected Legislator Name from Drop Down List
	String LegName = request.getParameter("legislator");
	
	int leg = Integer.parseInt(LegName);
	
	// get list of bills from DB
	Class.legName("com.mysql.jdbc.Driver").newInstance();
	Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
	+ "user=root&password=ojmayonnaise");
	PreparedStatement pst = connection.prepareStatement
			("SELECT Name, PassedBills, FailedBills FROM Legislators WHERE Id = ?");
	pst.setInt(1, leg);
	ResultSet rs = pst.executeQuery();
	
	// print list of bills
	rs.next();
	
	String DbLegName = rs.getString("Name");
	int passedBills = rs.getInt("PassedBills");
	int failedBills = re.getInt("FailedBills");
	
	out.print("<br/><br/>");
	out.print("<form name=\"submitForm\" method=\"post\" action=\"legislatorprofile.jsp\">" +
				"<input type=\"hidden\" name=\"DbLegNamer\" value=\"" + DbLegName + "\">" +
		    	"<A HREF=\"javascript:document.submitForm.submit()\">legislator Name " + DbLegName + "</A>" +
				"</form>");
	
	out.print("<br/><br/>");
	out.print("<form name=\"submitForm\" method=\"post\" action=\"legislatorprofile.jsp\">" +
				"<input type=\"hidden\" name=\"passedBills\" value=\"" + passedBills + "\">" +
		    	"<A HREF=\"javascript:document.submitForm.submit()\">Passed Bills " + passedBills + "</A>" +
				"</form>");
	
	out.print("<br/><br/>");
	out.print("<form name=\"submitForm\" method=\"post\" action=\"legislatorprofile.jsp\">" +
				"<input type=\"hidden\" name=\"failedBills\" value=\"" + failedBills + "\">" +
		    	"<A HREF=\"javascript:document.submitForm.submit()\">Failed Bills " + failedBills + "</A>" +
				"</form>");
	}
	%>
	</div>
	</div>
</body>
</html>