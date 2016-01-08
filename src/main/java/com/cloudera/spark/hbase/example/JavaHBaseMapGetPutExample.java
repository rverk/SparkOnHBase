package com.cloudera.spark.hbase.example;

import com.cloudera.spark.hbase.JavaHBaseContext;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JavaHBaseMapGetPutExample {
  public static void main(String args[]) {
    if (args.length == 0) {
      System.out
          .println("JavaHBaseBulkGetExample  {master} {tableName}");
    }

    String master = args[0];
    String tableName = args[1];

    JavaSparkContext jsc = new JavaSparkContext(master,
        "JavaHBaseBulkGetExample");
    jsc.addJar("spark.jar");

    List<byte[]> list = new ArrayList<>();
    list.add(Bytes.toBytes("1"));
    list.add(Bytes.toBytes("2"));
    list.add(Bytes.toBytes("3"));
    list.add(Bytes.toBytes("4"));
    list.add(Bytes.toBytes("5"));

    //All Spark
    JavaRDD<byte[]> rdd = jsc.parallelize(list);

    //All HBase
    Configuration conf = HBaseConfiguration.create();
    conf.addResource(new Path("/etc/hbase/conf/core-site.xml"));
    conf.addResource(new Path("/etc/hbase/conf/hbase-site.xml"));

    //This is me
    JavaHBaseContext hbaseContext = new JavaHBaseContext(jsc, conf);

    //This is me
    hbaseContext.foreachPartition(rdd, null);
    
    hbaseContext.foreach(rdd, new VoidFunction<Tuple2<byte[], Connection>>() {


      public void call(Tuple2<byte[], Connection> t)
          throws Exception {
          Table table1 = t._2().getTable(TableName.valueOf("Foo"));

        byte[] b = t._1();
        Result r = table1.get(new Get(b));
        if (r.getExists()) {
          table1.put(new Put(b));
        }
        
      }
    });
    
  }

  public static class GetFunction implements Function<byte[], Get> {

    private static final long serialVersionUID = 1L;

    public Get call(byte[] v) throws Exception {
      return new Get(v);
    }
  }

  public static class CustomFunction implements VoidFunction<Tuple2<Iterator<byte[]>, Connection>> {

    public void call(Tuple2<Iterator<byte[]>, Connection> t) throws Exception {
      
    }
    
  }
  
}
