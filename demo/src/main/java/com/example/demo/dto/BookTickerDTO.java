package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class BookTickerDTO {

    private String symbol;
    private Double bidPrice;
    private Double bidQty;
    private Double askPrice;
    private Double askQty;
}
