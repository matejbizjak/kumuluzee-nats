package com.kumuluz.ee.nats.tests;

import com.kumuluz.ee.testing.arquillian.spi.MavenDependencyAppender;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class NatsDependencyAppender implements MavenDependencyAppender {

    private static final ResourceBundle versionsBundle = ResourceBundle.getBundle("META-INF/kumuluzee/versions");

    @Override
    public List<String> addLibraries() {
        List<String> libs = new ArrayList<>();

        libs.add("com.kumuluz.ee:kumuluzee-microProfile-3.3:" + versionsBundle.getString("kumuluzee-version"));
        libs.add("io.nats:jnats:" + versionsBundle.getString("jnats-version"));
        libs.add("com.google.guava:guava:" + versionsBundle.getString("guava-version"));
        libs.add("com.fasterxml.jackson.core:jackson-databind:" + versionsBundle.getString("jackson-version"));
        libs.add("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:" + versionsBundle.getString("jackson-version"));

        return libs;
    }
}
