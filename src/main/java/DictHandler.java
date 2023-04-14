import java.io.*;

public class DictHandler {

    public static void loadUserDict(String path, String outputPath) throws IOException {

        FileWriter fileWriter = new FileWriter(outputPath);
        File file = new File(path);
        FileInputStream is = new FileInputStream(file);
        InputStreamReader reader = new InputStreamReader(is,"UTF-8");
        BufferedReader br = new BufferedReader(reader);
        StringBuilder builder = new StringBuilder();
        while (br.ready()) {
            try {
                String line = br.readLine();
                String[] tokens = line.split("[\t ]+");
                if (tokens.length != 2)
                    continue;
                String word = tokens[0];
                double freq = Double.valueOf(tokens[1]);
                builder.append(line + "\n");
            }
            catch (Exception e) {

            }
        }
        br.close();
        fileWriter.write(builder.toString());
        fileWriter.flush();
        fileWriter.close();
    }

    public static void main(String[] args) throws IOException {
        String basePath = "C:\\Users\\feifei\\Desktop\\Gaokao\\dicts\\";
        File file1 = new File(basePath);
        if(file1.isDirectory()) {
            //获取目录中的所有文件名称
            String[] fileName = file1.list();
            for(String s : fileName) {
                loadUserDict(basePath + s, basePath + s.split("\\.")[0] + "Dict.txt");
            }
        }
    }
}
