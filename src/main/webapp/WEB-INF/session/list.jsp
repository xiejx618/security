<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>test</title>
    <style type="text/css">
        table {
            border-collapse: collapse;
            border: none;
        }

        th, td {
            border: 1px solid #92c649;
        }
    </style>
</head>
<body>
<table>
    <tr>
        <th>id</th>
        <th>SessionId</th>
        <th>UserId</th>
        <th>lastRequest</th>
        <th>expired</th>
    </tr>
    <c:forEach items="${items}" var="item">
        <tr>
            <td>${item.id}</td>
            <td>${item.sid}</td>
            <td>${item.uid}</td>
            <td>${item.lastRequest}</td>
            <td>${item.expired}</td>
        </tr>
    </c:forEach>
</table>

</body>
</html>
