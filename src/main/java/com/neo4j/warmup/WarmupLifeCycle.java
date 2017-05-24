package com.neo4j.warmup;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WarmupLifeCycle extends LifecycleAdapter {

    private GraphDatabaseService graphDatabaseService;
    private Config config;
    private Future warmupCreationFuture;

    public WarmupLifeCycle(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    @Override
    public void start() throws Throwable {
			System.out.println("starting");
            // warmup will fail since DB is not yet started,
            // so using a scheduler to run this as soon as DB is up&running
            warmupCreationFuture = new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    System.out.println("warming up");
                    try (Transaction tx = graphDatabaseService.beginTx()) {
						for (Node node : graphDatabaseService.getAllNodes()) {
						     node.getPropertyKeys();
						}
                        tx.success();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        // thrown if index creation fails while startup is still in progress
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        throw e;
                    } finally {
                        warmupCreationFuture.cancel(false);
                    }
                }

            }, 1, 60, TimeUnit.SECONDS);
        }
}