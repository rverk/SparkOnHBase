/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License") you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudera.spark.hbase.example

import com.cloudera.spark.hbase.HBaseContext
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.{CellUtil, HBaseConfiguration}
import org.apache.hadoop.hbase.client.{Get, Result}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}

object HBaseBulkGetExample {
  def main(args: Array[String]) {
    if (args.length == 0) {
      System.out.println("HBaseBulkGetExample {tableName}")
      return 
    }

    val tableName = args(0)

    val sparkConf = new SparkConf().setAppName("HBaseBulkGetExample " + tableName)
    val sc = new SparkContext(sparkConf)
      

    //[(Array[Byte])]
    val rdd = sc.parallelize(Array(
      Bytes.toBytes("1"),
      Bytes.toBytes("2"),
      Bytes.toBytes("3"),
      Bytes.toBytes("4"),
      Bytes.toBytes("5"),
      Bytes.toBytes("6"),
      Bytes.toBytes("7")))

    val conf = HBaseConfiguration.create()
    conf.addResource(new Path("/etc/hbase/conf/core-site.xml"))
    conf.addResource(new Path("/etc/hbase/conf/hbase-site.xml"))

    val hbaseContext = new HBaseContext(sc, conf)

    val getRdd = hbaseContext.bulkGet[Array[Byte], String](
      tableName,
      2,
      rdd,
      record => { 
        System.out.println("making Get" )
        new Get(record)
      },
      (result: Result) => {

        val it = result.listCells().iterator()
        val b = new StringBuilder

        b.append(Bytes.toString(result.getRow) + ":")

        while (it.hasNext) {
          val kv = it.next()
          val q = Bytes.toString(CellUtil.cloneQualifier(kv))
          if (q.equals("counter")) {
            b.append("(")
            .append(Bytes.toString(CellUtil.cloneQualifier(kv)))
            .append(",")
            .append(Bytes.toLong(CellUtil.cloneValue(kv)))
            .append(")")
          } else {
            b.append("(")
            .append(Bytes.toString(CellUtil.cloneQualifier(kv)))
            .append(",")
            .append(Bytes.toString(CellUtil.cloneValue(kv)))
            .append(")")
          }
        }
        b.toString()
      })
      
    
    getRdd.collect().foreach(v => System.out.println(v))
    
  }
}