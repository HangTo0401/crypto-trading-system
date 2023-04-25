package com.example.demo.dto;

import java.io.Serializable;
import java.time.Instant;

public interface CacheableDTO<T extends Serializable> extends Serializable {

    T getCachingId();

    Instant getDateModified();
}
