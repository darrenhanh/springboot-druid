package com.joinforwin.config;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Log4jFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.alibaba.druid.pool.DruidDataSource;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

@Configuration
public class DruidConfiguration {

    /**
     * 配置statViewServlet
     *
     * @return
     */
    @Bean
    public ServletRegistrationBean druidServlet() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
        Collection<String> urlMappings = new ArrayList<String>();
        urlMappings.add("/druid/*");
        servletRegistrationBean.setUrlMappings(urlMappings);
        servletRegistrationBean.setServlet(new StatViewServlet());
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("allow", "192.168.16.110,127.0.0.1");// IP白名单 (没有配置或者为空，则允许所有访问)
        initParameters.put("deny", "192.168.16.111");// IP黑名单 (存在共同时，deny优先于allow)
        initParameters.put("loginUsername", "user");// 用户名
        initParameters.put("loginPassword", "password");// 密码
        initParameters.put("resetEnable", "false");// 禁用HTML页面上的“Reset All”功能
        servletRegistrationBean.setInitParameters(initParameters);
        // return new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        return servletRegistrationBean;
    }

    /**
     * 配置dataSource
     *
     * @param driver
     * @param url
     * @param username
     * @param password
     * @return
     */
    @Bean
    public DataSource druidDataSource(@Value("${spring.datasource.driver-class-name}") String driver,
                                      @Value("${spring.datasource.url}") String url,
                                      @Value("${spring.datasource.username}") String username,
                                      @Value("${spring.datasource.password}") String password) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(driver);
        druidDataSource.setUrl(url);
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);
        try {
            //每隔30秒将监控数据输出到日志中一次
            druidDataSource.setTimeBetweenLogStatsMillis(300000);
            //合并多个druidDataSource的监控数据
        //    druidDataSource.setUseGlobalDataSourceStat(true);
            //配置proxyFilter(代理过滤器)
            List<Filter> filters = new ArrayList<>();
            filters.add(statFilter());
            filters.add(wallFilter());
            filters.add(log4jFilter());
            druidDataSource.setProxyFilters(filters);
         //   druidDataSource.setFilters("stat, wall");
            druidDataSource.init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return druidDataSource;
    }

    /**
     * @return
     */
    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new WebStatFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        return filterRegistrationBean;
    }

    /**
     * 日志过滤器
     *
     * @return
     */
    @Bean
    public Log4jFilter log4jFilter() {
        Log4jFilter log4jFilter = new Log4jFilter();
        log4jFilter.setResultSetLogEnabled(false);
        return log4jFilter;
    }

    /**
     * 配置wallFilter,基于SQL语义分析实现防御SQL注入攻击，sql语句的填空游戏
     *
     * @return
     */
    @Bean
    public WallFilter wallFilter() {
        WallFilter wallFilter = new WallFilter();
        wallFilter.setDbType("oracle");
        return wallFilter;
    }

    /**
     * 统计监控信息过滤器
     *
     * @return
     */
    @Bean
    public StatFilter statFilter() {
        //sql命令执行10秒以上的log输出
        StatFilter statFilter = new StatFilter();
        statFilter.setSlowSqlMillis(10000);
        statFilter.setLogSlowSql(true);
        //sql合并执行（类似的sql）
        statFilter.setMergeSql(true);
        return statFilter;
    }
}