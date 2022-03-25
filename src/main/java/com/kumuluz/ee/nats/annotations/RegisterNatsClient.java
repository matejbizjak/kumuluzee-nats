package com.kumuluz.ee.nats.annotations;

import javax.enterprise.util.Nonbinding;
import java.lang.annotation.*;

/**
 * @author Matej Bizjak
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterNatsClient {

    @Nonbinding String connection() default "";
}
