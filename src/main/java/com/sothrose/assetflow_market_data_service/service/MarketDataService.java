package com.sothrose.assetflow_market_data_service.service;

import com.sothrose.assetflow_market_data_service.client.CoinGeckoClient;
import com.sothrose.assetflow_market_data_service.model.MarketData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MarketDataService {

  public static final String COIN_GECKO = "CoinGecko";
  private final CoinGeckoClient coinGeckoClient;

  public MarketData fetchCoinPrice(String symbol, String currency) {
    return coinGeckoClient.fetchCoinPrice(symbol, currency).toMarketData(currency, COIN_GECKO);
  }
}
