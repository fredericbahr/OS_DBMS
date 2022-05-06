package org.myorg.quickstart;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

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
public class SessionWindowDemo {
    public static void main(String[] args) throws Exception {

        /**
         * https://blog.knoldus.com/flink-implementing-the-session-window/
         * type nc -lk 9000 in console
         * Input:
         * Lary,Home, 3
         * Lary,Home, 4
         * *wait 10 seconds*
         * Lary,Products,5
         * Lary,Products,6
         * *wait 8 seconds*
         * Lary,Products,7
         *
         * Output
         * (Lary,Home,4) after 10 seconds of inactivity
         * (Lary,Products,7) after 10 seconds of inactivity after last insert
         */
        StreamExecutionEnvironment executionEnvironment =
                StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> text = executionEnvironment
                .socketTextStream("localhost", 9000, '\n', 6);

        DataStream<Tuple3<String,String, Double>> userClickStream = text.map(row -> {
            String[] fields = row.split(",");
            if (fields.length == 3) {
                return new Tuple3<>(
                        fields[0],
                        fields[1],
                        Double.parseDouble(fields[2])
                );
            }
            throw new Exception("Not valid arg passed");
        }, TypeInformation.of(new TypeHint<Tuple3<String, String, Double>>() {
        }));

        DataStream<Tuple3<String, String, Double>> maxPageVisitTime =
                userClickStream.keyBy(((KeySelector<Tuple3<String, String, Double>,
                                        Tuple2<String, String>>) stringStringDoubleTuple3 ->
                                        new Tuple2<>(stringStringDoubleTuple3.f0, stringStringDoubleTuple3.f1)),
                                TypeInformation.of(new TypeHint<Tuple2<String, String>>() {
                                }))
                        .window(ProcessingTimeSessionWindows.withGap(Time.seconds(10)))
                        .max(2);

        maxPageVisitTime.print();

        executionEnvironment.execute("Session window example.");
    }
}