package cn.edu.seig.MhWeb.handler;

import cn.edu.seig.MhWeb.enumeration.PlantPlanStatusEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 种植计划状态枚举自定义TypeHandler
 * 适配PlantPlanStatusEnum的id字段（数据库存储的数字值）
 */
@MappedTypes(PlantPlanStatusEnum.class) // 声明处理的Java枚举类型
@MappedJdbcTypes(JdbcType.INTEGER)      // 声明处理的JDBC类型（数据库status字段是INT）
public class PlantPlanStatusEnumTypeHandler extends BaseTypeHandler<PlantPlanStatusEnum> {

    /**
     * 插入/更新数据时：将枚举转换为数据库存储的id（数字）
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PlantPlanStatusEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getId()); // 取枚举的id字段（对应数据库1/2/3/4）
    }

    /**
     * 查询时：从ResultSet按列名获取数字id，转换为枚举
     */
    @Override
    public PlantPlanStatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Integer id = rs.getInt(columnName);
        // 调用你枚举的getById方法，匹配数据库数值
        return rs.wasNull() ? null : PlantPlanStatusEnum.getById(id);
    }

    /**
     * 查询时：从ResultSet按列索引获取数字id，转换为枚举
     */
    @Override
    public PlantPlanStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Integer id = rs.getInt(columnIndex);
        return rs.wasNull() ? null : PlantPlanStatusEnum.getById(id);
    }

    /**
     * 存储过程调用时：获取数字id转换为枚举
     */
    @Override
    public PlantPlanStatusEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Integer id = cs.getInt(columnIndex);
        return cs.wasNull() ? null : PlantPlanStatusEnum.getById(id);
    }
}