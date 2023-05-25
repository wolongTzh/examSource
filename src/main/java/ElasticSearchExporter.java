import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class ElasticSearchExporter {

    public static void main(String[] args) throws Exception {
        List<String> ls = Arrays.asList("irqa");
        for(String l : ls) {
            request(l);
        }
    }

    public static void request(String name) throws IOException {

        String index = name;  // 索引名
        String path = "./" + name + "_es_out.txt";
        File file = new File(path);
        FileWriter fileWritter = new FileWriter(file.getName(),false);
        String url = "http://47.94.201.245:9200/" + index +  "/_search?scroll=5m";

        // 发送 HTTP GET 请求
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        String requestBody = "{\"query\":{\"match_all\":{}},\"size\":\"10000\"}";
        connection.setDoOutput(true);
        connection.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));
        int responseCode = connection.getResponseCode();
        System.out.println("Response code: " + responseCode);

        // 读取响应数据
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONObject jsonObject = JSON.parseObject(response.toString());
        JSONArray contentArray = jsonObject.getJSONObject("hits").getJSONArray("hits");
        String record = "";
        for(int i=0; i<contentArray.size(); i++) {
            JSONObject content = contentArray.getJSONObject(i).getJSONObject("_source");
            JSONObject needWrite = new JSONObject();
            needWrite.put("predicate", null);
            needWrite.put("subject", null);
            needWrite.put("value", content.get("content"));
            needWrite.put("all", content.get("content"));
            // 打印响应数据
            record += JSON.toJSONString(needWrite) + "\n";
        }
        fileWritter.write(record);
        fileWritter.flush();

        String scrollId = (String) jsonObject.get("_scroll_id");
        int totalSize = 383867;
        int curSize = 10000;
        int acc = 10000;
        while (curSize < totalSize) {
            record = "";

            url = "http://47.94.201.245:9200/_search/scroll";
            // 发送 HTTP GET 请求
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            requestBody = "{\"scroll\":\"5m\",\"scroll_id\":\"%s\"}";
            requestBody = String.format(requestBody, scrollId);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));
            responseCode = connection.getResponseCode();
            System.out.println("Response code: " + responseCode);

            // 读取响应数据
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            jsonObject = JSON.parseObject(response.toString());
            contentArray = jsonObject.getJSONObject("hits").getJSONArray("hits");
            for(int i=0; i<contentArray.size(); i++) {
                JSONObject content = contentArray.getJSONObject(i).getJSONObject("_source");
                JSONObject needWrite = new JSONObject();
                needWrite.put("predicate", null);
                needWrite.put("subject", null);
                needWrite.put("value", content.get("content"));
                needWrite.put("all", content.get("content"));
                // 打印响应数据
                record += JSON.toJSONString(needWrite) + "\n";
            }
            fileWritter.write(record);
            fileWritter.flush();
            System.out.println(curSize + "all = " + totalSize);
            curSize += 10000;
            acc += 10000;
            if(acc >= 50000) {
                fileWritter.close();
                acc = 0;
                file = new File(path.replace(".txt", curSize + ".txt"));
                fileWritter = new FileWriter(file.getName(),false);
            }
        }
        fileWritter.close();
    }
}
