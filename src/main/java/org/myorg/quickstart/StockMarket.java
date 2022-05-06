package org.myorg.quickstart;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class StockMarket extends RichSourceFunction<StockPrice> {

    boolean isRunning = true;
    long startTime;
    int index = -1;
    StockPrice[] outOfOrderEvents = {
            new StockPrice("AAPL", 10, 2),
            new StockPrice("GOOG", 30, 3),
            new StockPrice("MSFT", 40, 7),
            new StockPrice("TSLA", 20, 1),
            new StockPrice("FB", 50, 8)
    };


    @Override
    public void run(SourceContext<StockPrice> ctx) throws Exception {
        int delayInMilliseconds = 5;
        startTime = System.currentTimeMillis();

        while (isRunning) {
            Tuple2<StockPrice,Long> nextEvent = getNextEvent();
            if (Objects.equals(nextEvent.f0.getName(), "TSLA")) {
                ctx.collectWithTimestamp(nextEvent.f0, delayInMilliseconds + nextEvent.f1);
            } else {
                ctx.collectWithTimestamp(nextEvent.f0, nextEvent.f1);
            }
        }
    }

    @Override
    public void cancel() {
        this.isRunning = false;
    }
    private Date addSeconds(Date date, Integer seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, seconds);
        return cal.getTime();
    }

    private Tuple2<StockPrice,Long> getNextEvent() {
        index++;

        if (index == outOfOrderEvents.length - 1) {
            isRunning = false;
        }
            return new Tuple2<StockPrice, Long>(outOfOrderEvents[index], outOfOrderEvents[index].getTimeStamp());

    }
}
