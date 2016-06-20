package org.exam.security;

import org.exam.config.Constants;
import org.exam.domain.doc.SessionInfo;
import org.exam.repository.mongo.MongoSessionInfoRepo;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * session的关系管理放在mongodb
 * Created by on 16/6/18.
 */
public class SessionRegistryImpl implements SessionRegistry, ApplicationListener<SessionDestroyedEvent> {

    private final MongoSessionInfoRepo mongoSessionInfoRepo;
    private final MongoTemplate mongoTemplate;

    private static final String C_SESSION_INFO = Constants.TABLE_PREFIX + "session_info";

    public SessionRegistryImpl(MongoSessionInfoRepo mongoSessionInfoRepo, MongoTemplate mongoTemplate) {
        this.mongoSessionInfoRepo = mongoSessionInfoRepo;
        this.mongoTemplate = mongoTemplate;
    }

    private String getUid(Object principal) {
        return (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object> getAllPrincipals() {
        return mongoTemplate.getCollection(C_SESSION_INFO).distinct("uid");
    }

    @Override
    public List<SessionInformation> getAllSessions(Object principal, boolean includeExpiredSessions) {
        String uid = getUid(principal);
        Iterable<SessionInfo> list = includeExpiredSessions ? mongoSessionInfoRepo.findByUid(uid) : mongoSessionInfoRepo.findByUidAndExpired(uid, false);
        List<SessionInformation> result = new ArrayList<>();
        for (SessionInfo info : list) {
            result.add(new SessionInformation(info.getUid(), info.getSid(), info.getLastRequest()));
        }
        return result;
    }

    @Override
    public SessionInformation getSessionInformation(String sessionId) {
        SessionInfo info = mongoSessionInfoRepo.findBySid(sessionId);
        if (info != null) {
            SessionInformation information = new SessionInformation(info.getUid(), info.getSid(), info.getLastRequest());
            if (info.isExpired()) {
                information.expireNow();
            }
            return information;
        } else {
            return null;
        }
    }

    @Override
    public void refreshLastRequest(String sessionId) {
        SessionInfo info = mongoSessionInfoRepo.findBySid(sessionId);
        info.setLastRequest(new Date());
        mongoSessionInfoRepo.save(info);
    }

    @Override
    public void registerNewSession(String sessionId, Object principal) {
        SessionInfo info = new SessionInfo();
        info.setSid(sessionId);
        info.setUid(getUid(principal));
        info.setLastRequest(new Date());
        info.setExpired(false);
        mongoSessionInfoRepo.save(info);
    }

    @Override
    public void removeSessionInformation(String sessionId) {
        mongoSessionInfoRepo.deleteBySid(sessionId);
    }

    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
        removeSessionInformation(event.getId());
    }
}
