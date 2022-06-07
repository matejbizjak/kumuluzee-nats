package com.kumuluz.ee.nats.jetstream.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JetStreamSubscriptionManager {
}
