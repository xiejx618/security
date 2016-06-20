<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>test</title>
</head>
<body>
<c:url value="" var="loginUrl"/>
<form action="${pageContext.servletContext.contextPath}/login" method="post">
    <c:if test="${SPRING_SECURITY_LAST_EXCEPTION.message != null}">
        <p>
           ${SPRING_SECURITY_LAST_EXCEPTION.message}
        </p>
    </c:if>
    <c:if test="${param.logout != null}">
        <p>
            You have been logged out.
        </p>
    </c:if>
    <p>
        <label for="username">Username</label>
        <input type="text" id="username" name="username" value="admin"/>
    </p>
    <p>
        <label for="password">Password</label>
        <input type="password" id="password" name="password" value="admin"/>
    </p>
    <p>
        <input type="checkbox" name="remember-me" value="true"/>Remember me
    </p>
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    <button type="submit" class="btn">Log in</button>
</form>
</body>
</html>
