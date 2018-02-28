<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <title>Upload image</title>
<head>
</head>

<body>
    <h1>Upload new image</h1>
    <form method="post" action="/upload" enctype="multipart/form-data">
        <input type="text" name="tags">
        <input type="file" name="file">
        <input type="submit" value="Upload">
    </form>
    <h2>Recent 5 files</h2>
    <#list recent as recentImage>
      <img alt="img" src="data:image/png;base64,${recentImage}" />
    </#list>
</body>

</html>