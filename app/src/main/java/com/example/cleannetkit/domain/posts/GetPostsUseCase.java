// com/example/cleannetkit/domain/posts/GetPostsUseCase.java
package com.example.cleannetkit.domain.posts;

import com.example.cleannetkit.domain.model.Post;
import java.util.List;
import lib.concurrent.CancellableFuture;

public interface GetPostsUseCase {
    CancellableFuture<List<Post>> handle(Command command);

    // opsiyonel filtre (userId); null ise tüm postlar
    class Command {
        private Integer userId;

        public Command() {}
        public Command(Integer userId) { this.userId = userId; }

        public static Command all() { return new Command(null); }
        public static Command byUser(int uid) { return new Command(uid); }

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
    }
}
