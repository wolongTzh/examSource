import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentExtractor {

    static Pattern imgPattern = Pattern.compile("<img>(.*?)</img>");
    static Map<String, String> imgMap = new HashMap<>();
    static Map<String, String> uriMap = new HashMap<>();

    public static List<String> getUriList(JSONObject result) {

        List<String> uriList = new ArrayList<String>();
        JSONArray questions = result.getJSONArray("Questions");
        if(questions == null) {
            return null;
        }
        for(int i=0; i<questions.size(); i++) {
            JSONArray keypoints = questions.getJSONObject(i).getJSONArray("Keypoint");
            if(keypoints == null) {
                continue;
            }
            for(int j=0; j<keypoints.size(); j++) {
                String uri = (String) keypoints.getJSONObject(j).get("uri");
                if(uri != null && !uri.equals("")) {
                    uriList.add(uri);
                }
            }
        }
        return uriList;
    }

    public static String getContent(JSONObject result) {

        return null;
    }

    public static void readJsonOfUri(JSONObject result, String name) {
        List<String> uriList = getUriList(result);
        if(uriList != null && uriList.size() != 0) {
            for(String uri : uriList) {
                String origin = uriMap.getOrDefault(uri, "");
                if(!origin.equals("")) {
                    List<String> names = Arrays.asList(origin.split("&"));
                    if(names.contains(name)) {
                        continue;
                    }
                    name = "&" + name;
                }
                if(name.length() > 1000) {
                    continue;
                }
                name = origin + name;
                uriMap.put(uri, name);
            }
        }
    }

    public static void addImgs(JSONObject result, String path) throws IOException {
        String source = result.toJSONString();
        Matcher matcher = imgPattern.matcher(source);
        JSONObject imgs = new JSONObject();
        while (matcher.find()) {
            String imgName = matcher.group(1);
            String url = imgMap.get(imgName);
            imgs.put(imgName, url);
        }
        result.put("pics", imgs);
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        // 创建文件
        file.createNewFile();
        // 写入文件
        Writer write = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        write.write(result.toJSONString());
        write.flush();
        write.close();
    }

    public static JSONObject loadJson(String filePath) throws IOException {
        File file = new File(filePath);
        Reader reader = new InputStreamReader(new FileInputStream(file), "Utf-8");
        int ch = 0;
        StringBuffer sb = new StringBuffer();
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        reader.close();
        String jsonStr = sb.toString();
        return JSON.parseObject(jsonStr);
    }

    public static void loadImgs(String sourcePath) throws IOException {
        imgMap.clear();
        BufferedReader br = new BufferedReader(new FileReader(sourcePath));
        // read until end of file
        String line;
        while ((line = br.readLine()) != null) {
            String url = line.split(".jpg: ")[1];
            String name = line.split(".jpg: ")[0];
            imgMap.put(name, url);
        }
    }

    public static void batchReader() throws IOException {
        String jsonPath = "C:\\Users\\feifei\\Desktop\\Gaokao\\json";
        String imgPath = "C:\\Users\\feifei\\Desktop\\Gaokao\\img";
        File file1 = new File(jsonPath);
        StringBuilder builder = new StringBuilder();
        String outputPath = "C:\\Users\\feifei\\Desktop\\Gaokao\\uriOut.json";
        FileWriter fileWriter = new FileWriter(outputPath);
        //判断是否有目录
        if(file1.isDirectory()) {
            //获取目录中的所有文件名称
            String[] yearName = file1.list();
            for(String str : yearName) {
                String yearPath = jsonPath + "\\" + str;
                loadImgs(imgPath + "\\" + str + "\\image_list.txt");
                File file2 = new File(yearPath);
                if(file2.isDirectory()) {
                    //获取目录中的所有文件名称
                    String[] innerName = file2.list();
                    for(String innerStr : innerName) {
                        String innerPath = yearPath + "\\" + innerStr;
                        File file3 = new File(innerPath);
                        if(file3.isDirectory()) {
                            //获取目录中的所有文件名称
                            String[] fileName = file3.list();
                            for(String s : fileName) {
                                String path = innerPath + "\\" + s;
                                JSONObject jsonObject = loadJson(path);
                                if(jsonObject == null) {
                                    System.out.println(path);
                                }
                                addImgs(jsonObject, path);
                                String name = str + "-" + innerStr + "-" + s.split("\\.")[0];
                               readJsonOfUri(jsonObject, name);
                            }
                        }
                    }
                }
            }
        }
        JSONObject out = JSONObject.parseObject(JSON.toJSONString(uriMap));
        fileWriter.write(JSON.toJSONString(out));
        fileWriter.flush();
        fileWriter.close();
    }

    public static Map<String, JSONObject> readRawJson() throws IOException {
        Map<String, JSONObject> map = new HashMap<>();
        String jsonPath = "C:\\Users\\feifei\\Desktop\\Gaokao\\json";
        File file1 = new File(jsonPath);
        //判断是否有目录
        if(file1.isDirectory()) {
            //获取目录中的所有文件名称
            String[] yearName = file1.list();
            for(String str : yearName) {
                String yearPath = jsonPath + "\\" + str;
                File file2 = new File(yearPath);
                if(file2.isDirectory()) {
                    //获取目录中的所有文件名称
                    String[] innerName = file2.list();
                    for(String innerStr : innerName) {
                        String innerPath = yearPath + "\\" + innerStr;
                        File file3 = new File(innerPath);
                        if(file3.isDirectory()) {
                            //获取目录中的所有文件名称
                            String[] fileName = file3.list();
                            for(String s : fileName) {
                                String path = innerPath + "\\" + s;
                                JSONObject jsonObject = loadJson(path);
                                if(jsonObject == null) {
                                    System.out.println(path);
                                    continue;
                                }
                                pickImgPath(str, jsonObject);
                                String name = str + "-" + innerStr + "-" + s.split("\\.")[0];
                                map.put(name, jsonObject);
                            }
                        }
                    }
                }
            }
        }
        return map;
    }

    static void pickImgPath(String name, JSONObject jsonObject) {
        String basePath = "/data/parser/GaoKao_data/GaoKao_generate/img";
        JSONObject pics = jsonObject.getJSONObject("pics");
        for(JSONObject.Entry entry : pics.entrySet()) {
            String picPath = (String) entry.getKey();
            entry.setValue(basePath + "/" + name + "/" + picPath + ".jpg");
        }
    }

    public static void main(String[] args) throws IOException {
        batchReader();
    }
}
