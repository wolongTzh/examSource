import java.util.List;

public class PageUtil {

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
}
