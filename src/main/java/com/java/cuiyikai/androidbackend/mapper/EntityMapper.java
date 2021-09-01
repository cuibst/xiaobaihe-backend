package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.Entity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EntityMapper {

    @Select("SELECT * FROM entity WHERE id = #{id}")
    Entity queryEntityById(int id);

    @Insert("INSERT INTO entity (json) VALUES (#{json})")
    void insertNewEntity(String json);

    @Select("SELECT * FROM entity WHERE json = #{json}")
    Entity queryEntityByJson(String json);

}
