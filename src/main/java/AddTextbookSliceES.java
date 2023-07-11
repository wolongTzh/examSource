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
public class AddTextbookSliceES {

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

    public static void addESIRQA() throws IOException {
        String index = "textbook_slice";
        String basePath = "./output";
        File baseList = new File(basePath);
        for(String fileName : baseList.list()) {
            System.out.println("cur file is " + fileName);
            List<String> contentList = CommonUtil.readPlainTextFile(basePath + "/" + fileName);
            for (String content : contentList) {
                IRQA irqa = new IRQA();
                irqa.setAll(content);
                irqa.setAllStand(content);
                IndexResponse indexResponse2 = client.index(b -> b
                        .index(index)
                        .document(irqa)
                );
            }
        }
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
