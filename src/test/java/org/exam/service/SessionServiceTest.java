package org.exam.service;

import org.exam.config.AppConfig;
import org.exam.domain.doc.SessionInfo;
import org.exam.repository.mongo.MongoSessionInfoRepo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by on 16/6/18.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
public class SessionServiceTest {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MongoSessionInfoRepo mongoSessionInfoRepo;

    @Test
    public void find(){
        Query query = new Query(Criteria.where("sid").is("1d1ccc43-c103-497b-9611-563952433de0"));
        SessionInfo info = mongoTemplate.findOne(query, SessionInfo.class, "t_session_info");
        System.out.println(info);
    }
    @Test
    public void truncate(){
        mongoSessionInfoRepo.deleteAll();
    }

    @Test
    public void addSessionInfo() {
        List<SessionInfo> list=new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            SessionInfo session = new SessionInfo();
            session.setSid(UUID.randomUUID().toString());
            session.setUid("user"+i%9);
            list.add(session);
        }
        mongoSessionInfoRepo.save(list);
    }


}