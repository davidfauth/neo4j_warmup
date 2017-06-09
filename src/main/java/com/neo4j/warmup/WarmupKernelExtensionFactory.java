package com.neo4j.warmup;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.impl.spi.KernelContext;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

public class WarmupKernelExtensionFactory extends KernelExtensionFactory<WarmupKernelExtensionFactory.Dependencies> {

    interface Dependencies {
        GraphDatabaseService getGraphDatabaseService();
		GraphDatabaseAPI graphdatabaseAPI();
    }

    @Override
    public Lifecycle newInstance(KernelContext kernelContext, final Dependencies dependencies) throws Throwable {
		GraphDatabaseAPI db = dependencies.graphdatabaseAPI();
        return new LifecycleAdapter() {

            @Override
            public void start() throws Throwable {
                WarmupLifeCycle warmupLifeCycle = new WarmupLifeCycle(dependencies.graphdatabaseAPI());
                warmupLifeCycle.start();
            }
        };
    }


    public WarmupKernelExtensionFactory() {
        super("registerTransactionEventHandler");
    }

}