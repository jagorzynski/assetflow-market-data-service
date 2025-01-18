package com.sothrose.assetflow_market_data_service.configuration;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class CoinGeckoWebClientConfig {

  @Value("${coin-gecko.url:https://api.coingecko.com/api/v3}")
  private String coinGeckoUrl;
  public static final String COIN_GECKO_API = "coinGeckoApi";

  @Bean(name = "coinGeckoWebClient")
  public WebClient coinGeckoWebClient(
      WebClient.Builder webClientBuilder,
      CircuitBreakerRegistry circuitBreakerRegistry,
      RetryRegistry retryRegistry,
      RateLimiterRegistry rateLimiterRegistry,
      BulkheadRegistry bulkheadRegistry,
      TimeLimiterRegistry timeLimiterRegistry) {

    var circuitBreaker = circuitBreakerRegistry.circuitBreaker(COIN_GECKO_API);
    var retry = retryRegistry.retry(COIN_GECKO_API);
    var rateLimiter = rateLimiterRegistry.rateLimiter(COIN_GECKO_API);
    var bulkhead = bulkheadRegistry.bulkhead(COIN_GECKO_API);
    var timeLimiter = timeLimiterRegistry.timeLimiter(COIN_GECKO_API);

    return webClientBuilder
        .baseUrl(coinGeckoUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .filter(
            (request, next) ->
                Mono.defer(() -> next.exchange(request))
                    .transformDeferred(RetryOperator.of(retry))
                    .transformDeferred(TimeLimiterOperator.of(timeLimiter))
                    .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                    .transformDeferred(RateLimiterOperator.of(rateLimiter))
                    .transformDeferred(BulkheadOperator.of(bulkhead))
                    .onErrorResume(ex -> Mono.just(fallbackClientResponse(ex))))
        .build();
  }

  private ClientResponse fallbackClientResponse(Throwable ex) {
    log.warn(
        "Coin gecko api connection fallback method executed, returning default data, exception: [{}]",
        ex.getMessage());
    var defaultResponse =
        """
    {
        "symbol": "N/A",
        "name": "Unknown",
        "current_price": 0,
        "market_cap": 0,
        "total_volume": 0,
        "high_24h": 0,
        "low_24h": 0
    }
    """;
    return ClientResponse.create(HttpStatus.OK)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(defaultResponse)
        .build();
  }
}
