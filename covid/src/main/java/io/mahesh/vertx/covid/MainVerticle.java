package io.mahesh.vertx.covid;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainVerticle {

  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  public static void main(String[] args) {
    // nonclusterdeploy();
    // Vertx.clusteredVertx();
    //Vertx vertx = nonclusterdeployment();

    Vertx.clusteredVertx(new VertxOptions())
            .onSuccess(vertx ->{
              vertx.deployVerticle(new CovidcasesCollector());
                      vertx.eventBus()
                              .<JsonObject>consumer("who.portal", message ->{
                                logger.info("New Covid update....{}", message.body().encodePrettily());
                              });
            }
            ).onFailure(fail -> {
              logger.error("shit..crash");
    });


  }

  private static Vertx nonclusterdeployment() {
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
    return vertx;
  }


}
