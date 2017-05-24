package com.neo4j.warmup;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
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
                    System.out.println("Warming up");
					int counter=0;
					int nodeCounter = 0;
					int nodePropertyCounter = 0;
					int relCounter = 0;
					int relPropertyCounter = 0;
					
                    Transaction tx = graphDatabaseService.beginTx();
				      try {
						for (Node node : graphDatabaseService.getAllNodes()) {
						     node.getPropertyKeys();
							 counter ++;
							 nodeCounter ++;
							for (String key : node.getPropertyKeys()) {
								node.getProperty(key);
								nodePropertyCounter ++;			
							}
							if (counter % 10_000 == 0) {
								tx.success();
							    tx.close();
							    tx = graphDatabaseService.beginTx();
							}
						}
						
						for ( Relationship relationship : graphDatabaseService.getAllRelationships()){
						              relationship.getPropertyKeys();
						              relationship.getNodes();
						              counter ++;
									  relCounter ++;
						              for (String key : relationship.getPropertyKeys()) {
						            	  relationship.getProperty(key);
										  relPropertyCounter ++;
						          	  }
						              if (counter % 10_000 == 0) {
						                  tx.success();
						                  tx.close();
						                  tx = graphDatabaseService.beginTx();
						              }
						          }
                        tx.success();
					System.out.println("Warmup complete");
					System.out.println("Nodes: " + nodeCounter);
					System.out.println("Node Properties: " + nodePropertyCounter);
					System.out.println("Relationships: " + relCounter);
					System.out.println("Relationship Properties: " + relPropertyCounter);
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

            }, 60, 60, TimeUnit.SECONDS);
        }
}