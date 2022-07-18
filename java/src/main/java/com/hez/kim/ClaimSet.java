package com.hez.kim;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
public class ClaimSet {
    @NonFinal
    @Setter
    private String iss;

    @NonFinal
    @Setter
    private String sub;

    @NonFinal
    @Setter
    private String aud;

    @NonFinal
    @Setter
    private int exp;
}
