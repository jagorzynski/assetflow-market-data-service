package com.sothrose.assetflow_market_data_service.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.sothrose.assetflow_market_data_service.model.MarketData;
import com.sothrose.assetflow_market_data_service.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/v1/assetflow/marketdata")
public class MarketDataController {

  private final MarketDataService marketDataService;

  @GetMapping(produces = APPLICATION_JSON_VALUE)
  public MarketData getSingleCryptoPrice(
      @RequestParam String symbol, @RequestParam String currency) {
    return marketDataService.fetchCoinPrice(symbol, currency);
  }
}
