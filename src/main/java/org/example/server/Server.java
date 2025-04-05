package org.example.server;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);

        router.route(HttpMethod.POST, "/upload")
                .failureHandler(rc -> {
                    logger.warn("Failure {}", rc.failure(), rc.failure());
                })
                .handler(rc -> {
                    rc.request().setExpectMultipart(true);
                    rc.request().uploadHandler(upload -> {
                        logger.info("File name {}", upload.filename());
                        upload.handler( buffer -> {});
                        upload.endHandler(unused -> logger.info("Finished with file {}", upload.filename()));
                    });
                    rc.request().endHandler( unused -> rc.response().setStatusMessage("Done").setStatusCode(200).end());
                });

        SelfSignedCertificate selfSignedCertificate = SelfSignedCertificate.create();
        var options = new HttpServerOptions()
                .setSsl(true)
                .setKeyCertOptions(new PemKeyCertOptions()
                        .setCertPath(selfSignedCertificate.certificatePath())
                        .setKeyPath(selfSignedCertificate.privateKeyPath())
                );

        HttpServer httpServer = vertx.createHttpServer(options)
                .requestHandler(router)
                .exceptionHandler(t -> logger.error(t.getMessage(), t));

        httpServer.listen(8443)
                .onSuccess( s -> logger.info("Started"))
                .onFailure( e -> {
                   logger.error("Unable to start {}", e.getMessage(), e);
                   vertx.close();
                });
    }
}