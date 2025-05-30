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

    String redisUrl = dotenv.get("REDIS_URL", "redis://localhost:6379/0");
    RedisOptions options = new RedisOptions();
    options.setConnectionString(redisUrl);
    Redis redisClient = Redis.createClient(vertx, options);
    RedisAPI api = RedisAPI.api(redisClient);
    redisClient.connect().onSuccess(conn -> {
      logger.info("Connected to Redis");
    }).onFailure(err -> {
      logger.error("Failed to connect to Redis: " + err.getMessage());
    });

    HttpClient proxyClient = vertx.createHttpClient();
    HttpProxy proxy = HttpProxy.reverseProxy(new ProxyOptions().setSupportWebSocket(true), proxyClient);
    proxy.origin(11435, "localhost");
    HttpServer proxyServer = vertx.createHttpServer();

    return proxyServer.requestHandler(proxy).listen(11434)
        .onSuccess(server -> {
          logger.info("Proxy server started on port 11434");
        })
        .onFailure(err -> {
          logger.error("Failed to start proxy server: " + err.getMessage());
        });
  }
}
