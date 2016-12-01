<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import= "java.sql.*" %>

<% /**
* This class handles the validation of user input during their attempt to login
* to the admin dashboard.
* Written by Joel Galva
*/%>



<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login Validate</title>
    </head>
    <body>    
 	<%	
	try{
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
				+ "user=root&password=ojmayonnaise");
		PreparedStatement pst = connection.prepareStatement("SELECT username, password FROM users WHERE username=? and password=?");
		pst.setString(1, username);
		pst.setString(2, password);
		ResultSet rs = pst.executeQuery();
		if (rs.next())
			out.println("Valid login");
		if((username.equals("admin") && password.equals("admin")))
            {
            session.setAttribute("username",username);
            response.sendRedirect("admindashboard.jsp");
            }
		else
			out.println("Invalid login");
	}
	catch(Exception e){
		out.println(e.toString());
	}
 %>
     </body>
</html>