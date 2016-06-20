package org.exam.repository.mongo;

import org.exam.domain.doc.SessionInfo;

import java.util.List;

/**
 * Created by on 16/6/19.
 */
public interface MongoSessionInfoRepo extends MongoBaseRepo<SessionInfo> {
    SessionInfo findBySid(String sid);
    List<SessionInfo> findByUid(String uid);
    List<SessionInfo> findByUidAndExpired(String uid,boolean expired);
    void deleteBySid(String sid);
}
