package model;

import com.alibaba.fastjson.JSONObject;

public class ExamSource {

    String searchText;

    String content;

    JSONObject pics;

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public JSONObject getPics() {
        return pics;
    }

    public void setPics(JSONObject pics) {
        this.pics = pics;
    }
}
