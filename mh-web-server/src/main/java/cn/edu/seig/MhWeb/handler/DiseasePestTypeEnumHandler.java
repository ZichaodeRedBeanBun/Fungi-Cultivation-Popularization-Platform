package cn.edu.seig.MhWeb.handler;

import cn.edu.seig.MhWeb.enumeration.DiseasePestTypeEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

public class DiseasePestTypeEnumHandler extends BaseTypeHandler<DiseasePestTypeEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, DiseasePestTypeEnum parameter, JdbcType jdbcType) throws SQLException {
        // 存入数据库的是id字段（数字）
        ps.setInt(i, parameter.getId());
    }

    @Override
    public DiseasePestTypeEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int id = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return DiseasePestTypeEnum.getById(id);
    }

    @Override
    public DiseasePestTypeEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int id = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        }
        return DiseasePestTypeEnum.getById(id);
    }

    @Override
    public DiseasePestTypeEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int id = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return null;
        }
        return DiseasePestTypeEnum.getById(id);
    }
}
