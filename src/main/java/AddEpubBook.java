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
import model.TextBook;
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
public class AddEpubBook {

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

    public static void addEpub() throws IOException {
        String index = "newBook";
        String inputPath = "./bookOut.json";
        List<String> contents = CommonUtil.readPlainTextFile(inputPath);
        int count = 0;
        for(String content : contents) {
            count++;
            TextBook textBook = JSON.parseObject(content, TextBook.class);
            IndexResponse indexResponse2 = client.index(b -> b
                    .index(index)
                    .document(textBook)
            );
            log.info("current progress: " + count + "/" + contents.size());
        }
    }

    public static void main(String[] args) throws IOException {
        init();
        addEpub();
    }
}
