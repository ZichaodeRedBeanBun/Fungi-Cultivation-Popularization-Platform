package cn.edu.seig.MhWeb.handler;

import cn.edu.seig.MhWeb.enumeration.PopSciContentStatusEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.*;

public class PopSciContentStatusEnumHandler extends BaseTypeHandler<PopSciContentStatusEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PopSciContentStatusEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getId());
    }

    @Override
    public PopSciContentStatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int id = rs.getInt(columnName);
        return PopSciContentStatusEnum.getById(id);
    }

    @Override
    public PopSciContentStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int id = rs.getInt(columnIndex);
        return PopSciContentStatusEnum.getById(id);
    }

    @Override
    public PopSciContentStatusEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int id = cs.getInt(columnIndex);
        return PopSciContentStatusEnum.getById(id);
    }
}
