
package com.lister.nestedscrolltest.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface ViewHolderAnnotation {

    int layoutId();

}
