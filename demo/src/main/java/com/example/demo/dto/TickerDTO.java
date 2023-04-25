package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TickerDTO {

    private String symbol;
    private Double buyPrice;
    private String buyPlatForm;
    private Double sellPrice;
    private String sellPlatForm;
}
