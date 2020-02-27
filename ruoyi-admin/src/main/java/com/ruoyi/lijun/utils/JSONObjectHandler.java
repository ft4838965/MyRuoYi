package com.ruoyi.lijun.utils;

import net.sf.json.JSONObject;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes({JSONObject.class})
@MappedJdbcTypes({JdbcType.LONGVARCHAR})
public class JSONObjectHandler implements TypeHandler<JSONObject> {

    @Override
    public JSONObject getResult(ResultSet rs, String columnName) throws SQLException {
        String string = rs.getString(columnName);
        JSONObject jsonObject=JSONObject.fromObject(Tool.isNull(string)?"{}":string);
        return jsonObject;
    }

    @Override
    public JSONObject getResult(ResultSet rs, int columnIndex) throws SQLException {
        String string = rs.getString(columnIndex);
        JSONObject jsonObject=JSONObject.fromObject(Tool.isNull(string)?"{}":string);
        return jsonObject;
    }

    @Override
    public JSONObject getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String string = cs.getString(columnIndex);
        JSONObject jsonObject=JSONObject.fromObject(Tool.isNull(string)?"{}":string);
        return jsonObject;
    }
    /**
     * 用于定义在Mybatis设置参数时该如何把Java类型的参数转换为对应的数据库类型
     * @param ps 当前的PreparedStatement对象
     * @param i 当前参数的位置
     * @param parameter 当前参数的Java对象
     * @param jdbcType 当前参数的数据库类型
     * @throws SQLException
     */
    public void setParameter(PreparedStatement ps, int i, JSONObject parameter, JdbcType jdbcType) throws SQLException {
        if(parameter == null){
            ps.setString(i, null);
            return;
        }
        String json = parameter.toString();
        ps.setString(i, json);

    }
}
