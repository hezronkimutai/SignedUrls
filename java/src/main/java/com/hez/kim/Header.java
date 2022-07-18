package com.hez.kim;

import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;


@Value
 class Header {
    @NonFinal
    @Setter
    private String alg;
}


