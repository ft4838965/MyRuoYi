package com.ruoyi.lijun.task;

import com.ruoyi.lijun.utils.FSS;
import com.ruoyi.lijun.utils.HttpClientUtils;
import com.ruoyi.system.service.ISysConfigService;
import net.sf.json.JSONObject;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class AllTask {
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private ISysConfigService configService;

    /**
     * initialDelay 第一次执行时间,设为0,项目启动就执行一次(单位:毫秒)
     * fixedRate 每次间隔多长时间执行(单位:毫秒)
     * @throws Exception
     */
//    @Scheduled(initialDelay=0,fixedRate=7140000)
    public void getAccessToken() throws Exception {
        String xcx_tokenResponseBody= HttpClientUtils.get("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+configService.selectConfigByKey("wx.xcx_appid")+"&secret="+configService.selectConfigByKey("wx.xcx_secret"));
        if(JSONObject.fromObject(xcx_tokenResponseBody).containsKey("access_token")){
            cacheManager.getCache(FSS.CONSTANT).put("xcx_token",JSONObject.fromObject(xcx_tokenResponseBody).getString("access_token"));
            System.err.println("刷新access_token-->"+ cacheManager.getCache(FSS.CONSTANT).get(FSS.XCX_TOKEN));
        }else{
            System.err.println("刷新access_token失败:"+xcx_tokenResponseBody);
        }
    }
}
