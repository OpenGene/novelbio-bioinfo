<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<title>Feedback Form</title>
</head>
<body>
<form:form commandName="feedback" method="POST">
<form:errors path="*"/>
<table>
<tr>
<td>Your email:</td>
<td><form:input path="email" size="30" /></td>
</tr>
<tr>
<td>Rating:</td>
<td><c:forEach var="count" begin="1" end="5">
<form:radiobutton path="rating" value="${count}" /> ${count} 
</c:forEach></td>
</tr>
<tr>
<td>Comments:</td>
<td><form:textarea path="comments" cols="40" rows="10" /></td>
</tr>
<tr>
<td colspan="2"><input type="submit" value="Send" /></td>
</tr>
</table>
</form:form>
</body>
</html>