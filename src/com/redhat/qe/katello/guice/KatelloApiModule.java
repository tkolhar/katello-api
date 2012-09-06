package com.redhat.qe.katello.guice;

import com.google.inject.AbstractModule;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.katello.tasks.impl.KatelloApiTasks;

public class KatelloApiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(KatelloTasks.class).to(KatelloApiTasks.class);
    }

}
