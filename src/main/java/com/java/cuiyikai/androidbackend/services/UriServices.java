package com.java.cuiyikai.androidbackend.services;

import com.java.cuiyikai.androidbackend.entity.Entity;
import com.java.cuiyikai.androidbackend.entity.Uri;
import com.java.cuiyikai.androidbackend.mapper.EntityMapper;
import com.java.cuiyikai.androidbackend.mapper.UriMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UriServices {

    @Autowired
    UriMapper uriMapper;

    @Autowired
    EntityMapper entityMapper;

    public List<Uri> getRandomUri() {
        return uriMapper.getRandomUriList();
    }

    public List<Uri> getRandomUriBySubject(String subject) {
        return uriMapper.getRandomUriListBySubject(subject);
    }

    public int insertNewUri(Uri uri) {
        Uri check = uriMapper.getUriByUri(uri.getUri());
        if(check != null)
            return 0;
        return uriMapper.insertNewUri(uri.getSubject(), uri.getUri());
    }

    public void updateEntityId(int id, int eid) {
        uriMapper.updateUriEntityId(id, eid);
    }

    public Entity getEntityById(int id) {
        return entityMapper.queryEntityById(id);
    }

    public Entity getEntityByJson(String json) {
        return entityMapper.queryEntityByJson(json);
    }

    public void insertNewEntity(String json) {
        entityMapper.insertNewEntity(json);
    }
}
