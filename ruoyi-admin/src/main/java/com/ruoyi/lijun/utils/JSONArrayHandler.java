package com.ruoyi.lijun.utils;

import net.sf.json.JSONArray;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
@MappedTypes({JSONArray.class})
@MappedJdbcTypes({JdbcType.LONGVARCHAR})
public class JSONArrayHandler implements TypeHandler<JSONArray> {
    /**
     * 获得结果的时候,获取到列名的时候,调度此方法
     * @param rs
     * @param columnName
     * @return
     * @throws SQLException
     */
    @Override
    public JSONArray getResult(ResultSet rs, String columnName) throws SQLException {
        String string = rs.getString(columnName);
        JSONArray jsonArray=JSONArray.fromObject(Tool.isNull(string)?"[]":string);
        return jsonArray;
    }

    /**
     * 获取结果集中的index
     * @param rs
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    @Override
    public JSONArray getResult(ResultSet rs, int columnIndex) throws SQLException {
        String string = rs.getString(columnIndex);
        JSONArray jsonArray=JSONArray.fromObject(Tool.isNull(string)?"[]":string);
        return jsonArray;
    }

    /**
     * 获取结果集中的index
     * @param cs
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    @Override
    public JSONArray getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String string = cs.getString(columnIndex);
        JSONArray jsonArray=JSONArray.fromObject(Tool.isNull(string)?"[]":string);
        return jsonArray;
    }
    /**
     * 用于定义在Mybatis设置参数时该如何把Java类型的参数转换为对应的数据库类型
     * @param ps 当前的PreparedStatement对象
     * @param i 当前参数的位置
     * @param parameter 当前参数的Java对象
     * @param jdbcType 当前参数的数据库类型
     * @throws SQLException
     */
    @Override
    public void setParameter(PreparedStatement ps, int i, JSONArray parameter, JdbcType jdbcType) throws SQLException {
        if(parameter == null){
            ps.setString(i, null);
            return;
        }
        String json = parameter.toString();
        ps.setString(i, json);

    }
}
