<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <title>Pass captcha to access content</title>
<head>
</head>

<body>
    <h1>Pass captcha!</h1>
    <form method="get" action="/validate">
        <input type="hidden" name="fileId" value="${fileId}">
        <input type="submit" value="VALIDATE">
    </form>
</body>

</html>