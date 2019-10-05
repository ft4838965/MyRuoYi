package com.ruoyi.lijun.utils;


import java.util.List;
import java.util.Map;

public interface Dao {

  public List<Map<String, Object>> selectBySQL(String sql);

  public int insertBySQL(String sql);

  public int updateBySQL(String sql);

  public int deleteBySQL(String sql);
}
