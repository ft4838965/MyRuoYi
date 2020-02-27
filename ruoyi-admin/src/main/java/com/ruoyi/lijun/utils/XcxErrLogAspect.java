package com.ruoyi.lijun.utils;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.enums.BusinessStatus;
import com.ruoyi.common.json.JSON;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.framework.util.ShiroUtils;
import com.ruoyi.system.domain.SysOperLog;
import com.ruoyi.system.domain.SysUser;
import com.ruoyi.yzyx.domain.XcxErrLog;
import com.ruoyi.yzyx.service.IXcxErrLogService;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * 操作日志记录处理
 * 
 * @author ruoyi
 */
@Aspect
@Component
public class XcxErrLogAspect
{
    @Autowired
    private IXcxErrLogService errLogService;

    private static final Logger log = LoggerFactory.getLogger(XcxErrLogAspect.class);

    // 配置织入点
    @Pointcut("@annotation(io.swagger.annotations.ApiOperation)")
    public void logPointCut()
    {
    }

    /**
     * 拦截异常操作
     * 
     * @param joinPoint 切点
     * @param e 异常
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e)
    {
        handleLog(joinPoint, e);
    }

    protected void handleLog(final JoinPoint joinPoint, final Exception e)
    {
        try
        {
            // 获得注解
            ApiOperation apiOperation =

                    getAnnotationLog(joinPoint);
            if (apiOperation == null)
            {
                return;
            }


            // *========数据库日志=========*//
            XcxErrLog errLog = new XcxErrLog();
            // 请求的地址
            String ip = Tool.getIpAddress(ServletUtils.getRequest());
            errLog.setIp(ip);

            errLog.setUrl(ServletUtils.getRequest().getRequestURI());

            if (e != null)
            {
                JSONArray msg=new JSONArray();
                msg.add(e.getMessage());
                new ArrayList<>(Arrays.asList(e.getStackTrace())).stream().filter(stackTraceElement -> stackTraceElement.toString().indexOf("XcxController")>0).forEach(stackTraceElement -> msg.add(stackTraceElement.toString()));
                errLog.setMsg(msg);
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            errLog.setMethod(className + "." + methodName + "()");
            // 设置标题
            errLog.setTitle(apiOperation.value());
            // 保存request，参数和值
            errLog.setParam(JSONObject.fromObject(ServletUtils.getRequest().getParameterMap()));
            // 保存数据库
            errLogService.insertXcxErrLog(errLog);
        }
        catch (Exception exp)
        {
            System.err.println("------------------->小程序错误日志捕捉切面都报错啦,看看吧您嘞↓↓");
            exp.printStackTrace();
        }
    }


    /**
     * 是否存在注解，如果存在就获取
     */
    private ApiOperation getAnnotationLog(JoinPoint joinPoint)
    {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null)
        {
            return method.getAnnotation(ApiOperation.class);
        }
        return null;
    }
}
