package com.mesilat.bvp;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class Util {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy");
    private static final Pattern RE_HTML_TAG = Pattern.compile("<.+?>");
    

    public static ObjectNode toObjectNode(ObjectMapper mapper, Long id){
        ObjectNode obj = mapper.createObjectNode();
        obj.put("id", id);
        return obj;
    }

    public static ObjectNode toObjectNode(ObjectMapper mapper, Long id, String title){
        ObjectNode obj = mapper.createObjectNode();
        obj.put("id", id);
        obj.put("title", title);
        return obj;
    }

    public static ObjectNode toObjectNodeUser(ObjectMapper mapper, String username, String fullName){
        ObjectNode obj = mapper.createObjectNode();
        obj.put("username", username);
        obj.put("fullName", fullName);
        return obj;
    }

    public static List<String> repeat(String text, int n){
        List<String> result = new ArrayList<>();
        for (int i = 0; i < n; i++){
            result.add(text);
        }
        return result;
    }

    public static String formatDate(Date d){
        if (d == null){
            return null;
        } else {
            return SDF.format(d);
        }
    }
    public static Date parseDate(String d) throws ParseException{
        if (d == null){
            return null;
        } else {
            return SDF.parse(d);
        }
    }

    public static String getTextFromHtml(String html){
        Matcher m = RE_HTML_TAG.matcher(html);
        StringBuffer sb = new StringBuffer(html.length());
        while (m.find()) {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        return sb.toString().trim();
    }

    public static String getText(JsonNode node, String ...path){
        for (int i = 0; i < path.length; i++){
            if (node == null || node.isNull()){
                return null;
            } else {
                node = node.get(path[i]);
            }
        }
        if (node == null || node.isNull()){
            return null;
        } else {
            return node.asText();
        }
    }

    public static Long getLong(JsonNode node, String ...path){
        for (int i = 0; i < path.length; i++){
            if (node == null || node.isNull()){
                return null;
            } else {
                node = node.get(path[i]);
            }
        }
        if (node == null || node.isNull()){
            return null;
        } else if (!node.isNumber()) {
            return null;
        } else {
            return node.asLong();
        }
    }

    public static Double getDouble(JsonNode node, String ...path){
        for (int i = 0; i < path.length; i++){
            if (node == null || node.isNull()){
                return null;
            } else {
                node = node.get(path[i]);
            }
        }
        if (node == null || node.isNull()){
            return null;
        } else if (node.isTextual()) {
            try {
                return Double.parseDouble(node.asText());
            } catch (NumberFormatException t) {
                return null;
            }
        } else if (!node.isNumber()) {
            return null;
        } else {
            return node.asDouble();
        }
    }

    public static Date getDate(SimpleDateFormat format, JsonNode node, String ...path) throws ParseException{
        for (int i = 0; i < path.length; i++){
            if (node == null || node.isNull()){
                return null;
            } else {
                node = node.get(path[i]);
            }
        }
        if (node == null || node.isNull()){
            return null;
        } else if (node.isObject() && node.has("date")) {
            return getDate(format, node, "date");
        } else {
            return format.parse(node.asText());
        }
    }
    public static String getSerialized(ObjectMapper mapper, JsonNode node, String ...path) throws IOException{
        for (int i = 0; i < path.length; i++){
            if (node == null || node.isNull()){
                return null;
            } else {
                node = node.get(path[i]);
            }
        }
        if (node == null || node.isNull()){
            return null;
        } else {
            return mapper.writeValueAsString(node);
        }
    }
    public static List<JsonNode> getList(JsonNode node, String ...path) {
        List<JsonNode> list = new ArrayList<>();
        for (int i = 0; i < path.length; i++){
            if (node == null || node.isNull())
                return list;
            else
                node = node.get(path[i]);
        }
        if (node == null || node.isNull())
            return list;

        if (node.isArray()) {
            ArrayNode arr = (ArrayNode)node;
            arr.forEach(elt -> list.add(elt));
        } else {
            list.add(node);
        }

        return list;
    }
}