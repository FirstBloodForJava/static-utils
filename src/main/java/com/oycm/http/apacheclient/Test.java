package com.oycm.http.apacheclient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ouyangcm
 * create 2024/12/5 10:29
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        String content = "上海中免日上商业有限公司(区内企业)：\n" +
                "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp你单位申报的自主声明，经审核现决定实施口岸检查，请做好相关准备并主动联系洋山港区海关、港务部门办理检查手续。\n" +
                "特此通知。\n" +
                "<table class='gridtable'><tr><td>自主声明编号：</td><td>49I202411221024152</td><td>提运单号：</td><td>wsh002</td><td>运输工具：</td><td>海运</td></tr><tr><td>检查关区：</td><td>洋山特综(2249)</td><td>查验类型：</td><td></td><td></td><td></td></tr><tr><td>其他提示：</td><td colspan='5'></td></tr></table>";
        Map<String,String> queryParams = new HashMap<>();
        queryParams.put("orgCode", "104370");
        queryParams.put("fileIds", "");
        queryParams.put("title", "口岸检查通知 | 关于径予放行自主声明\"49I202411221024152\"的查验通知");
        queryParams.put("noticeType", "1");
        queryParams.put("content", content);

//        String url = "http://192.168.116.13:9100/notice/noticeSet/auto2";
        String url = "http://192.168.8.8:8080/get";

        Map<String,String> requestBody = new HashMap<>();
        requestBody.put("key", "value");


        System.out.println(ApacheHttpClientUtils.sentHttpFormPost(url, null, queryParams));
    }
}
