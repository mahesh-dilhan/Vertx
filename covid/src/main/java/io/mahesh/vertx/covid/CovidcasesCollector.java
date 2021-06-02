package io.mahesh.vertx.covid;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CovidcasesCollector extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(CovidcasesCollector.class);

  private  List<String> countries = Arrays.asList("USA","SL","IND","PK","AUS");
  private final Random randomcountry = new Random();

  @Override
  public void start(Promise<Void> startPromise)  {
    vertx.setPeriodic(1000, this::updateCountryStats);
    startPromise.complete();
  }

  private void updateCountryStats(Long aLong) {
    int idx = randomcountry.nextInt(countries.size());
    String cntry = countries.get(idx);

    int postivecases = randomcountry.nextInt(1000);
    logger.info("Country {}  "+cntry+ "  stat update:{}" + postivecases);

  }
}
