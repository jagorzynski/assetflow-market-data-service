package com.sothrose.assetflow_market_data_service.exception;

public class InvalidCoinGeckoApiResponseException extends RuntimeException {
  public InvalidCoinGeckoApiResponseException(String message) {
    super(message);
  }

  public InvalidCoinGeckoApiResponseException(String message, Throwable cause) {
    super(message, cause);
  }
}
