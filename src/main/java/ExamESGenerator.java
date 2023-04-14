import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import model.ExamSource;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExamESGenerator {

    static ElasticsearchClient client;

    public static void init() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
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





    public static void main(String[] args) throws IOException {
//        String source = "4.十月革命：1917年11月，彼得格勒武装起义推翻了资产阶级临时政府的统治，其他城市的起义也相继取得成功。\",\"width=\\\"750\\\">";
//        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
//        Matcher matcher = pattern.matcher(source);
//        if(!matcher.find()) {
//            System.out.println(matcher.group(0));
//        }

        init();
        addES();
    }
}
