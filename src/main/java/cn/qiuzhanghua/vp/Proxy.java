package cn.qiuzhanghua.vp;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.httpproxy.HttpProxy;
import io.vertx.httpproxy.ProxyOptions;

import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.cdimascio.dotenv.Dotenv;

public class Proxy extends VerticleBase {
  private static final Logger logger = LoggerFactory.getLogger(Proxy.class);

  @Override
  public Future<?> start() {

    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    String withRedisString = dotenv.get("SECURE_PROXY_WITH_REDIS", "false");
    boolean withRedis = false;
    RedisAPI api;

    withRedisString = withRedisString.toLowerCase();
    if (withRedisString == "true") {
      withRedis = true;
      String redisUrl = dotenv.get("REDIS_URL", "redis://localhost:6379/0");
      RedisOptions options = new RedisOptions();
      options.setConnectionString(redisUrl);
      Redis redisClient = Redis.createClient(vertx, options);
      api = RedisAPI.api(redisClient);
      redisClient.connect().onSuccess(conn -> {
        logger.info("Connected to Redis");
      }).onFailure(err -> {
        logger.error("Failed to connect to Redis: " + err.getMessage());
      });
    }

    String proxyTarget = dotenv.get("SECURE_PROXY_TARGET", "http://localhost:11435");
    if (proxyTarget.startsWith("http://") || proxyTarget.startsWith("https://")) {
      proxyTarget = proxyTarget.substring(proxyTarget.indexOf("://") + 3);
    }
    int port = 11435;
    String host = "localhost";

    String[] result = proxyTarget.split(":");
    if (result.length > 2) {
      logger.error(proxyTarget + " Error, check your SECURE_PROXY_TARGET");
    } else if (result.length == 2) {
      host = result[0];
      port = Integer.parseInt(result[1]);
    } else {
      host = proxyTarget;
    }

    logger.info("Proxy target: " + proxyTarget);

    String exposedPortString = dotenv.get("SECURE_PROXY_PORT", "11434");
    int exposedPort = Integer.parseInt(exposedPortString);

    HttpClient proxyClient = vertx.createHttpClient();
    HttpProxy proxy = HttpProxy.reverseProxy(new ProxyOptions().setSupportWebSocket(true), proxyClient);
    proxy.origin(port, host);
    HttpServer proxyServer = vertx.createHttpServer();

    return proxyServer.requestHandler(proxy).listen(exposedPort)
        .onSuccess(server -> {
          logger.info("Proxy server started on port {}", exposedPort);
        })
        .onFailure(err -> {
          logger.error("Failed to start proxy server: " + err.getMessage());
        });
  }
}
