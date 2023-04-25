package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class MarketTickerDTO {

    private String symbol;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Double amount;
    private Double vol;
    private Integer count;
    private Double bid;
    private Double bidSize;
    private Double ask;
    private Double askSize;

}
