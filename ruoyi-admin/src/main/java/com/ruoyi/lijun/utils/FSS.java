package com.ruoyi.lijun.utils;

public class FSS {
    /**
     * 不会过期的缓存名称
     */
    public static final String CONSTANT = "CONSTANT";
    /**
     * 后台设置系统中服务电话的key
     */
    public static final String SERVICE_PHONE = "service.phone";
    /**
     * 项目域名
     */
    public static final String DOMAIN_NAME="6.hsrd.cc";

    public static final String USER_STATE_STOP_MSG="账号已被封停";

    public static final String ABOUT_ME="about_me";

    public static final String FILE_UPLOAD_PATH_LOC="D:/项目测试路径/jjk/fileupload/";
    public static final String FILE_UPLOAD_PATH_ONLINE="/lijun/jjk/fileupload/";

    public static final String XCX_TOKEN="xcx_token";
    public static String getStaticFilePathBySyetem(String fileName){
        return (System.getProperties().getProperty("os.name").toLowerCase().indexOf("win")>-1?FILE_UPLOAD_PATH_LOC:FILE_UPLOAD_PATH_ONLINE)+fileName;
    }
}
