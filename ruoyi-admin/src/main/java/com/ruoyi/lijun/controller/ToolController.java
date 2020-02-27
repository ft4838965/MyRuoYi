package com.ruoyi.lijun.controller;

import com.alibaba.fastjson.JSON;
import com.ruoyi.common.config.Global;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.lijun.utils.*;
import com.ruoyi.system.mapper.Dao;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.yzyx.domain.Media;
import com.ruoyi.yzyx.service.IMediaService;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 放一些共用接口,可能会被攻击
 */
@Controller
@RequestMapping("/tool")
public class ToolController {
    @Autowired
    private Dao dao;
    @Autowired
    private IMediaService mediaService;
    @Autowired
    private ISysConfigService configService;

    @RequestMapping("getDistrictByPid")
    @ResponseBody
    public AjaxResult getDistrictByPid(String pid){
        return AjaxResult.success(dao.selectBySQL("select * from yzyx_district where district_pid="+ Tool.IFNULL(pid,"0")));
    }


    /**
     * 上传文件后台系统接口,如果有base_id(媒体集ID),就意味着这次上传文件后的信息要统一保存到media表中,否则就把保存路径返给前台,前台自己决定怎么处理
     * @param files
     * @param base_id
     * @return
     */
    @RequestMapping("uploadMediaByBaseId")
    @ResponseBody
    public AjaxResult uploadMediaByBaseId(String base_id, MultipartFile... files){
        try {
            List<Media>medias=new ArrayList<>();
            if (files!=null&&files.length>0) {
                Long maxBaseSort=mediaService.selectMaxSortByBaseId(Tool.IFNULL(base_id,"随便写的值"));
                for (int i=0,len=files.length;i<len;i++) {
                    if (files[i]!=null&&!files[i].isEmpty()) {
                        if(files[i].getContentType().startsWith("image")&&!WXUtils.checkPic(files[i],configService.selectConfigByKey(FSS.XCX_TOKEN)))return AjaxResult.error("图片内容违规");
                        String fileName= FileUploadUtils.upload(Global.getUploadPath(), files[i]);
//                        String url = serverConfig.getUrl() + fileName;//带域名的绝对路径,留着备用
                        Media media=new Media();
                        media.setUrl(fileName);
                        media.setBaseId(base_id);
//                        if (Tool.IFNULL(media.getUrl(), "").endsWith("mp4"))
//                            media.setBase64("data:image/png;base64," + VideoUtil.returnBase64(new File(Tool.IFNULL(media.getUrl(), "").replace("/profile/upload", Global.getUploadPath()))));
                        if(!Tool.isNull(base_id)){
                            media.setBaseSort(maxBaseSort+i+1);
                            mediaService.insertMedia(media);
                        }
                        medias.add(media);
                    }
                }
            }
            return AjaxResult.success(medias);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("服务器错误:"+e.toString());
        }
    }

    /**
     * 上传文件后台系统接口,如果有base_id(媒体集ID),就意味着这次上传文件后的信息要统一保存到media表中,否则就把保存路径返给前台,前台自己决定怎么处理(这里市上传到阿里云)
     * @param files
     * @param base_id
     * @return
     */
    @RequestMapping("uploadMediaByBaseIdToAli")
    @ResponseBody
    public AjaxResult uploadMediaByBaseIdToAli(String base_id, Integer sort,String folder_path, MultipartFile... files){
        try {
            List<Media>medias=new ArrayList<>();
            if (files!=null&&files.length>0) {
                Long maxBaseSort=mediaService.selectMaxSortByBaseId(Tool.IFNULL(base_id,"随便写的值"));
                for (int i=0,len=files.length;i<len;i++) {
                    if(files[i].getContentType().startsWith("image")&&!WXUtils.checkPic(files[i],configService.selectConfigByKey(FSS.XCX_TOKEN)))return AjaxResult.error("图片内容违规");
                    if (files[i]!=null&&!files[i].isEmpty()) {
                        Media media=new Media();
                        BufferedImage bufferedImage=ImageIO.read(files[i].getInputStream());
                        if(bufferedImage!=null){
                            media.setWidth(Long.valueOf(bufferedImage.getWidth()));
                            media.setHeight(Long.valueOf(bufferedImage.getHeight()));
                        }
                        Map<String,Object>result=new OSSClientUtil(configService.selectConfigByKey("ali.access_key_id"),configService.selectConfigByKey("ali.access_key_secret"),configService.selectConfigByKey("ali.bucket_name"),folder_path).uploadFile2OSS(files[i]);
//                        System.err.println(result);
                        media.setUrl(result.get("url").toString());
                        media.setOtherId(result.get("objectName").toString());
                        media.setBaseId(base_id);
                        if(!Tool.isNull(base_id)){
                            media.setBaseSort(Tool.isNull(sort)?(maxBaseSort+i+1):sort);
                            mediaService.insertMedia(media);
                        }
                        medias.add(media);
                    }
                }
            }
            return AjaxResult.success(medias);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("服务器错误:"+e.toString());
        }
    }

    @RequestMapping("updateMedia")
    @ResponseBody
    AjaxResult updateMedia(Long mediaId, String baseId, Long sort,String folder_path, MultipartFile files){
        if(Tool.isNull(mediaId))return AjaxResult.error("缺少ID");
        try {
            Media media=mediaService.selectMediaById(mediaId);
            if(media==null)return AjaxResult.error("要更新的数据不存在,ID:"+mediaId);
            if(files!=null&&!files.isEmpty()){
                if(files.getContentType().startsWith("image")&&!WXUtils.checkPic(files,configService.selectConfigByKey(FSS.XCX_TOKEN)))return AjaxResult.error("图片内容违规");
                BufferedImage bufferedImage=ImageIO.read(files.getInputStream());
                if(bufferedImage!=null){
                    media.setWidth(Long.valueOf(bufferedImage.getWidth()));
                    media.setHeight(Long.valueOf(bufferedImage.getHeight()));
                }
                Map<String,Object>result=new OSSClientUtil(configService.selectConfigByKey("ali.access_key_id"),configService.selectConfigByKey("ali.access_key_secret"),configService.selectConfigByKey("ali.bucket_name"),folder_path).uploadFile2OSS(files);
                if(!Tool.isNull(media.getOtherId()))new OSSClientUtil(configService.selectConfigByKey("ali.access_key_id"),configService.selectConfigByKey("ali.access_key_secret"),configService.selectConfigByKey("ali.bucket_name")).deleteFile2OSS(new ArrayList<>(Arrays.asList(String.valueOf(media.getOtherId()))));
                media.setUrl(result.get("url").toString());
                media.setOtherId(result.get("objectName").toString());
            }
            if(!Tool.isNull(baseId))media.setBaseId(baseId);
            if(!Tool.isNull(sort))media.setBaseSort(sort);
            if(mediaService.updateMedia(media)>0)return AjaxResult.success("更新成功",media);
            else return AjaxResult.error("没有数据被更新");
        } catch (IOException e) {
            e.printStackTrace();
            return AjaxResult.error("服务器错误:"+e.toString());
        }
    }

    @RequestMapping( "deleteMedia")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        List<Map<String,Object>>medias=dao.selectBySQL("select other_id from yzyx_media where media_id in ("+ids+")");
        if(!Tool.listIsNull(medias))new OSSClientUtil(configService.selectConfigByKey("ali.access_key_id"),configService.selectConfigByKey("ali.access_key_secret"),configService.selectConfigByKey("ali.bucket_name")).deleteFile2OSS(medias.stream().filter(media->!Tool.isNull(media.get("other_id"))).map(media->media.get("other_id").toString()).collect(Collectors.toList()));
        int deleteResult=mediaService.deleteMediaByIds(ids);
        if(deleteResult>0)return AjaxResult.success(deleteResult+"条删除成功");
        else return AjaxResult.error("没有数据被删除");
    }

    @RequestMapping("saveMediaSort")
    @ResponseBody
    AjaxResult saveMediaSort(@RequestBody List<Map<String,Object>> medias){
        dao.updateBySQL("update yzyx_media set base_sort=case media_id "+
                medias.parallelStream().map(media->("when "+media.get("mediaId")+" then "+media.get("baseSort"))).collect(Collectors.joining(" "))+
                " end where media_id in ("+medias.parallelStream().map(media->media.get("mediaId").toString()).collect(Collectors.joining(","))+")");
        return AjaxResult.success("保存成功");
    }

    @RequestMapping("drawImg")
    public void drawImg(HttpServletResponse response,Double canvasHeight,Double canvasWidth,Double pixelRatio,String shareUrl,String title,String intro,String page_view,String publish_id,String first_type_id,String community_id,String bannerUrl){
        try {
            if(Tool.isNull(title))title="家家看";
            if(Tool.isNull(intro))intro="社区信息平台";
            if(Tool.isNull(page_view))page_view="0";
            if(Tool.isNull(publish_id)/*||Tool.isNull(title)||Tool.isNull(intro)||Tool.isNull(page_view)*/|| Tool.isNull(community_id)|| Tool.isNull(canvasHeight)|| Tool.isNull(canvasWidth)|| Tool.isNull(pixelRatio)|| Tool.isNull(shareUrl)/*||Tool.isNull(bannerUrl)*/)return;
            canvasHeight=canvasHeight*2;canvasWidth=canvasWidth*2;
            BigDecimal tempPixelRatio=new BigDecimal(pixelRatio.toString()).subtract(new BigDecimal("0.19"));
            BigDecimal backgroundWidth=new BigDecimal(canvasWidth.toString()),backgroundHeight=new BigDecimal(canvasHeight.toString()),bannerImgHeight=backgroundHeight.subtract(new BigDecimal(260));
            BufferedImage bufImg = new BufferedImage(backgroundWidth.intValue(), backgroundHeight.intValue(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bufImg.createGraphics();
            g.setColor(Color.white);
            g.fillRect(0,0,backgroundWidth.intValue(),backgroundHeight.intValue());
            //封面图
            Image cover=null;
            if(!Tool.isNull(bannerUrl)&&bannerUrl.startsWith("http")){
                URL cover_url = new URL(bannerUrl);
                HttpURLConnection conn = (HttpURLConnection) cover_url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5 * 1000);
                InputStream inStream = conn.getInputStream();
                cover = ImageIO.read(inStream);
                inStream.close();
            }else cover=ImageIO.read(new File(FSS.getStaticFilePathBySyetem("logo.jpg")));
            BigDecimal ratio=new BigDecimal("1.0"),cover_width=new BigDecimal(cover.getWidth(null)),cover_height=new BigDecimal(cover.getHeight(null));
            BufferedImage cove_buf=new BufferedImage(backgroundWidth.intValue(),bannerImgHeight.intValue(),BufferedImage.TYPE_INT_RGB);
            Graphics2D cove_g=cove_buf.createGraphics();
            boolean is_cross=cover.getWidth(null)>cover.getHeight(null);
            if(is_cross){
                //横图
                ratio=new BigDecimal(cover.getHeight(null)).divide(bannerImgHeight,2,BigDecimal.ROUND_DOWN);
                cover_width=cover_width.divide(ratio,2,BigDecimal.ROUND_DOWN);
                cover_height=cover_height.divide(ratio,2,BigDecimal.ROUND_DOWN);
            }else{
                //竖图
                ratio=new BigDecimal(cover.getWidth(null)).divide(backgroundWidth,2,BigDecimal.ROUND_DOWN);
                cover_width=cover_width.divide(ratio,2,BigDecimal.ROUND_DOWN);
                cover_height=cover_height.divide(ratio,2,BigDecimal.ROUND_DOWN);
            }
            cove_g.drawImage(cover,is_cross?backgroundWidth.subtract(cover_width).divide(new BigDecimal("2"),2,BigDecimal.ROUND_DOWN).intValue():0,
                    !is_cross?bannerImgHeight.subtract(cover_height).divide(new BigDecimal("2"),2,BigDecimal.ROUND_DOWN).intValue():0,
                    cover_width.intValue(),cover_height.intValue(),null);
            cove_g.dispose();
            g.drawImage(cove_buf,0,0,null);

            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="+configService.selectConfigByKey(FSS.XCX_TOKEN));
            httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
            Map<String,Object> param = new HashMap<>();
            param.put("page", shareUrl);//必须是已经发布的小程序页面，例如 “pages/index/index” ,如果不填写这个字段，默认跳主页面
            param.put("scene", publish_id+"+"+community_id+"+"+first_type_id);
            StringEntity entity=new StringEntity(JSON.toJSONString(param));
            entity.setContentType("image/png");
            httpPost.setEntity(entity);
            HttpResponse responses = httpClient.execute(httpPost);
            InputStream in = responses.getEntity().getContent();

//            BufferedImage QRimg=ImageIO.read(new File("D:\\工作\\项目\\暖通售后派单\\小程序\\merchants源码\\static\\img\\ewm.png"));
            BufferedImage QRimg=new BufferedImage(new BigDecimal(180).intValue(),new BigDecimal(180).intValue(),BufferedImage.TYPE_INT_RGB);
            Graphics2D QRg=QRimg.createGraphics();
            QRimg = QRg.getDeviceConfiguration().createCompatibleImage(new BigDecimal(180).intValue(),new BigDecimal(180).intValue(), Transparency.TRANSLUCENT);
            QRg.dispose();
            QRg = QRimg.createGraphics();
//            Image QR = ImageIO.read(new File("D:\\工作\\项目\\暖通售后派单\\小程序\\merchants源码\\static\\img\\ewm.png"));
            Image QR=ImageIO.read(in);
            QRg.drawImage(QR,0,0,new BigDecimal(180).intValue(),new BigDecimal(180).intValue(),null);
            QRg.dispose();
            g.drawImage(QRimg,backgroundWidth.subtract(new BigDecimal(225)).intValue(),bannerImgHeight.add(new BigDecimal(20)).intValue(),null);

            BigDecimal notBannerLeftPadding=new BigDecimal(20);

            BufferedImage LOGOimg=new BufferedImage(new BigDecimal(250).intValue(),new BigDecimal(40).intValue(),BufferedImage.TYPE_INT_RGB);
            Graphics2D LOGOg=LOGOimg.createGraphics();
            LOGOimg = LOGOg.getDeviceConfiguration().createCompatibleImage(new BigDecimal(250).intValue(),new BigDecimal(40).intValue(), Transparency.TRANSLUCENT);
            LOGOg.dispose();
            LOGOg = LOGOimg.createGraphics();
            Image LOGO=ImageIO.read(new File(FSS.getStaticFilePathBySyetem("listFooter.png")));
            LOGOg.drawImage(LOGO,0,0,new BigDecimal(250).intValue(),new BigDecimal(31).intValue(),null);
            LOGOg.dispose();
            g.drawImage(LOGOimg,notBannerLeftPadding.intValue(),bannerImgHeight.add(notBannerLeftPadding).add(new BigDecimal(190)).intValue(),null);


            BufferedImage HOTimg=new BufferedImage(new BigDecimal(25).intValue(),new BigDecimal(30).intValue(),BufferedImage.TYPE_INT_RGB);
            Graphics2D HOTg=HOTimg.createGraphics();
            HOTimg = HOTg.getDeviceConfiguration().createCompatibleImage(new BigDecimal(25).intValue(),new BigDecimal(30).intValue(), Transparency.TRANSLUCENT);
            HOTg.dispose();
            HOTg = HOTimg.createGraphics();
            Image HOT=ImageIO.read(new File(FSS.getStaticFilePathBySyetem("hotNumIcon.png")));
            HOTg.drawImage(HOT,0,0,new BigDecimal(25).intValue(),new BigDecimal(30).intValue(),null);
            HOTg.dispose();
            g.drawImage(HOTimg,notBannerLeftPadding.intValue(),bannerImgHeight.add(notBannerLeftPadding).add(new BigDecimal(125)).intValue(),null);
//            g.setColor(Color.black);

            g.setColor(new Color(0,0,0));
            g.setFont(new Font("微软雅黑", Font.BOLD, new BigDecimal(40).intValue()));
            g.drawString(title.length()>10?(title.substring(0,11)+"..."):title,notBannerLeftPadding.intValue(),bannerImgHeight.add(notBannerLeftPadding).add(new BigDecimal(45)).intValue());
            g.setColor(new Color(102,102,102));
            g.setFont(new Font("微软雅黑",Font.PLAIN,new BigDecimal(24).intValue()));
            g.drawString(intro.length()>14?(intro.substring(0,15)+"..."):intro,notBannerLeftPadding.intValue(),bannerImgHeight.add(notBannerLeftPadding).add(new BigDecimal(98)).intValue());
            g.setColor(new Color(0,0,0));
            g.setFont(new Font("微软雅黑",Font.PLAIN,new BigDecimal(28).intValue()));
            g.drawString(page_view.length()>15?(page_view.substring(0,16)+"..."):page_view,notBannerLeftPadding.add(new BigDecimal(HOTimg.getWidth())).add(new BigDecimal(5)).intValue(),bannerImgHeight.add(notBannerLeftPadding).add(new BigDecimal(150)).intValue());
            g.setFont(new Font("微软雅黑",Font.PLAIN,21));
            g.drawString("长按识别小程序码",backgroundWidth.subtract(new BigDecimal(215)).intValue(),bannerImgHeight.add(new BigDecimal(20)).add(new BigDecimal(QRimg.getHeight())).add(new BigDecimal(25)).intValue());
            g.dispose();

            // 输出图片
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("image/jpeg");
            // 将图像输出到Servlet输出流中。
            ServletOutputStream sos = response.getOutputStream();
            ImageIO.write(bufImg, "jpeg", sos);
            sos.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("imgSecCheck")
    @ResponseBody
    public AjaxResult imgSecCheck(MultipartFile files){
        if(files==null||files.isEmpty())return AjaxResult.error("检查图片文件不能为空");
        if(!WXUtils.checkPic(files,configService.selectConfigByKey(FSS.XCX_TOKEN)))return AjaxResult.error("图片内容违规");
        return AjaxResult.success();
    }
}
