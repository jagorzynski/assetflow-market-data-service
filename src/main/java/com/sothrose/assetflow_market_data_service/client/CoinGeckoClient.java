package com.sothrose.assetflow_market_data_service.client;

import static java.lang.String.format;
import static reactor.core.publisher.Mono.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sothrose.assetflow_market_data_service.exception.ExternalApiException;
import com.sothrose.assetflow_market_data_service.exception.InvalidCoinGeckoApiResponseException;
import com.sothrose.assetflow_market_data_service.exception.InvalidCryptoSymbolException;
import com.sothrose.assetflow_market_data_service.model.CryptoDataDto;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

@Slf4j
@RequiredArgsConstructor
@Component
public class CoinGeckoClient {

  public static final String VS_CURRENCY = "vs_currency";
  public static final String IDS = "ids";
  public static final String COIN_GECKO_COINS_MARKETS_PATH = "/coins/markets";
  private final WebClient coinGeckoWebClient;
  private final ObjectMapper objectMapper;

  public CryptoDataDto fetchCoinPrice(String symbol, String currency) {
    return coinGeckoWebClient
        .get()
        .uri(uriBuilder -> buildCoinGeckoUri(uriBuilder, symbol, currency))
        .retrieve()
        .onStatus(
            HttpStatusCode::is4xxClientError,
            clientResponse ->
                clientResponse
                    .bodyToMono(String.class)
                    .flatMap(
                        errorMessage ->
                            error(
                                new InvalidCryptoSymbolException(
                                    format(
                                        "Invalid symbol: [%s], or unsupported currency: [%s] - [%s]",
                                        symbol, currency, errorMessage)))))
        .onStatus(
            HttpStatusCode::is5xxServerError,
            clientResponse ->
                clientResponse
                    .bodyToMono(String.class)
                    .flatMap(
                        errorMessage ->
                            error(
                                new ExternalApiException("CoinGecko API error: " + errorMessage))))
        .bodyToMono(String.class)
        .map(this::parseResponse)
        .block();
  }

  private CryptoDataDto parseResponse(String responseBody) {
    try {
      List<CryptoDataDto> dtoList = objectMapper.readValue(responseBody, new TypeReference<>() {});
      if (dtoList.isEmpty()) {
        throw new InvalidCoinGeckoApiResponseException(
            "Coin gecko API returned an empty list as a response");
      }
      return dtoList.get(0);
    } catch (JsonProcessingException ex) {
      log.error("Failed to parse coin gecko API response: [{}]", ex.getMessage());
      throw new InvalidCoinGeckoApiResponseException("Failed to parse coin gecko API response", ex);
    }
  }

  private URI buildCoinGeckoUri(UriBuilder uriBuilder, String coinId, String currency) {
    return uriBuilder
        .path(COIN_GECKO_COINS_MARKETS_PATH)
        .queryParam(VS_CURRENCY, currency)
        .queryParam(IDS, coinId)
        .build();
  }
}
