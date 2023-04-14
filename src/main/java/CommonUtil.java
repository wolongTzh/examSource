import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    /**
     *
     * @description: Map转实体类
     * @param <T>
     * @param map    需要初始化的数据，key字段必须与实体类的成员名字一样，否则赋值为空
     * @param entity 需要转化成的实体类
     * @return
     */
    public static <T> T mapToEntity(Map<String, Object> map, Class<T> entity) {
        T t = null;
        try {
            t = entity.newInstance();
            for (Field field : entity.getDeclaredFields()) {
                if (map.containsKey(field.getName())) {
                    boolean flag = field.isAccessible();
                    field.setAccessible(true);
                    Object object = map.get(field.getName());
                    if (object != null && field.getType().isAssignableFrom(object.getClass())) {
                        field.set(t, object);
                    }
                    field.setAccessible(flag);
                }
            }
            return t;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> MultiValueMap<Object, Object> entityToMutiMap(T entity) throws IllegalAccessException {
        MultiValueMap<Object, Object> map = new LinkedMultiValueMap<>();
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object name = field.getName();
            Object value = field.get(entity);
            map.add(name, value);
        }
        return map;
    }

    public static List<String> readPlainTextFile(String path) {
        File file =new File(path);
        List<String> contents = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));) {
            contents = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    public static JSONObject readJsonOut(String filePath) throws IOException {
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

    public static JSONArray readJsonArray(String filePath) throws IOException {
        File file = new File(filePath);
        Reader reader = new InputStreamReader(new FileInputStream(file), "Utf-8");
        int ch = 0;
        StringBuffer sb = new StringBuffer();
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        reader.close();
        String jsonStr = sb.toString();
        return JSON.parseArray(jsonStr);
    }

    public static Map<String, String> readMapOut(String filePath) throws IOException {
        JSONObject jsonObject = readJsonOut(filePath);
        return (Map) JSON.parseObject(JSON.toJSONString(jsonObject));
    }

    /**
     * load系列
     * 加载文件里的内容，在程序启动时执行
     */
    public static Map readJsonInResource(InputStreamReader inputStreamReader) {
        BufferedReader br = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer();
        String s = null;
        Map map = new HashMap();
        try {
            while((s = br.readLine()) != null){
                sb.append(s);
            }
            map = JSON.parseObject(sb.toString());
        }
        catch (Exception e) {
            logger.error("reading static file failed !");
        }
        return map;
    }

    public static List<String> readTextInResource(InputStreamReader inputStreamReader) throws IOException {
        BufferedReader br = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer();
        String s = null;
        List<String> ret = new ArrayList<>();
        try {
            while((s = br.readLine()) != null){
                ret.add(s);
            }
        }
        catch (Exception e) {
            logger.error("reading static file failed !");
        }
        return ret;
    }

    public static List<String> readTextFromPath(String path) {
        File file =new File(path);
        List<String> contents = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));) {
            contents = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    /**
     * 分页工具，根据输入进行分页 (页号从0开始)
     * @param input 输入的list
     * @param pageNum 页号
     * @param pageSize 页容量
     * @param <T> list的类型
     * @return 分页后的list
     */
    public static <T> List<T> pageHelper(List<T> input, Integer pageNum, Integer pageSize) {
        int start = pageNum * pageSize;
        int end = (start + pageSize) > input.size() ? input.size() : start + pageSize;
        if(start >= input.size()) {
            return null;
        }
        return input.subList(start, end);
    }

    /**
     * 根据前后标签获取target在source中的文本片段
     * @param source 原文本
     * @param target 要找的核心词
     * @param preTag 前标签
     * @param postTag 后标签
     * @return
     */
    public static List<String> getMiddleTextFromTags(String source, String target, String preTag, String postTag) {
        List<String> resultList = new ArrayList<>();
        Pattern pattern;
        pattern = Pattern.compile(String.format("([^%s]*%s[^%s]*)", preTag, target, postTag));
        Matcher matcher = pattern.matcher(source);
        while(matcher.find()) {
            String result = matcher.group(1);
            resultList.add(result);
        }
        return resultList;
    }

    /**
     * 根据前后标签获取target在source中的文本片段
     * @param source 原文本
     * @param preTag 前标签
     * @param postTag 后标签
     * @return
     */
    public static List<String> getMiddleTextFromTags(String source, String preTag, String postTag) {
        List<String> resultList = new ArrayList<>();
        Pattern pattern;
        pattern = Pattern.compile(String.format("%s.*?%s", preTag, postTag));
        Matcher matcher = pattern.matcher(source);
        while(matcher.find()) {
            String result = matcher.group(0);
            resultList.add(result);
        }
        return resultList;
    }

    public static void main(String[] args) throws IOException {
        String source = "《我生活的故事》（líng）《海伦·凯勒日记》";
        String preTag = "（";
        String postTag = "）";
        List<String> results = getMiddleTextFromTags(source, preTag, postTag);
        for(String s : results) {
            System.out.println(s);
        }
    }
}
