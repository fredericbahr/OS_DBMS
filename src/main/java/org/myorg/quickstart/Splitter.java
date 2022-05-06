package org.myorg.quickstart;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;

public class Splitter implements FlatMapFunction<StockPrice, Tuple3<Integer, Integer, String>> {

    @Override
    public void flatMap(StockPrice stockPrice, Collector<Tuple3<Integer, Integer, String>> collector) throws Exception {
        collector.collect(new Tuple3<Integer, Integer,String>(1,stockPrice.price, stockPrice.getTimeStamp() + ", "));
    }
}