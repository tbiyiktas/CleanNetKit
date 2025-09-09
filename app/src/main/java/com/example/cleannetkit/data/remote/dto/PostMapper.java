// com/example/cleannetkit/data/remote/dto/PostMapper.java
package com.example.cleannetkit.data.remote.dto;

import com.example.cleannetkit.domain.model.Post;
import java.util.ArrayList;
import java.util.List;

public final class PostMapper {
    private PostMapper() {}

    public static Post toDomain(PostDto d) {
        if (d == null) return null;
        return new Post(d.id, d.userId, d.title, d.body);
    }

    public static List<Post> toDomainList(List<PostDto> list) {
        if (list == null) return java.util.Collections.emptyList();
        List<Post> out = new ArrayList<>(list.size());
        for (PostDto d : list) out.add(toDomain(d));
        return out;
    }

    public static PostDto toDto(Post p) {
        if (p == null) return null;
        PostDto d = new PostDto();
        d.id = p.getId();
        d.userId = p.getUserId();
        d.title = p.getTitle();
        d.body = p.getBody();
        return d;
    }
}
