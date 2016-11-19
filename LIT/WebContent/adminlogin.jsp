<%@ page import= "java.sql.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Admin Login</title>
</head>
<body>
	<div align="center">
	<h1><u>Login</u></h1>
	
	<% 
	// get parameters
	String username = request.getParameter("username");
	String password = request.getParameter("password");
	
	// Get data from DB
	Class.forName("com.mysql.jdbc.Driver").newInstance();
	Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.4:3306/litdb?" 
	+ "user=root&password=ojmayonnaise");
	
	PreparedStatement pst = connection.prepareStatement("SELECT username, password FROM users WHERE username=? and password=?");
	pst.setString(1, username);
	pst.setString(2, password);
	ResultSet rs = pst.executeQuery();
	
	if (rs.next())
		out.println("Valid login");
	else
		out.println("Invalid login");
	%>
	
	<form method="post" action="validate.jsp">
			<div align="center">
			<table border="1">
				<thead>
					<tr>
						<th colspan="2">Login</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Username</td>
						<td><input type="text" name="username" required/></td>
					</tr>
					<tr>
						<td>Password</td>
						<td><input type="password" name="password" required/></td>
					</tr>
					<tr>
						<td colspan="2" align="center"><input type="submit" value="Login" />
							&nbsp;&nbsp;
							<input type="reset" value="Reset" />
						</td>
					</tr>
				</tbody>
			</table>
			</div>
		</form>
</body>
</html>