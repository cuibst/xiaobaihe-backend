package com.java.cuiyikai.androidbackend.mapper;

import com.java.cuiyikai.androidbackend.entity.Uri;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.lang.Nullable;

import java.util.List;

@Mapper
public interface UriMapper {

    /**
     * Get 15 random uris from the database.
     * @return A {@link List} of 15 random {@link Uri}.
     */
    @Select("SELECT * FROM uris ORDER BY (entity_id+10000*random()) DESC LIMIT 15;")
    List<Uri> getRandomUriList();

    /**
     * Get 15 random uris with given subject from the database.
     * @param subject the related subject.
     * @return A {@link List} of 15 random {@link Uri} with the given subject.
     */
    @Select("SELECT * FROM uris WHERE subject = #{subject} ORDER BY (entity_id+10000*random()) DESC LIMIT 15;")
    List<Uri> getRandomUriListBySubject(String subject);

    /**
     * Insert a new uri into the database.
     * @param subject the subject of the uri.
     * @param uri the uri of the entity.
     * @return 1 when success, 0 when failed.
     */
    @Insert("INSERT INTO uris (subject, uri) VALUES (#{subject}, #{uri})")
    int insertNewUri(String subject, String uri);

    /**
     * Get the {@link Uri} from the uri String.
     * @param uri the uri String.
     * @return The related {@link Uri} if exists, otherwise {@code null}.
     */
    @Nullable
    @Select("SELECT * FROM uris WHERE uri = #{uri}")
    Uri getUriByUri(String uri);

    /**
     * Update a uri's related entity id.
     * @param id the uri's id.
     * @param eid the new entity id.
     */
    @Update("UPDATE uris SET entity_id = #{eid} WHERE id = #{id}")
    void updateUriEntityId(int id, int eid);

}
