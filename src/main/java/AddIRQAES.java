import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import model.ExamSource;
import model.IRQA;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class AddIRQAES {

    static ElasticsearchClient client;

    public static void init() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "5Vc8fC8kjJNiT=SYZFKF"));
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost("47.94.201.245", 9200, "http"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                        return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });
        RestClient restClient = restClientBuilder.build();
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        client = new ElasticsearchClient(transport);
    }

    public static String searchTextGen(JSONObject jsonObject) {
        String splitTag = "||";
        String searchText = "";
        JSONArray questions = jsonObject.getJSONArray("Questions");
        if(questions == null) {
            return searchText;
        }
        for(int i=0; i<questions.size(); i++) {
            Object alter = questions.getJSONObject(i).get("Question");
            if(alter == null) {
                continue;
            }
            if(alter instanceof String) {
                searchText += alter + splitTag;
            }
            else if(alter instanceof JSONArray) {
                List<String> uniqueQuestion = (List<String>) alter;
                for(String u : uniqueQuestion) {
                    searchText += u + splitTag;
                }
            }
            JSONArray choices = questions.getJSONObject(i).getJSONArray("Choices");
            if(choices == null) {
                continue;
            }
            if(choices.size() == 0) {
                List<String> answers = (List<String>) questions.getJSONObject(i).get("Answer");
                if(answers == null) {
                    continue;
                }
                for(String answer : answers) {
                    searchText += answer + splitTag;
                }
            }
            else {
                for(int j=0; j<choices.size(); j++) {
                    String value = (String) choices.getJSONObject(j).get("value");
                    if(value == null) {
                        continue;
                    }
                    searchText += value + splitTag;
                }
            }
        }
        return searchText;
    }

    public static void addES() throws IOException {
        String index = "examsource";
        Map<String, JSONObject> map = ContentExtractor.readRawJson();
        for(Map.Entry entry : map.entrySet()) {
            String name = entry.getKey().toString();
            JSONObject content = (JSONObject) entry.getValue();
            String subject = (String) content.get("Subject");
            //TODO:暂时去除英语试题，英语试题的格式与其它学科略有出入，如果后续需要加上需要对格式做相应特殊处理。
            if(subject.equals("english")) {
                continue;
            }
            String searchText = searchTextGen(content);
            ExamSource examSource = new ExamSource();
            examSource.setContent(JSON.toJSONString(content));
            if(searchText != null && !searchText.equals("")) {
                examSource.setSearchText(searchText);
            }
            IndexResponse indexResponse2 = client.index(b -> b
                    .index(index)
                    .id(name)
                    .document(examSource)
            );
        }
    }

    public static void addESIRQA() throws IOException {
        String index = "irqa_all_stand";
        String outPath = "./outProgress.txt";
        File outFile = new File(outPath);
        FileWriter fileWriter = new FileWriter(outFile, false);
        List<String> pathList = Arrays.asList("./biology", "./chemistry", "./english", "./geo", "./history", "./physics", "./politics", "./chinese", "./all", "old");
        int acc = 0;
        for(String path : pathList) {
            File file = new File(path);
            if(file.isDirectory()) {
                String[] names = file.list();
                for(String name : names) {
                    name = path + "/" + name;
                    List<String> contents = CommonUtil.readPlainTextFile(name);
                    int count = 0;
                    for(String content : contents) {
                        count++;
                        acc++;
                        IRQA irqa = JSON.parseObject(content, IRQA.class);
                        IndexResponse indexResponse2 = client.index(b -> b
                                .index(index)
                                .document(irqa)
                        );
                        if(acc >= 1000) {
                            acc = 0;
                            fileWriter.write("当前：" + name + "，进度：" + count + "/" + contents.size() + "\n");
                            log.info("当前：" + name + "，进度：" + count + "/" + contents.size());
                            fileWriter.flush();
                        }
                    }
                }
            }
        }
        fileWriter.close();
    }





    public static void main(String[] args) throws IOException {
//        String source = "4.十月革命：1917年11月，彼得格勒武装起义推翻了资产阶级临时政府的统治，其他城市的起义也相继取得成功。\",\"width=\\\"750\\\">";
//        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
//        Matcher matcher = pattern.matcher(source);
//        if(!matcher.find()) {
//            System.out.println(matcher.group(0));
//        }
//        String path = "http://47.94.201.245:8081/data1/home/keg/epub/68/Text/Chapter_06_02_古代文化常识.html";
//        URL url = new URL(path);
//        InputStream in = url.openStream();
//
//        System.out.println(1);
        init();
        addESIRQA();
    }
}
