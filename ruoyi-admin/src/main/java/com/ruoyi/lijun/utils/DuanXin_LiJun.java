package com.ruoyi.lijun.utils;
/**
 * Created by bingone on 15/12/16.
 */

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短信http接口的java代码调用示例
 * 基于Apache HttpClient 4.3
 *
 * @author songchao
 * @since 2015-04-03
 */

public class DuanXin_LiJun {

    //模板发送接口的http地址(单发)
    private static String URI_TPL_SEND_SMS =
            "https://sms.yunpian.com/v2/sms/tpl_single_send.json";

    //模板发送接口的http地址(群发)
    private static String URI_TPL_SEND_SMSS =
            "https://sms.yunpian.com/v2/sms/tpl_batch_send.json";
    //编码格式。发送编码格式统一用UTF-8
    private static String ENCODING = "UTF-8";

    private String APIKEY;
    private String TPL_ID_XXTC;
    public DuanXin_LiJun(String APIKEY, String TPL_ID_XXTC) {
        this.APIKEY = APIKEY;
        this.TPL_ID_XXTC = TPL_ID_XXTC;
    }

    /**
     * 发送验证码
     * @param code    验证码
     * @param phone    　接受的手机号
     * @return json格式字符串
     * @throws IOException
     */
    public String codeSms(String code, String phone) throws IOException {
        Map < String, String > params = new HashMap < String, String > ();
        params.put("apikey", APIKEY);
        params.put("tpl_id",TPL_ID_XXTC);
        params.put("tpl_value",(URLEncoder.encode(("#code#"), ENCODING)+"="+URLEncoder.encode(code, ENCODING)) );
        params.put("mobile", phone);
        return post(URI_TPL_SEND_SMS, params);
    }

    /**
     * 发送通知类短信
     * @param phone 手机号码
     * @param params 可变长参数
     * @return
     * @throws IOException
     */
    public String sendAllSms(String phone, Object... params) throws IOException {
        List<String>paramList=new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            if(params[i]!=null)paramList.add((URLEncoder.encode((("#number"+(i==0?"":i)+"#")), ENCODING)+"="+URLEncoder.encode(params[i].toString(), ENCODING)));
        }
        Map < String, String > sendParams = new HashMap < String, String > ();
        sendParams.put("apikey", APIKEY);
        sendParams.put("tpl_id",TPL_ID_XXTC);
        sendParams.put("tpl_value", StringUtils.join(paramList,"&"));
        sendParams.put("mobile", phone);
        return post(URI_TPL_SEND_SMS, sendParams);
//        return null;
    }
    /**
     * 发送通知类短信
     * @param phones 手机号码,多个逗号分隔
     * @param params 可变长参数
     * @return
     * @throws IOException
     */
    public String sendAllSmses(String phones, Object... params) throws IOException {
        List<String>paramList=new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            if(params[i]!=null)paramList.add((URLEncoder.encode((("#number"+(i==0?"":i)+"#")), ENCODING)+"="+URLEncoder.encode(params[i].toString(), ENCODING)));
        }
        Map < String, String > sendParams = new HashMap < String, String > ();
        sendParams.put("apikey", APIKEY);
        sendParams.put("tpl_id",TPL_ID_XXTC);
        sendParams.put("tpl_value", StringUtils.join(paramList,"&"));
        sendParams.put("mobile", phones);
        return post(URI_TPL_SEND_SMSS, sendParams);
//        return null;
    }
    /**
     * 基于HttpClient 4.3的通用POST方法
     *
     * @param url       提交的URL
     * @param paramsMap 提交<参数，值>Map
     * @return 提交响应
     */

    public static String post(String url, Map < String, String > paramsMap) {
        CloseableHttpClient client = HttpClients.createDefault();
        String responseText = "";
        CloseableHttpResponse response = null;
        try {
            HttpPost method = new HttpPost(url);
            if (paramsMap != null) {
                List < NameValuePair > paramList = new ArrayList <
                        NameValuePair > ();
                for (Map.Entry < String, String > param: paramsMap.entrySet()) {
                    NameValuePair pair = new BasicNameValuePair(param.getKey(),
                            param.getValue());
                    paramList.add(pair);
                }
                method.setEntity(new UrlEncodedFormEntity(paramList,
                        ENCODING));
            }
            response = client.execute(method);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                responseText = EntityUtils.toString(entity, ENCODING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return responseText;
    }
}
