import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import model.TextBook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Slf4j
public class EpubExtractor {

    public static void readEpub() throws IOException {
        String baseEpubPath = "/data/epub";
        String outPath = "./bookOut.json";
        File file = new File(outPath);
        FileWriter fileWriter = new FileWriter(file.getName());
        List<String> dirList = CommonUtil.readDir(baseEpubPath);
        int count = 0;
        for(String subDir : dirList) {
            count++;
            String curPath = baseEpubPath + "/" + subDir;
            List<String> coverMsg = CommonUtil.readPlainTextFile(curPath + "/Content.opf");
            TextBook textBook = extractBaseMsg(coverMsg);
            for(String htmlName : CommonUtil.readDir(curPath + "/Text")) {
                if(!htmlName.contains("Chapter") || htmlName.contains(".swp")) {
                    continue;
                }
                textBook.setHtmlName("./epub/" + subDir + "/Text/" + htmlName);
                List<String> content = CommonUtil.readPlainTextFile(curPath + "/Text/" + htmlName);
                textBook.setHtml(String.join("", content));
                fileWriter.write(JSON.toJSONString(textBook) + "\n");
                fileWriter.flush();
            }
            log.info("current progress: " + count + "/" + dirList.size());
        }
        fileWriter.close();
    }

    public static TextBook extractBaseMsg(List<String> coverMsg) {
        TextBook textBook = new TextBook();
        for(String msg : coverMsg) {
            if(msg.contains("dc:title")) {
                textBook.setBookName(RegularSliceTextUtil.getHtmlTagInner(msg));
            }
            else if(msg.contains("dc:subject")) {
                textBook.setSubject(RegularSliceTextUtil.getHtmlTagInner(msg));
            }
            else if(msg.contains("urn:uuid:")) {
                textBook.setIsbn(RegularSliceTextUtil.getHtmlTagInner(msg).split("urn:uuid:")[1]);
            }
            else if(msg.contains("kt-version")) {
                textBook.setEdition(msg.split("content=\"")[1].split("\"")[0]);
            }
            else if(msg.contains("kt-date")) {
                textBook.setEditionTime(msg.split("content=\"")[1].split("\"")[0]);
            }
        }
        return textBook;
    }

    public static void main(String[] args) throws IOException {
        readEpub();
    }
}
