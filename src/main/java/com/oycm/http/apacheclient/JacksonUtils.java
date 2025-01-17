package com.oycm.http.apacheclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author ouyangcm
 * create 2024/12/5 14:30
 */
public class JacksonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static JavaType getJavaType(Type type){
        return objectMapper.getTypeFactory().constructType(type);
    }

    /**
     * 对象转json字符串
     * @param object 要转成json的对象
     * @return
     */
    public static String objectToJson(Object object){

        try {
            return objectMapper.writerFor(object.getClass()).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     *
     * @param json json字符串
     * @param clazz 结果类
     * @return json解析结果对象
     * @param <T>
     */
    public static <T> T stringToClass(String json, Class<T> clazz){
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println(objectToJson(new A()));
    }

    static class A {
        private String iEFlag = "1";
        private String dDate = "1";

        public String getiEFlag() {
            return iEFlag;
        }

        public void setiEFlag(String iEFlag) {
            this.iEFlag = iEFlag;
        }

        public String getdDate() {
            return dDate;
        }

        public void setdDate(String dDate) {
            this.dDate = dDate;
        }
    }

}
