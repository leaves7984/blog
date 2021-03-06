package ch.rasc.capped;

import java.util.Arrays;
import java.util.Date;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.CreateCollectionOptions;

public class ToNormal {

  public static void main(String[] args) {
    try (MongoClient mongoClient = new MongoClient()) {
      MongoDatabase db = mongoClient.getDatabase("test");
      db.drop();

      db.createCollection("log",
          new CreateCollectionOptions().capped(true).sizeInBytes(1024));

      MongoCollection<Document> collection = db.getCollection("log");

      for (int i = 0; i < 1000; i++) {
        Document logMessage = new Document();
        logMessage.append("index", i);
        logMessage.append("message", "User sr");
        logMessage.append("loggedIn", new Date());
        logMessage.append("loggedOut", new Date());
        collection.insertOne(logMessage);
      }

      Document collStats = db.runCommand(new Document("collStats", "log"));
      System.out.println(collStats.get("capped")); // true

      MongoNamespace newName = new MongoNamespace("test", "logOld");
      collection.renameCollection(newName);

      collection = db.getCollection("logOld");
      collection.aggregate(Arrays.asList(Aggregates.out("log")))
          .forEach((Block<Document>) block -> {
          });

      collStats = db.runCommand(new Document("collStats", "log"));
      System.out.println(collStats.get("capped")); // falses

      db.getCollection("logOld").drop();

    }
  }

}
