<%@ page import="java.sql.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Legislator Search Page</title>
</head>
<body>
	<div align="center">
	<h1><u>Legislator Name Search Page</u></h1>

	<%
	// get user selected Legislator Name from Drop Down List
	int leg = Integer.parseInt(request.getParameter("legislator"));
	
	// get info from "Legislators" table in database.
	Class.forName("com.mysql.jdbc.Driver").newInstance();
	Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
	+ "user=root&password=ojmayonnaise");
	PreparedStatement pst = connection.prepareStatement
			("SELECT Name, PassedBills, FailedBills FROM Legislators WHERE Id = ?");
	pst.setInt(1, leg);
	ResultSet rs = pst.executeQuery();
	
	// Assign local variables to values from "Legislators" table in DB
	rs.next();
	
	String DbLegName = rs.getString("Name");
	String[] legWordCloud = new String[10];
	String[] topThreeCats = new String[3];
	
	int passedBills = rs.getInt("PassedBills");
	int failedBills = rs.getInt("FailedBills");
	int totalBills = passedBills + failedBills;
	double successRate = (100 * ((totalBills - passedBills) / totalBills));
	
	// get info from "Concentrations" table in DB
	pst = connection.prepareStatement
			("SELECT c.Name FROM Concentrations AS co INNER JOIN Categories AS c.Id = co.Category WHERE co.Legislator = ?");
	pst.setInt(1, leg);
	rs = pst.executeQuery();
	
	// Sets local array to legislators top three areas of concentration
	int i = 0;
	while(rs.next())
	{
		topThreeCats[i] = rs.getString("Name");		
		i++;
	}
	
	// get info from "LegWordClouds" table in DB
	pst = connection.prepareStatement("SELECT Word FROM legWordClouds WHERE Legislator = ?");
	pst.setInt(1, leg);
	rs = pst.executeQuery();
	
	// Set local array to legislators word cloud
	i = 0;
	while(rs.next())
	{
		legWordCloud[i] = rs.getString("Word");		
		i++;
	}
	
	// Output the results
	out.print("Legislator Name: " + DbLegName);
	out.print("Passed Bills: " + passedBills);
	out.print("Failed Bills: " + failedBills);
	out.print("Success Rate: " + successRate);
	out.print("Legislator Word Cloud: ");
	
	for(i=0; i < legWordCloud.length; i++)
		out.print('\n' + legWordCloud[i]);
	
	out.print('\n');
	out.print("Legislator Top Three Categories: ");
	for(i=0; i < topThreeCats.length; i++)
		out.print('\n' + topThreeCats[i]);
	
	%>
	</div>
</body>
</html>
