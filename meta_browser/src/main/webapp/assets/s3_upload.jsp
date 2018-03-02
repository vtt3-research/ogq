<%@ page language="java" contentType="text/html; charset=UTF-8" import="
java.util.*
,java.io.*
,java.text.SimpleDateFormat

,java.net.*

,org.w3c.dom.*

,java.awt.*
,java.awt.image.BufferedImage
,java.io.*
,java.net.URL
,java.util.Calendar
,javax.imageio.ImageIO

" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="ko">
<%!
%>
<%
    try{


    }catch(Exception e){e.printStackTrace();}

%>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no">

    <script src="${pageContext.request.contextPath}/assets/js/jquery-1.12.4.min.js"></script>
    <script src="https://sdk.amazonaws.com/js/aws-sdk-2.173.0.min.js"></script>
<!--
    <script src="https://sdk.amazonaws.com/js/aws-sdk-2.173.0.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/aws-sdk-2.175.0.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/aws-sdk-2.175.0.min.js"></script>
-->


    <script>
        var names = [""

        ];

        function getURL(name){
            return "${pageContext.request.contextPath}"+name;
        }

        function drawLinks(){
            for(var i=0; i<names.length; i++){
                $("#sampleItem").clone().appendTo("#container")
                    .find("a")
                    .attr("href", "${pageContext.request.contextPath}"+names[i])
                    .text(names[i]);
            }
        }

        function goAfter(){
//	setTimeout( function() {
            location.href=getURL(names[0]);
//    }, 14000);//페이지 로딩 몇초후 디폴트로 이동
        }

        function onload(){
//	drawLinks();
//	goAfter();
        }

        function test(){
            location.href=getURL(names[0]);
        }

        function changeHost(){
            location.href="http://"+$('#hoster').val()+"${pageContext.request.requestURI}?${pageContext.request.queryString}";
        }





        /********** AWS 시작 **************/

            //url: DB에서 가져온다.
            //id: 생성한다. 컨텐츠 아이디랑 같다.

            //1. 동영상업로드
            //1.1. 전송 기능
            //1.2. cancel 기능
            //1.3. progress 기능
            //1.4. 완료콜백 기능

            //

        var bucketRegion = 'ap-northeast-2';//ap-northeast-2
        var bucketName = 'vtt-ogq';
        var IdentityPoolId = 'IDENTITY_POOL_ID';

        AWS.config.update({
            region: bucketRegion,
            credentials: new AWS.CognitoIdentityCredentials({
                IdentityPoolId: IdentityPoolId
            })
        });

        var s3 = new AWS.S3({
            apiVersion: '2006-03-01',
            params: {Bucket: bucketName}
        });





        function attachVideo() {
            var movieFile = document.getElementById('sourceFile').files[0];
            if(movieFile==undefined) return;

            var fileName = movieFile.name;
            var fullPAth = encodeURIComponent('movie_attach') + '//' + fileName;
            console.log(3);
            s3.upload({
                Key: fullPAth,
                Body: movieFile,
                ACL: 'public-read'
            }, function(err, data) {
                console.log(4);
                if (err) {
                    return alert('s3.upload().Error: ', err.message);
                }
                alert('Successfully uploaded movie.');
            });
        }


        /**************** AWS 끝 **********************/








    </script>
</head>
<body onload="javascript:onload()">

<%



%>

<form action="/submitOK" method="post" enctype="multipart/form-data">
</form>

<input type="file" id="sourceFile" name="sourceFile">
<button onclick="attachVideo();">do upload</button>



</body>
</html>