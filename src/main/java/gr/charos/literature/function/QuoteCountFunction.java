package gr.charos.literature.function;

import gr.charos.literature.dto.AuthorQuotes;
import gr.charos.literature.dto.AuthorQuotesCount;
import gr.charos.literature.dto.Quote;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

public class QuoteCountFunction extends ProcessWindowFunction<Quote, AuthorQuotesCount, String, TimeWindow> {
    private transient ValueState<AuthorQuotes> currentState;

    private final StateTtlConfig ttlConfig = StateTtlConfig
            .newBuilder(Duration.ofDays(1)) // Keep last day
            .setUpdateType(StateTtlConfig.UpdateType.OnReadAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .cleanupInRocksdbCompactFilter(1000)
            .build();

    @Override
    public void open(OpenContext openContext) {
        ValueStateDescriptor<AuthorQuotes> mState =
                new ValueStateDescriptor<>("state", AuthorQuotes.class);
        mState.enableTimeToLive(ttlConfig);
        currentState = getRuntimeContext().getState(mState);

    }
    @Override
    public void process(String key,
                        Context context,
                        Iterable<Quote> elements,
                        Collector<AuthorQuotesCount> out) throws Exception {
        AuthorQuotes  current = currentState.value();
        if (current == null) {
            current = new AuthorQuotes(key);
        }

        for (Quote element : elements) {
            current.getQuotes().add(element.quote());
        }

        out.collect(new AuthorQuotesCount(key,current.getQuotes().size()));
        currentState.update(current);


    }
}
