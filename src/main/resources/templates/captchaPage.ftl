<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <title>Pass captcha to access content</title>
    <link rel="stylesheet" href="css/captcha.css">
</head>

<body>
    <div id="captcha-form">
        <div id="header">
            <div id="welcome-text">
                <p>Select all images with <b>Girls</b></p>
            </div>
            <div id="girl-sample">
                <img src="img/girl_sample.png">
            </div>

        </div>

        <form method="get" action="/validate">
            <div id="task-form">
                <#list imageList as image>
                    <div class="single-image">
                        <img alt="img" src="data:image/png;base64,${image}" />
                        <input class="single-checkbox" type="checkbox"
                            name="answer" value="${image?counter}">
                    </div>
                </#list>
            </div>
            <p>
                <input type="hidden" name="fileId" value="${fileId}">
                <input type="hidden" name="taskId" value="${taskId}">
                <input type="submit" value="Verify">
            </p>

        </form>
    </div>


</body>

</html>