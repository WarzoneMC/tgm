package network.warzone.tgm.file;

import kong.unirest.Config;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Jorge on 02/03/2021
 */
public class BytebinUploader {

    private UnirestInstance unirest;
    @Setter @Getter
    private String url;

    public BytebinUploader(String url) {
        this.unirest = new UnirestInstance(new Config().addDefaultHeader("content-type", "application/heap-dump"));
        this.url = url;
    }

    public String upload(Path path) throws IOException {
        return this.unirest.post(url + "/post")
                .body(Files.readAllBytes(path))
                .asJson()
                .getBody()
                .getObject()
                .getString("key");
    }
}
