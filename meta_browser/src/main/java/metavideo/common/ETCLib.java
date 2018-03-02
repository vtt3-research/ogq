package metavideo.common;

import com.google.gson.*;
import okhttp3.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ETCLib {

    //멀티파트 파일을 파일로 저장하고 파일명을 리턴.
    public static String multPart2File(MultipartFile multipartFile, String saveFileName){
        String tempFileName="";
        String fullSavePath="";
        File saveFile = null;

        tempFileName = new String(multipartFile.getOriginalFilename().getBytes());
        fullSavePath = "D:/attachments/" + saveFileName;
        saveFile = new File(fullSavePath);
        saveFile.getParentFile().mkdirs();

        try {
            multipartFile.transferTo(saveFile);//저장한다.
        }catch (IOException ioe){
            System.out.println("[IOException] "+ ioe.getMessage());
        }

        return fullSavePath;
    }


    //멀티파트 파일을 파일로 저장하고 파일명을 리턴.
    public static String multPart2File(MultipartFile multipartFile){
        String tempFileName="";
        String fullPath="";
        //String fileName = null;
        File ec2File = null;

        tempFileName = new String(multipartFile.getOriginalFilename().getBytes());
        fullPath = "D:/attachments/" + tempFileName;
        ec2File = new File(fullPath);
        while (ec2File.exists())// 있으면 0짜 하나 더붙임
        {
            tempFileName = tempFileName.replaceAll("[.]","0.");// 있으면 0짜 하나 더붙임
            fullPath = "D:/attachments/" + tempFileName;
            ec2File = new File(fullPath);
        }

        ec2File.getParentFile().mkdirs();

        try {
            multipartFile.transferTo(ec2File);//저장한다.
        }catch (IOException ioe){
            System.out.println("[IOException] "+ ioe.getMessage());
        }

        return fullPath;
    }

    //S3 전송은 AWSService에 있음.

    //비디오 태깅 API 호출
    public static void callVideoTaggingAPI(String callback_url, String request_id, String video_url){
        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "{" +
                        "\r\n   \"callback_url\": \""+callback_url+"\"," +
                        "\r\n   \"request_id\": \""+request_id+"\"," +
                        "\r\n   \"video_url\": \""+video_url+"\"" +
                        "\r\n}\r\n");
        Request request = new Request.Builder()
                .url("http://183.110.246.21:8080/ogq/videoTagging")
                .put(body)
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        okHttpClient.newCall(request).enqueue(null);


        /*
        client.newCall(request).enqueue(new Callback() {
            // 통신이 성공했을 때
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 통신 결과를 로그에 출력한다
                final String responseBody = response.body().string();
                Log.d(TAG, "result: " + responseBody);
                final Weather weather = new Gson().fromJson(responseBody, Weather.class);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.success(weather);
                    }
                });
            }

            // 통신이 실패했을 때
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.error(e);
                    }
                });
            }
        });
        */
    }







    public static void obtainParseResult2(String jsonString){
        Gson gson=null;
        try {
            gson = new com.google.gson.Gson();
        }catch (Exception e){e.printStackTrace();}

        JsonObject rootJO = gson.fromJson(jsonString, JsonObject.class);//노드
        //JsonElement rootJE = new JsonParser().parse(br);//노드
        //System.out.println("root: "+rootJO);


        JsonObject currentJO=rootJO.getAsJsonObject("businesses")
                //.getAsJsonObject("lastKey")
                //.getAsJsonObject("items")
                ;

        //동적으로 생성되는 키를 먼저 얻어서
        String lastKey = currentJO.getAsJsonPrimitive("lastKey").getAsString();



        //항목 리스트 부분을 얻는다.
        JsonArray ja =currentJO.getAsJsonObject(lastKey).getAsJsonArray("items");
        //System.out.println("ja: "+ja);

        //return getArrayFromJSON(ja);
    }

/*
    public static ArrayList<StoreVO> getArrayFromJSON(JsonArray jsonArray){
        ArrayList<StoreVO> resultArray = new ArrayList<StoreVO>();

        //각 항목을 처리한다.
        for(int i=0; i<jsonArray.size(); i++){
            JsonElement jsonElement = jsonArray.get(i);
            if(jsonElement.toString().equals("null"))continue;
            String s=jsonElement.toString();

            JsonObject thisSet = jsonElement.getAsJsonObject();

            StoreVO svo=new StoreVO();
            svo.id	=	(""+thisSet.get("id")).toString().replaceAll("\"","");
            svo.name	=	(""+thisSet.get("name")).toString().replaceAll("\"","");
            svo.businessCategory	=	(""+thisSet.get("businessCategory")).toString().replaceAll("\"","");
            svo.dbType	=	(""+thisSet.get("dbType")).toString().replaceAll("\"","");
            svo.category	=	(""+thisSet.get("category")).toString().replaceAll("\"","");
            svo.store_description	=	(""+thisSet.get("desc")).toString().replaceAll("\"","");
            svo.hasBooking	=	(""+thisSet.get("hasBooking")).toString().replaceAll("\"","");
            svo.x	=	(""+thisSet.get("x")).toString().replaceAll("\"","");
            svo.y	=	(""+thisSet.get("y")).toString().replaceAll("\"","");
            svo.distance	=	Double.parseDouble(("0"+thisSet.get("distance")).toString().replaceAll("[^.0-9]",""));
            svo.imageSrc	=	(""+thisSet.get("imageSrc")).toString().replaceAll("\"","");
            svo.imageCount	=	Integer.parseInt(("0"+thisSet.get("imageCount")).toString().replaceAll("[^0-9]",""));
            svo.phone	=	(""+thisSet.get("phone")).toString().replaceAll("\"","");
            svo.routeUrl	=	(""+thisSet.get("routeUrl")).toString().replaceAll("\"","");
            svo.streetViewUrl	=	(""+thisSet.get("streetViewUrl")).toString().replaceAll("\"","");
            svo.microReview	=	(""+thisSet.get("microReview")).toString().replaceAll("\"","");
            svo.roadAddr	=	(""+thisSet.get("roadAddr")).toString().replaceAll("\"","");
            svo.commonAddr	=	(""+thisSet.get("commonAddr")).toString().replaceAll("\"","");
            svo.addr	=	(""+thisSet.get("addr")).toString().replaceAll("\"","");
            svo.broadcastInfo	=	(""+thisSet.get("broadcastInfo")).toString().replaceAll("\"","");
            svo.blogCafeReviewCount	=	Integer.parseInt(("0"+thisSet.get("blogCafeReviewCount")).toString().replaceAll("[^0-9]",""));
            svo.bookingReviewCount	=	Integer.parseInt(("0"+thisSet.get("bookingReviewCount")).toString().replaceAll("[^0-9]",""));
            svo.moreUGCReviewsPath	=	(""+thisSet.get("moreUGCReviewsPath")).toString().replaceAll("\"","");
            svo.moreFsasReviewsPath	=	(""+thisSet.get("moreFsasReviewsPath")).toString().replaceAll("\"","");
            svo.tags	=	(""+thisSet.get("tags")).toString().replaceAll("\"","");
            svo.street_panorama	=	(""+thisSet.get("street_panorama")).toString().replaceAll("\"","");
            svo.naver_lastupdated	=	(""+thisSet.get("naver_lastupdated")).toString().replaceAll("\"","");
            svo.register_dt	=	(""+thisSet.get("register_dt")).toString().replaceAll("\"","");
            svo.update_dt	=	(""+thisSet.get("update_dt")).toString().replaceAll("\"","");

            String cat="restaurants";
            if(svo.moreUGCReviewsPath.contains("/")){
                cat=svo.moreUGCReviewsPath.split("/")[1];
            }
            String naverLastUpdate="<a id=\""+svo.id+"\" onclick=\"getTheDate('"+cat+"', '"+svo.id+"');\">[check]</a>";

            resultArray.add(svo);
        }

        return resultArray;

    }
*/





    //인풋스트림으로부터 문자열을 얻는다.
    public static String watchDog(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while( (nRead = is.read(data)) != -1 ) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();

        String str = new String(buffer.toByteArray());
        //System.out.println("XML String: "+str );
        return str;
    }

    //파일로그 찍기
    public static void printFileLog(String logString){
        String theDatetime = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss_SSS")
                .format(Calendar.getInstance().getTime());
        String direcoryPath="D:\\callbacker_logs";
        String filePath=direcoryPath+"\\"+"metavideo_"+theDatetime+".log";
        BufferedWriter bw = null;
        File directory = new File(direcoryPath);

        if(!directory.exists()) return;

        try{
            bw = new BufferedWriter(new FileWriter(filePath, true));
            bw.write(logString);
            bw.newLine();
            if(bw != null) bw.close();
        }catch (IOException ioe){}

    }



    public static String obtainDateString(String dateTime){
        String returnString="";
        //dateTime = dateTime.split(" ")[0];
        if(dateTime==null || dateTime.length()<5)return "";

        String yyyy="";
        String mm="";
        String dd="";
        String delimiter="/";

        yyyy=dateTime.substring(0,4);
        mm=dateTime.substring(4,6);
        dd=dateTime.substring(6,8);


        returnString=yyyy+delimiter+mm+delimiter+dd;
        return returnString;
    }




    public static String obtainDatetimeString(String dateTime){
        String returnString="";
        if(dateTime==null || dateTime.length()==0) return "-";

        String yyyy="";
        String mm="";
        String dd="";
        String delimiter="/";

        String hh="";
        String MM="";
        String ss="";
        String delimiter2=":";

        yyyy=dateTime.substring(0,4);
        mm=dateTime.substring(4,6);
        dd=dateTime.substring(6,8);

        hh=dateTime.substring(9,11);
        MM=dateTime.substring(11,13);
        ss=dateTime.substring(13,15);


        returnString=yyyy+delimiter+mm+delimiter+dd +", "+hh+delimiter2+MM+delimiter2+ss;
        return returnString;
    }


    public static String obtainFileSizeString(int byteSize){
        String returnString="";
        DecimalFormat df = new DecimalFormat("#,###.##");

        if(byteSize>1024*1024*1024){
            returnString = df.format(byteSize /(1024*1024*1024)) + "GB";
        }else{
            returnString = df.format(byteSize /(1024*1024)) + "MB";
        }

        return returnString;
    }

    public static void asyncTransfer(String savedFileFullPath, String saveFileName){
        final String fSavedFileFullPath = savedFileFullPath;
        final String fSaveFileName = saveFileName;
        new Thread(new Runnable() {
            @Override
            public void run() {
                AWSService.transferVideoFile(fSavedFileFullPath, fSaveFileName);//파일전송
                boolean b=false;
                b=new File(fSavedFileFullPath).delete();//보내고 나서 지운다.
            }
        }).start();
    }

}
