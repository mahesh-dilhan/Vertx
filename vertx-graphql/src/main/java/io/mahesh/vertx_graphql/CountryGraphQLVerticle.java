package io.mahesh.vertx_graphql;

import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class CountryGraphQLVerticle extends AbstractVerticle {
  // tag::start[]
  private Map<String, Country> tasks;

  @Override
  public void start() {
    tasks = initData();

    GraphQL graphQL = setupGraphQL();
    GraphQLHandler graphQLHandler = GraphQLHandler.create(graphQL); // <1>

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create()); // <2>
    router.route("/graphql").handler(graphQLHandler); // <3>

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8080);
  }
  // end::start[]

  // tag::initData[]
  private Map<String, Country> initData() {
    Stream<Country> stream = Stream.<Country>builder()
      .add(new Country("USA"))
      .add(new Country("SG"))
      .add(new Country("IND"))
      .build();

    return stream.collect(toMap(country -> country.name, country -> country));
  }
  // end::initData[]

  // tag::setupGraphQL[]
  private GraphQL setupGraphQL() {
    String schema = vertx.fileSystem().readFileBlocking("countries.graphqls").toString(); // <1>

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema); // <2>

    RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring() // <3>
      .type("Query", builder -> builder.dataFetcher("allCountries", this::allCountries))
      .type("Mutation", builder -> builder.dataFetcher("complete", this::complete))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring); // <4>

    return GraphQL.newGraphQL(graphQLSchema).build(); // <5>
  }
  // end::setupGraphQL[]

  // tag::allTasks[]
  private List<Country> allCountries(DataFetchingEnvironment env) {
    boolean uncompletedOnly = env.getArgument("uncompletedOnly");
    return tasks.values().stream()
      .filter(country -> !uncompletedOnly || !country.completed)
      .collect(toList());
  }
  // end::allTasks[]

  // tag::complete[]
  private boolean complete(DataFetchingEnvironment env) {
    String id = env.getArgument("id");
    Country country = tasks.get(id);
    if (country == null) {
      return false;
    }
    country.completed = true;
    return true;
  }
  // end::complete[]

  // tag::main[]
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx(); // <1>
    vertx.deployVerticle(new CountryGraphQLVerticle()); // <2>
  }
  // end::main[]
}
