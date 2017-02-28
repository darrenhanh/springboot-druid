package com.joinforwin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * Created by Darren on 2017/2/27 0027.
 */
@RestController
@Controller
public class OracleService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void queryTest() {
        try {
            List<Map<String,Object>> list = this.jdbcTemplate.queryForList("select * from Table1");
        }catch (Exception e){
            System.out.println(e);
        }
    }

    @RequestMapping("/test")
    public String index() {
        queryTest();
        return "Sql语句执行完成";
    }

}
