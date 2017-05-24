package org.neo4j.jpmc.warmup;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;


import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.impl.spi.KernelContext;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class WarmupKernelExtensionFactory extends KernelExtensionFactory<WarmupKernelExtensionFactory.Dependencies> {

    @Override
    public Lifecycle newInstance(KernelContext kernelContext, final Dependencies dependencies) throws Throwable {
        return new LifecycleAdapter() {

            private WarmupLifeCycle handler;
            private ExecutorService executor;
			private Future warmupCreationFuture;
			private GraphDatabaseService graphDatabaseService;

            @Override
            public void start() throws Throwable {
                dependencies.getGraphDatabaseService();
	            handler = new WarmupLifeCycle(dependencies.getGraphDatabaseService());
//				dependencies.getGraphDatabaseService().registerKernelEventHandler(handler);
            	}

            @Override
            public void shutdown() throws Throwable {
                executor.shutdown();
//                dependencies.getGraphDatabaseService().unregisterTransactionEventHandler(handler);
            }
        };
    }

    interface Dependencies {
        GraphDatabaseService getGraphDatabaseService();
    }

    public WarmupKernelExtensionFactory() {
        super("registerTransactionEventHandler");
    }

}