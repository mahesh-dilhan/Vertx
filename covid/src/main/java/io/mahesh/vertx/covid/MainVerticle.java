package io.mahesh.vertx.covid;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainVerticle {

  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  public static void main(String[] args) {
    // nonclusterdeploy();
    // Vertx.clusteredVertx();
    Vertx vertx = Vertx.vertx();

    DeploymentOptions dop = new DeploymentOptions()
            .setInstances(1);
    vertx.deployVerticle(CovidcasesCollector.class.getName(), dop, (event) -> {
      if (!event.failed()) {
        logger.info("started......");
      } else {
        event.cause().printStackTrace();
      }
    });
  }

}
