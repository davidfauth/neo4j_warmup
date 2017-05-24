package org.neo4j.jpmc.warmup;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;

import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WarmupLifeCycle extends LifecycleAdapter {

    private IndexManager indexManager;
    private GraphDatabaseService graphDatabaseService;
    private Config config;
    private Future warmupCreationFuture;

    public WarmupLifeCycle(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    /*@Override
    public void init() throws Throwable {

        System.out.println("HURZ init");

    }*/

    @Override
    public void start() throws Throwable {
            // direct index creation will fail since DB is not yet started,
            // so using a scheduler to run this as soon as DB is up&running
            warmupCreationFuture = new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
					System.out.println("here");
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
                    }
                }

            }, 1, 5, TimeUnit.MILLISECONDS);
        }
}