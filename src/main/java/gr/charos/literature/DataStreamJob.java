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

package gr.charos.literature;


import gr.charos.literature.dto.Quote;

import org.apache.flink.formats.json.JsonDeserializationSchema;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Optional;
import java.util.Properties;

public class DataStreamJob {

	private static final String DEFAULT_SOURCE_BOOTSTRAP_SERVERS = "localhost:9092";
	private static final String DEFAULT_SOURCE_TOPIC = "quotes-with-author-test2";

	private static final String DEFAULT_SOURCE_GROUP_ID = "flink-authors-group-21";

	private static final String DEFAULT_DESTINATION_BOOTSTRAP_SERVERS = "localhost:9092";
	private static final String DEFAULT_DESTINATION_TOPIC = "sentiment-analysis-results";

	public static void main(String[] args) throws Exception {

		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		Properties kafkaProps = new Properties();

		Config config = getConfig(args);

		kafkaProps.setProperty("bootstrap.servers",config.sourceBootstrapServers());
		kafkaProps.setProperty("group.id", config.sourceGroupId());

		JsonDeserializationSchema<Quote> jsonFormat=new JsonDeserializationSchema<>(Quote.class);

		FlinkKafkaConsumer<Quote> kafkaConsumer = new FlinkKafkaConsumer<>(config.sourceTopic(),jsonFormat, kafkaProps);


		// Execute program, beginning computation.
		env.execute("Author Quote Count Job");
	}

	private static Config getConfig(String[] args) {

		String sourceBootstrapServersArg = null;
		String sourceTopicArg = null;
		String sourceGroupIdArg = null;

		String destinationBootstrapServersArg = null;
		String destinationTopicArg =null;


		if (null != args && args.length > 0) {
			for (int i=0; i<args.length; i++) {
				if ("KAFKA_SOURCE_ENV".equals(args[i])) {
					sourceBootstrapServersArg  = args[i+1];
				}
				if ("KAFKA_SOURCE_TOPIC".equals(args[i])) {
					sourceTopicArg = args[i+1];
				}
				if ("KAFKA_SOURCE_GROUP".equals(args[i])) {
					sourceGroupIdArg = args[i+1];
				}

				if ("KAFKA_DEST_ENV".equals(args[i])) {
					destinationBootstrapServersArg  = args[i+1];
				}
				if ("KAFKA_DEST_TOPIC".equals(args[i])) {
					destinationTopicArg = args[i+1];
				}

			}
		}

		String sourceBootstrapServers =  Optional.ofNullable(sourceBootstrapServersArg).orElse(DEFAULT_SOURCE_BOOTSTRAP_SERVERS);
		String sourceTopic =  Optional.ofNullable(sourceTopicArg).orElse(DEFAULT_SOURCE_TOPIC);
		String sourceGroupId= Optional.ofNullable(sourceGroupIdArg).orElse(DEFAULT_SOURCE_GROUP_ID);

		String destinationBootstrapServers = Optional.ofNullable(destinationBootstrapServersArg).orElse(DEFAULT_DESTINATION_BOOTSTRAP_SERVERS);
		String destinationTopic =Optional.ofNullable(destinationTopicArg).orElse(DEFAULT_DESTINATION_TOPIC);
		return new Config(sourceBootstrapServers,sourceTopic,sourceGroupId,destinationBootstrapServers,destinationTopic);
	}


	record Config(String sourceBootstrapServers,
				  String sourceTopic,
				  String sourceGroupId,
				  String destinationBootstrapServers,
				  String destinationTopic) {}
}
