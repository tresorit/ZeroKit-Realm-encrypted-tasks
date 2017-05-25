package io.realm.realmtasks.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Permission extends RealmObject {
    @Required
    private String userId;
    private String path;
    private Boolean mayRead;
    private Boolean mayWrite;
    private Boolean mayManage;
    private Date updatedAt;

    public Permission() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getMayRead() {
        return mayRead;
    }

    public void setMayRead(Boolean mayRead) {
        this.mayRead = mayRead;
    }

    public Boolean getMayWrite() {
        return mayWrite;
    }

    public void setMayWrite(Boolean mayWrite) {
        this.mayWrite = mayWrite;
    }

    public Boolean getMayManage() {
        return mayManage;
    }

    public void setMayManage(Boolean mayManage) {
        this.mayManage = mayManage;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}

