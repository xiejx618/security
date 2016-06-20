package org.exam.domain.doc;

import org.exam.config.Constants;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by on 16/6/18.
 */
@Document(collection = Constants.TABLE_PREFIX + "session_info")
public class SessionInfo implements Serializable {
    //objectId
    private String id;
    //sessionId
    private String sid;
    //用户标识:比如登录只有用户名,那么用户名也可以作为用户标识,否则
    private String uid;
    private Date lastRequest = new Date();
    private boolean expired = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getLastRequest() {
        return lastRequest;
    }

    public void setLastRequest(Date lastRequest) {
        this.lastRequest = lastRequest;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}
