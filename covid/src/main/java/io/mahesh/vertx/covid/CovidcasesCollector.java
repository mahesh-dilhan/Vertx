package io.mahesh.vertx.covid;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CovidcasesCollector extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(CovidcasesCollector.class);

  private  List<String> countries = Arrays.asList("USA","SL","IND","PK","AUS");
  private final Random randomcountry = new Random();

  private Map<String, Country> data = new Hashtable<>();


  @Override
  public void start(Promise<Void> startPromise)  {
    vertx.setPeriodic(1000, this::updateCountryStats);

    Router router = Router.router(vertx);
    router.get("/list").handler(this::list);

    vertx.createHttpServer()
            .requestHandler(router)
            .listen(8888)
            .onSuccess(ok ->{
              logger.info("Running......server");
              startPromise.complete();
            })
            .onFailure(
                startPromise::fail
            );

    //startPromise.complete();
  }

  class Country {
    String name;
    Integer cases;

    public Country(String cntry, int postivecases) {
      name = cntry;
      cases = postivecases;
    }
  }
  private void list(RoutingContext routingContext) {
    JsonArray list = new JsonArray();
    JsonObject  jsonObject = new JsonObject();
    data.forEach((k, v) ->  {
      list.add(new JsonObject()
      .put(k, v.cases).toString());
    });
    routingContext.response()
            .putHeader("Content-Type","application/json")
            .setStatusCode(200)
            .end(new JsonObject().put("data",list).encode());
  }

  private void updateCountryStats(Long aLong) {
    int idx = randomcountry.nextInt(countries.size());
    String cntry = countries.get(idx);

    int postivecases = randomcountry.nextInt(1000);
    logger.info("Country {}  "+cntry+ "  stat update:{}" + postivecases);
    data.put(cntry,new Country(cntry,postivecases));
  }
}
