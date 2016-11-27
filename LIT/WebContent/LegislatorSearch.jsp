<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Legislator Search Page</title>
</head>
<body>
	<div align="center">
	<h1><u>Legislator Name Search Page</u></h1>
	
	// Build Legislator Drop Down
	<div align="center">
	<h1><u>Legislator Search Results</u></h1>
	<form action="LegislatorName" method="POST">
	<select name="legislator">
	<option>Sam</option>
	</select>
	<input type="submit"/>
	</form>form>
	
	<%
	// get user selected Legislator Name from Drop Down List
	request.getParameter("Legislator");
	
	// ensure the user selects a legislator to search for
	while (Legislator == null)
	{
		out.print("You did not select a Legislator");
		request.getParameter("Legislator");
	}

	out.print("You selected: " + Legislator);
	
	// get list of bills from DB
	Class.legName("com.mysql.jdbc.Driver").newInstance();
	Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
	+ "user=root&password=ojmayonnaise");
	PreparedStatement pst = connection.prepareStatement
			("SELECT Legislator Names from Drop Down");
	ResultSet rs = pst.executeQuery();
	
	// print list of bills
	while(rs.next())
	{
		String LegName = rs.getString("Legislator");
		out.print("<br/><br/>");
		out.print("<form name=\"submitForm\" method=\"post\" action=\"legislatorprofile.jsp\">" +
					"<input type=\"hidden\" name=\"Legislator\" value=\"" + Legislator + "\">" +
			    	"<A HREF=\"javascript:document.submitForm.submit()\">Legislator Name " + Legislator + "</A>" +
					"</form>");
	}
	
	%>
	</div>
	</div>
</body>
</html>