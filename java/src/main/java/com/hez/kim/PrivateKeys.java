package com.hez.kim;

import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;


@Value
 class PrivateKeys {
   @NonFinal
   @Setter
   private final  String derStringPrivateKey = "";

   @NonFinal
   @Setter
   private final String pemStringPrivateKey = "";
}
