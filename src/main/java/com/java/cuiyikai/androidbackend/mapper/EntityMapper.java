package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.Entity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.lang.Nullable;

@Mapper
public interface EntityMapper {

    /**
     * Query an entity by its id in database
     * @param id the id you want to query.
     * @return the corresponding {@link Entity}. {@code null} when not exists.
     */
    @Nullable
    @Select("SELECT * FROM entity WHERE id = #{id}")
    Entity queryEntityById(int id);

    /**
     * Insert a new entity to the database.
     * @param json the entity's json.
     */
    @Insert("INSERT INTO entity (json) VALUES (#{json})")
    void insertNewEntity(String json);

    /**
     * Query an entity by its json in database
     * @param json the JSON you want to query.
     * @return the corresponding {@link Entity}. {@code null} when not exists.
     */
    @Nullable
    @Select("SELECT * FROM entity WHERE json = #{json}")
    Entity queryEntityByJson(String json);

}
