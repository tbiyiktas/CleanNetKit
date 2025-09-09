// com/example/cleannetkit/data/remote/dto/CommentMapper.java
package com.example.cleannetkit.data.remote.dto;

import com.example.cleannetkit.domain.model.Comment;
import java.util.ArrayList;
import java.util.List;

public final class CommentMapper {
    private CommentMapper() {}

    public static Comment toDomain(CommentDto d) {
        if (d == null) return null;
        Comment c = new Comment();
        c.setId(d.id);
        c.setPostId(d.postId);
        c.setName(d.name);
        c.setEmail(d.email);
        c.setBody(d.body);
        return c;
    }

    public static List<Comment> toDomainList(List<CommentDto> list) {
        if (list == null) return java.util.Collections.emptyList();
        List<Comment> out = new ArrayList<>(list.size());
        for (CommentDto d : list) out.add(toDomain(d));
        return out;
    }
}
