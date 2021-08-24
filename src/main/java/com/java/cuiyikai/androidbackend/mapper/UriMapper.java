package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.Uri;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UriMapper {

    @Select("SELECT * FROM uris ORDER BY random() LIMIT 1")
    List<Uri> getRandomUriList();

    @Select("SELECT * FROM uris WHERE subject = #{subject} ORDER BY random() LIMIT 1")
    List<Uri> getRandomUriListBySubject(String subject);

    @Insert("INSERT INTO uris (subject, uri) VALUES (#{subject}, #{uri})")
    int insertNewUri(String subject, String uri);

    @Select("SELECT * FROM uris WHERE uri = #{uri}")
    Uri getUriByUri(String uri);

}
