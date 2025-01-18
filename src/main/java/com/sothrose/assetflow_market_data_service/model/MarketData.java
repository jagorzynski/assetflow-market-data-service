package com.sothrose.assetflow_market_data_service.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketData {
  private String symbol;
  private AssetType assetType;
  private BigDecimal price;
  private String currency;
  private String source;
  private LocalDateTime timestamp;
  private BigDecimal marketCap;
  private BigDecimal volume24h;
}
