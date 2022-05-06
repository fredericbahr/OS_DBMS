package org.myorg.quickstart;
import java.io.Serializable;
import java.util.Date;

public class StockPrice implements Serializable {
    public String name;
    public Integer price;
    public Long eventTime;

    public StockPrice() {

    }

    public StockPrice(String name, Integer price, long eventTime) {
        this.name = name;
        this.price = price;
        this.eventTime = eventTime;
    }

    @Override
    public String toString() {
        return "StockPrice: " + name + ", " + price + ", " + eventTime;
    }

    public long getTimeStamp() {
        return this.eventTime;
    }

    public String getName() {
        return name;
    }
}