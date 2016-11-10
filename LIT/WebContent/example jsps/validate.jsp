<%@page import="java.sql.*" %>
<%
	try{
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		 Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javademo?" + "user=root&password=ClancysLays");
		PreparedStatement pst = connection.prepareStatement("SELECT username, password FROM users WHERE username=? and password=?");
		pst.setString(1, username);
		pst.setString(2, password);
		ResultSet rs = pst.executeQuery();
		if (rs.next())
			out.println("Valid login");
		else
			out.println("Invalid login");
	}
	catch(Exception e){
		out.println(e.toString());
	}
%>