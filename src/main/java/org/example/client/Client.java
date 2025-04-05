package org.example.client;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpResponseHead;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
    public static void main(String[] args) {
        //large file 2GB - 4GB
        String absolutePath = "";

        Vertx vertx = Vertx.vertx();
        HttpClientOptions httpClientOptions = new HttpClientOptions()
                .setKeepAlive(false)
                .setTrustAll(true)
                .setVerifyHost(false);

        HttpClient client = vertx.httpClientBuilder().with(httpClientOptions)
                .build();
        WebClient webClient = WebClient.wrap(client);

        try{
            var form = MultipartForm.create()
                            .binaryFileUpload("tarfile", "ubi-minimal.tar", absolutePath, MimeMapping.getMimeTypeForExtension("tar"));
            HttpResponse<Buffer> statusMessage = webClient.postAbs("https://localhost:8443/upload")
                    .sendMultipartForm(form)
                    .toCompletionStage().toCompletableFuture().get(5, TimeUnit.MINUTES);
            LOGGER.info("Response code : {}, message {}", statusMessage.statusCode(), statusMessage.statusMessage());
        }catch (Throwable e){
            LOGGER.error(e.getMessage(),e);
        }finally {
            vertx.close();
        }
    }
}