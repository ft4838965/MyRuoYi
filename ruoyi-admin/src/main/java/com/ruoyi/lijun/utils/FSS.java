package com.ruoyi.lijun.utils;

public class FSS {
    /**
     * 不会过期的缓存名称
     */
    public static final String CONSTANT = "CONSTANT";
    /**
     * 微信wx.login在服务器端维护的登录状态码 不管用不用,1小时失效
     */
    public static final String wxSessionIdCache="wxSessionIdCache";
    /**
     * 手机验证码缓存,不管用不用,1小时过期
     */
    public static final String verificationCode="verificationCode";
    /**
     * 后台设置系统中服务电话的key
     */
    public static final String SERVICE_PHONE = "service.phone";
    /**
     * 项目域名
     */
    public static final String DOMAIN_NAME="6.hsrd.cc";

    public static final String USER_STATE_STOP_MSG="账号已被封停";
    public static final String NEED_SUPERIOR_CODE_MSG="请填写您的邀请人他的邀请码";

    public static final String ABOUT_ME="about_me";

    public static final String FILE_UPLOAD_PATH_LOC="D:/项目测试路径/jjk/fileupload/";
    public static final String FILE_UPLOAD_PATH_ONLINE="/lijun/jjk/fileupload/";

    public static final String OSS_INTEGRALGOODS_PATH="integralGoods";
    public static final String OSS_LIQUOR_PATH="liquor";
    public static final String OSS_ICO_PATH="ico";
    public static final String OSS_BANNER_PATH="banner";
    public static final String OSS_PHOTO_WALL="photoWall";
    public static final String XCX_TOKEN="xcx.access_token";

    /**
     * 除了主键,某些键也是有唯一约束的,这个时候就需要在mybatis报错的时候判断报错内容是否包含这个键
     */
    public enum DICT_LABEL{
        用户邀请码("invitation_code"),
        礼包物品类型("gift_bag_item_type"),
        会员与超级会员订单状态("vip_order_state"),
        超级会员等级类型("super_vip_lv"),
        会员等级类型("vip_lv"),
        礼包领取状态("gift_bag_record_state"),
        礼包类型("gifts_type"),
        收益记录类型("yield_type"),
        时间单位("time_unit"),
        问答位置("ask_position"),
        提现审核("withdrawal_state"),
        积分记录类型("integral_record_type"),
        订单状态("order_state"),
        协议_声明位置("position"),
        广告类型("banner_type"),
        广告位置("banner_osition");

        private final String dictName;

        DICT_LABEL(String dictName)
        {
            this.dictName = dictName;
        }

        public String getDictName()
        {
            return dictName;
        }

    }

    public static String getStaticFilePathBySyetem(String fileName){
        return (System.getProperties().getProperty("os.name").toLowerCase().indexOf("win")>-1?FILE_UPLOAD_PATH_LOC:FILE_UPLOAD_PATH_ONLINE)+fileName;
    }

}
