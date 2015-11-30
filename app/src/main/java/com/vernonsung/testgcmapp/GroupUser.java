package com.vernonsung.testgcmapp;

// HTTP body of joining or leaving group requests from users
// Since it's going to convert to JSON format with GSON library parser, use lowercase property names.
public class GroupUser {
    private String instanceid;
    private String groupname;

    public GroupUser() {
    }

    public GroupUser(String instanceid, String groupname) {
        this.instanceid = instanceid;
        this.groupname = groupname;
    }

    public String getInstanceid() {
        return instanceid;
    }

    public void setInstanceid(String instanceid) {
        this.instanceid = instanceid;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }
}
