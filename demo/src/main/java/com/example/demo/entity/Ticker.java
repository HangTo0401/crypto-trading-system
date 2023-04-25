package com.example.demo.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "T_Ticker")
@NoArgsConstructor
public class Ticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "buyPrice")
    private Double buyPrice;

    @Column(name = "sellPrice")
    private Double sellPrice;

    @Column(name = "buyPlatForm")
    private String buyPlatForm;

    @Column(name = "sellPlatForm")
    private String sellPlatForm;
}
