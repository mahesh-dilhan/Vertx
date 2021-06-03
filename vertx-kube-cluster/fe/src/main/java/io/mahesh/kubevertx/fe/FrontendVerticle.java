package io.mahesh.kubevertx.fe;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class FrontendVerticle extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(FrontendVerticle.class);

  private static final int HTTP_PORT = Integer.parseInt(System.getenv().getOrDefault("HTTP_PORT", "8080"));

  @Override
  public void start() {
    Router router = Router.router(vertx);

    setupRouter(router);

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(HTTP_PORT)
      .onSuccess(server -> log.info("Server started and listening on port {}", server.actualPort()));
  }
  // end::start[]

  // tag::router[]
  private void setupRouter(Router router) {
    router.get("/hello").handler(this::handleHelloRequest);

    router.get("/health").handler(rc -> rc.response().end("OK"));

    Handler<Promise<Status>> procedure = ClusterHealthCheck.createProcedure(vertx, false);
    HealthChecks checks = HealthChecks.create(vertx).register("cluster-health", procedure);
    router.get("/readiness").handler(HealthCheckHandler.createWithHealthChecks(checks));
  }
  // end::router[]

  // tag::handle-request[]
  private void handleHelloRequest(RoutingContext rc) {
    vertx.eventBus().<String>request("greetings", rc.queryParams().get("name"))
      .map(Message::body)
      .onSuccess(reply -> rc.response().end(reply))
      .onFailure(rc::fail);
  }
  // end::handle-request[]

  // tag::main[]
  public static void main(String[] args) {
    Vertx.clusteredVertx(new VertxOptions())
      .compose(vertx -> vertx.deployVerticle(new FrontendVerticle()))
      .onFailure(t -> t.printStackTrace());
  }
  // end::main[]
}
