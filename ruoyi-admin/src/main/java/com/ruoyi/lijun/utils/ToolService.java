//package com.ruoyi.lijun.utils;
//
//import com.ruoyi.jjk.domain.JjkPublishType;
//import com.ruoyi.jjk.domain.JjkTopicType;
//import com.ruoyi.jjk.service.IJjkPublishTypeService;
//import com.ruoyi.jjk.service.IJjkTopicTypeService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service("tool")
//public class ToolService {
//    @Autowired
//    private IJjkPublishTypeService publishTypeService;
//    @Autowired
//    private IJjkTopicTypeService topicTypeService;
//    public List<JjkPublishType> getFirstPublishTypeList(){
//        JjkPublishType publishType=new JjkPublishType();
//        publishType.setPid(0l);
//        return publishTypeService.selectJjkPublishTypeList(publishType);
//    }
//    public List<JjkPublishType> getAllPublishTypeList(){
//        return publishTypeService.selectJjkPublishTypeList(null);
//    }
//    public List<JjkTopicType> getAllTopicTypeList(){
//        return topicTypeService.selectJjkTopicTypeList(null);
//    }
//}
