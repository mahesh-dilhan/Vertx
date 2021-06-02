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
  private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("HTTP_PORT","8888"));
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
            .listen(PORT)
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
    String name = countries.get(idx);

    int postivecases = randomcountry.nextInt(1000);
   // logger.info("Country {}  "+name+ "  stat update:{}" + postivecases);
    Country cntry = new Country(name,postivecases);
    data.put(name, cntry);
    logger.info("{}",cntry.cases+"-"+cntry.name);
    vertx.eventBus().publish("who.portal",payload(cntry));
  }

  private JsonObject payload(Country cntry) {
    return
            new JsonObject()
                    .put("name", cntry.name)
                    .put("cases", cntry.cases);
  }

}
