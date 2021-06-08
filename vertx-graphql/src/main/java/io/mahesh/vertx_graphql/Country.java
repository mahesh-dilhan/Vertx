package io.mahesh.vertx_graphql;

import java.util.Random;

public class Country {

  public Integer positiveCases;
  public String name;
  public boolean completed;

  public Country(String name) {
    positiveCases = new Random().nextInt(1000); // <1>
    this.name = name;
  }
}
