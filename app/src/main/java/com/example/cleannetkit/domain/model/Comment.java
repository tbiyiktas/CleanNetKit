// com/example/cleannetkit/application/model/Comment.java
package com.example.cleannetkit.domain.model;

public class Comment {
    private Integer id;
    private Integer postId;
    private String name;
    private String email;
    private String body;

    public Integer getPostId() { return postId; }
    public void setPostId(Integer postId) { this.postId = postId; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    @Override
    public String toString() {
        return "Comment{" +
                " id=" + id +
                ", postId=" + postId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
