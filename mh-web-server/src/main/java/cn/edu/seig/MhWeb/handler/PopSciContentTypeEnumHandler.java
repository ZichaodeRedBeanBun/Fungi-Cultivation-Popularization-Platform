package cn.edu.seig.MhWeb.handler;

import cn.edu.seig.MhWeb.enumeration.PopSciContentTypeEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

public class PopSciContentTypeEnumHandler extends BaseTypeHandler<PopSciContentTypeEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PopSciContentTypeEnum parameter, JdbcType jdbcType) throws SQLException {
        // 存入数据库的是code字段
        ps.setString(i, parameter.getCode());
    }

    @Override
    public PopSciContentTypeEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return PopSciContentTypeEnum.getByCode(code);
    }

    @Override
    public PopSciContentTypeEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return PopSciContentTypeEnum.getByCode(code);
    }

    @Override
    public PopSciContentTypeEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return PopSciContentTypeEnum.getByCode(code);
    }
}
