import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegularSliceTextUtil {

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
        Pattern pattern = Pattern.compile(String.format("([^%s]*%s[^%s]*)", preTag, target, postTag));
        Matcher matcher = pattern.matcher(source);
        while(matcher.find()) {
            String result = matcher.group(1);
            resultList.add(result);
        }
        return resultList;
    }
}
