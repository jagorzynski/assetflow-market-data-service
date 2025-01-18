package com.sothrose.assetflow_market_data_service.exception;

public class InvalidCryptoSymbolException extends RuntimeException {
  public InvalidCryptoSymbolException(String message) {
    super(message);
  }
}
