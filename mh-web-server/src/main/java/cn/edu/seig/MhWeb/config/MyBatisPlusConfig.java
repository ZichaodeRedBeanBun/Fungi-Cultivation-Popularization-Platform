package cn.edu.seig.MhWeb.config;
import cn.edu.seig.MhWeb.enumeration.DiseasePestTypeEnum;
import cn.edu.seig.MhWeb.enumeration.PlantPlanStatusEnum;
import cn.edu.seig.MhWeb.enumeration.PopSciContentStatusEnum;
import cn.edu.seig.MhWeb.handler.DiseasePestTypeEnumHandler;
import cn.edu.seig.MhWeb.handler.PlantPlanStatusEnumTypeHandler;
import cn.edu.seig.MhWeb.handler.PopSciContentStatusEnumHandler;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import cn.edu.seig.MhWeb.enumeration.PopSciContentTypeEnum;
import cn.edu.seig.MhWeb.handler.PopSciContentTypeEnumHandler;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("cn.edu.seig.MhWeb.mapper")
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 设置数据库类型
        return interceptor;
    }
    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return configuration -> {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            typeHandlerRegistry.register(PopSciContentTypeEnum.class, PopSciContentTypeEnumHandler.class);
            typeHandlerRegistry.register(PopSciContentStatusEnum.class, PopSciContentStatusEnumHandler.class);
            typeHandlerRegistry.register(DiseasePestTypeEnum.class, DiseasePestTypeEnumHandler.class);
            typeHandlerRegistry.register(PlantPlanStatusEnum.class, PlantPlanStatusEnumTypeHandler.class);
        };
    }

}

