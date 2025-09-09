// com/example/cleannetkit/data/remote/dto/TodoMapper.java
package com.example.cleannetkit.data.remote.dto;

import com.example.cleannetkit.domain.model.Todo;
import java.util.ArrayList;
import java.util.List;

public final class TodoMapper {
    private TodoMapper() {}

    public static Todo toDomain(TodoDto d) {
        if (d == null) return null;
        return new Todo(d.id, d.userId, d.title, d.completed);
    }

    public static List<Todo> toDomainList(List<TodoDto> list) {
        if (list == null) return java.util.Collections.emptyList();
        List<Todo> out = new ArrayList<>(list.size());
        for (TodoDto d : list) out.add(toDomain(d));
        return out;
    }

    public static TodoDto toDto(Todo t) {
        if (t == null) return null;
        TodoDto d = new TodoDto();
        d.id = t.getId();
        d.userId = t.getUserId();
        d.title = t.getTitle();
        d.completed = t.isCompleted();
        return d;
    }
}
