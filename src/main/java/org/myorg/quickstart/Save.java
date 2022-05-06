/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.myorg.quickstart;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssignerSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;

/**
 * Skeleton for a Flink DataStream Job.
 *
 * <p>For a tutorial how to write a Flink application, check the
 * tutorials and examples on the <a href="https://flink.apache.org">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class Save {

    public static Date addSeconds(Date date, Integer seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, seconds);
        return cal.getTime();
    }

    public static void main(String[] args) throws Exception {

        // Sets up the execution environment, which is the main entry point
        // to building Flink applications.
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);


        /**
         * Data stream with Stock Prices
         */
        DataStream stream = env.addSource(new StockMarket())
                .assignTimestampsAndWatermarks(WatermarkStrategy.<StockPrice>forBoundedOutOfOrderness(Duration.ofMillis(100)).withTimestampAssigner(
                        new SerializableTimestampAssigner<StockPrice>() {
                            @Override
                            public long extractTimestamp(StockPrice stockPrice, long l) {
                                System.out.println("Stock: " + stockPrice.name + ", Event-Time: " + stockPrice.getTimeStamp() + ", Processing-Time: " + l);
                                return stockPrice.getTimeStamp();
                            }
                        }
                ))
                .flatMap(new Splitter())
                .keyBy(t -> t.f0)
                .window(TumblingEventTimeWindows.of(Time.milliseconds(5)))
                .reduce((t1, t2) -> {
                    t1.f1 += t2.f1;
                    t1.f2 += t2.f2;
                    return t1;
                });

        // Execute program, beginning computation.
        stream.print("Result");

        env.execute("Test");
    }
}