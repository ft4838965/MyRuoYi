package com.ruoyi.lijun.utils;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WXUtils {
    public static JSONObject getEncryptedData(String encryptedData, String sessionKey, String iv){
        // 被加密的数据
        byte[] dataByte = Base64.decode(encryptedData);
        // 加密秘钥
        byte[] keyByte = Base64.decode(sessionKey);
        // 偏移量
        byte[] ivByte = Base64.decode(iv);

        try {
            // 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
            int base = 16;
            if (keyByte.length % base != 0) {
                int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
                byte[] temp = new byte[groups * base];
                Arrays.fill(temp, (byte) 0);
                System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
                keyByte = temp;
            }
            // 初始化
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding","BC");
            SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(new IvParameterSpec(ivByte));
            cipher.init(Cipher.DECRYPT_MODE, spec, parameters);// 初始化
            byte[] resultByte = cipher.doFinal(dataByte);
            if (null != resultByte && resultByte.length > 0) {
                String result = new String(resultByte, "UTF-8");
                return JSONObject.fromObject(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  微信小程序内容安全-文本
     * @param textConetnt
     * @return
     */
    public static Boolean checkText(String textConetnt,String access_token) {

        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();

            CloseableHttpResponse response = null;

            String accessToken = access_token;
            HttpPost request = new HttpPost("https://api.weixin.qq.com/wxa/msg_sec_check?access_token=" + accessToken);
            request.addHeader("Content-Type", "application/json;charset=UTF-8");

            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("content", textConetnt);
            request.setEntity(new StringEntity(JSONObject.fromObject(paramMap).toString(), ContentType.create("application/json", "utf-8")));


            response = httpclient.execute(request);
            HttpEntity httpEntity = response.getEntity();
            String result = EntityUtils.toString(httpEntity, "UTF-8");// 转成string
            JSONObject jso = JSONObject.fromObject(result);


            Object errcode = jso.get("errcode");
            int errCode = (int) errcode;
            if (errCode == 0) {
                return true;
            } else if (errCode == 87014) {
                System.out.println("内容违规-----------" + textConetnt);
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("----------------调用腾讯内容过滤系统出错------------------");
            return true;
        }
    }

    /**
     *  恶意图片过滤
     * @param multipartFile
     * @return
     */
    public static Boolean checkPic(MultipartFile multipartFile, String accessToken) {
        try {

            CloseableHttpClient httpclient = HttpClients.createDefault();

            CloseableHttpResponse response = null;

            HttpPost request = new HttpPost("https://api.weixin.qq.com/wxa/img_sec_check?access_token=" + accessToken);
            request.addHeader("Content-Type", "application/octet-stream");


            InputStream inputStream = multipartFile.getInputStream();

            byte[] byt = new byte[inputStream.available()];
            inputStream.read(byt);
            request.setEntity(new ByteArrayEntity(byt, ContentType.create("image/jpg")));


            response = httpclient.execute(request);
            HttpEntity httpEntity = response.getEntity();
            String result = EntityUtils.toString(httpEntity, "UTF-8");// 转成string
            JSONObject jso = JSONObject.fromObject(result);
            System.out.println(jso + "-------------验证效果");


            Object errcode = jso.get("errcode");
            int errCode = (int) errcode;
            if (errCode == 0) {
                return true;
            } else if (errCode == 87014) {
                System.out.println("图片内容违规-----------");
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("----------------调用腾讯内容过滤系统出错------------------");
            return true;
        }
    }

    public static String errCodeToString(Object code){
        if(Tool.isNull(code))return "(没有获取到错误代码)";
        switch (code.toString()){
            case "40163":return "code被重复使用";
            case "40029":return "不合法的code";
            case "40125":return "小程序配置不正确";
            default:return "未知错误码:"+code;
        }
    }
}
