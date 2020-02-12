package com.ruoyi.lijun.utils;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class OSSClientUtil {

    private String endpoint="oss-cn-beijing.aliyuncs.com";
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public OSSClientUtil(String accessKeyId, String accessKeySecret, String bucketName) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
    }


    /**
     * 上传图片获取fileUrl
     * @return
     */
    public Map<String,Object> uploadFile2OSS(MultipartFile file) throws IOException {
        String ret = "";
        InputStream instream=null;
        instream=file.getInputStream();
        //创建上传Object的Metadata
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(instream.available());
        objectMetadata.setCacheControl("no-cache");
        objectMetadata.setHeader("Pragma", "no-cache");
        String fileName=file.getOriginalFilename();
        String substring = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        objectMetadata.setContentType(getcontentType(substring));
        fileName= Tool.get32UUID()+substring;
        objectMetadata.setContentDisposition("inline;filename=" + fileName);
        //上传文件

        OSS ossClient = new OSSClientBuilder().build("http://"+endpoint, accessKeyId, accessKeySecret);

        PutObjectResult putResult = ossClient.putObject(bucketName, fileName, instream, objectMetadata);

        ossClient.shutdown();
        if (instream != null) {
            instream.close();
        }
        return new ModelMap("url","https://" + bucketName+"."+endpoint+ "/" + fileName).addAttribute("objectName",fileName);
    }

    public static String getcontentType(String FilenameExtension) {
        if (FilenameExtension.equalsIgnoreCase(".bmp")) {
            return "image/bmp";
        }
        if (FilenameExtension.equalsIgnoreCase(".gif")) {
            return "image/gif";
        }
        if (FilenameExtension.equalsIgnoreCase(".jpeg") ||
                FilenameExtension.equalsIgnoreCase(".jpg") ||
                FilenameExtension.equalsIgnoreCase(".png")) {
            return "image/jpeg";
        }
        if (FilenameExtension.equalsIgnoreCase(".html")) {
            return "text/html";
        }
        if (FilenameExtension.equalsIgnoreCase(".txt")) {
            return "text/plain";
        }
        if (FilenameExtension.equalsIgnoreCase(".vsd")) {
            return "application/vnd.visio";
        }
        if (FilenameExtension.equalsIgnoreCase(".pptx") ||
                FilenameExtension.equalsIgnoreCase(".ppt")) {
            return "application/vnd.ms-powerpoint";
        }
        if (FilenameExtension.equalsIgnoreCase(".docx") ||
                FilenameExtension.equalsIgnoreCase(".doc")) {
            return "application/msword";
        }
        if (FilenameExtension.equalsIgnoreCase(".xml")) {
            return "text/xml";
        }
        return "image/jpeg";
    }


    public void deleteFile2OSS(List<String>objectNames){
        if (!Tool.listIsNull(objectNames)) {
            OSS ossClient = new OSSClientBuilder().build("http://"+endpoint, accessKeyId, accessKeySecret);
            DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(objectNames));
            List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();

            ossClient.shutdown();
        }
    }
}
