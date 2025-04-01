package gr.charos.literature.function;

import gr.charos.literature.dto.AuthorQuotes;
import gr.charos.literature.dto.AuthorQuotesCount;
import gr.charos.literature.dto.Quote;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.triggers.ProcessingTimeTrigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.runtime.operators.windowing.WindowOperator;
import org.apache.flink.streaming.runtime.operators.windowing.functions.InternalIterableProcessWindowFunction;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuoteCountFunctionHarnessTest {

    private KeyedOneInputStreamOperatorTestHarness<String, Quote, AuthorQuotesCount> testHarness;
    private static final TypeInformation<Quote> STRING_INT_TUPLE =
            TypeInformation.of(new TypeHint<>() {});

    @BeforeEach
    void setUp() throws Exception {
        // Use a test harness to simulate the window operator

        ListStateDescriptor<Quote> stateDesc =
                new ListStateDescriptor<>(
                        "window-contents",
                        STRING_INT_TUPLE.createSerializer(new ExecutionConfig()));

        WindowOperator <String,
                        Quote,
                        Iterable<Quote>,
                        AuthorQuotesCount,
                        TimeWindow> windowOperator =

                            new WindowOperator<>(
                                    TumblingEventTimeWindows.of(Duration.ofMillis(100)),
                                    new TimeWindow.Serializer(),
                                    Quote::author,
                                    BasicTypeInfo.STRING_TYPE_INFO.createSerializer(
                                            new ExecutionConfig()),
                                    stateDesc,
                                    new InternalIterableProcessWindowFunction<>(new QuoteCountFunction()),
                                    ProcessingTimeTrigger.create(),
                                    0,
                                    null
                            );

        testHarness = new KeyedOneInputStreamOperatorTestHarness<>(windowOperator, Quote::author, BasicTypeInfo.STRING_TYPE_INFO);

        testHarness.open();
    }


    @Test
    public void testProcessCount() throws Exception {
        // manipulate processing time
        testHarness.setProcessingTime(0);

        // push elements and their timestamp
        testHarness.processElement(
                new StreamRecord<>(new Quote("Orwell","Freedom is the right to tell people what they do not want to hear."),
                        10));
        testHarness.processElement(
                new StreamRecord<>(new Quote("Huxley","After silence, that which comes nearest to expressing the inexpressible is music."),
                        20));
        testHarness.processElement(
                new StreamRecord<>(new Quote("Orwell","Happiness can exist only in acceptance."),
                        50));

        testHarness.processElement(
                new StreamRecord<>(new Quote("Dickens","There are dark shadows on the earth, but its lights are stronger in the contrast."),
                        100));
        testHarness.processElement(
                new StreamRecord<>(new Quote("Steinbeck","Power does not corrupt. Fear corrupts... perhaps the fear of a loss of power."),
                        100));

        testHarness.setProcessingTime(100);

        assertEquals(2, testHarness.getRecordOutput().size());

        long orwellRecordsCount = testHarness.getRecordOutput().stream().filter(p->p.getValue().author().equals("Orwell")).count();
        long huxleyRecordsCount = testHarness.getRecordOutput().stream().filter(p->p.getValue().author().equals("Huxley")).count();
        long dickensRecordsCount = testHarness.getRecordOutput().stream().filter(p->p.getValue().author().equals("Dickens")).count();
        long steinbeckRecordsCount = testHarness.getRecordOutput().stream().filter(p->p.getValue().author().equals("Steinbeck")).count();

        assertEquals(1, orwellRecordsCount);
        assertEquals(1, huxleyRecordsCount);
        assertEquals(0, dickensRecordsCount);
        assertEquals(0, steinbeckRecordsCount);


        int orwellQuotes = testHarness.getRecordOutput().stream().filter(p->p.getValue().author().equals("Orwell")).findFirst().get().getValue().quotesCount();
        int huxleyQuotes = testHarness.getRecordOutput().stream().filter(p->p.getValue().author().equals("Huxley")).findFirst().get().getValue().quotesCount();

        assertEquals(2, orwellQuotes);
        assertEquals(1, huxleyQuotes);

    }



}
