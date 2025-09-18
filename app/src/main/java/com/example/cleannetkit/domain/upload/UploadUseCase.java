// com/example/cleannetkit/application/UploadUseCase.java
package com.example.cleannetkit.domain.upload;

import java.io.File;
import java.util.Map;
import lib.concurrent.CancellableFuture;
import com.example.cleannetkit.application.api.UploadApi.UploadResponse;

public interface UploadUseCase {
    CancellableFuture<UploadResponse> handle(Command c);

    class Command {
        private File file;
        private Map<String,String> fields;
        private Map<String,String> headers;

        public Command() {}
        public Command(File file, Map<String,String> fields, Map<String,String> headers){
            this.file=file; this.fields=fields; this.headers=headers;
        }
        public File getFile(){ return file; }
        public void setFile(File f){ this.file=f; }
        public Map<String,String> getFields(){ return fields; }
        public void setFields(Map<String,String> m){ this.fields=m; }
        public Map<String,String> getHeaders(){ return headers; }
        public void setHeaders(Map<String,String> h){ this.headers=h; }
    }
}
