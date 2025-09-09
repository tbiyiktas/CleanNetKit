// com/example/cleannetkit/domain/posts/PatchPostUseCase.java
package com.example.cleannetkit.domain.posts;

import com.example.cleannetkit.domain.model.Post;
import java.util.Map;
import lib.net.CancellableFuture;

public interface PatchPostUseCase {
    CancellableFuture<Post> handle(Command command);

    class Command {
        private int id;
        private Map<String,Object> patch;

        public Command() {}
        public Command(int id, Map<String,Object> patch) { this.id = id; this.patch = patch; }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public Map<String, Object> getPatch() { return patch; }
        public void setPatch(Map<String, Object> patch) { this.patch = patch; }
    }
}
