package com.kumuluz.ee.nats.jetstream.annotations;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JetStreamProducer {

    @Nonbinding String connection() default "";

//    @Nonbinding String subject() default "";

//    .expectedStream("TEST")
//                .messageId()
//                .expectedLastMsgId()
//                .expectedLastSequence()
//                .expectedLastSubjectSequence()
//                .streamTimeout()
//                .messageId("mid1");
}
