# Quotes Job

A Flink-based streaming application for processing quotes data.

## Overview

This project is a Flink streaming application that processes quotes data. It's built using Apache Flink and includes Kafka integration for data streaming.

## Prerequisites

- Java 21
- Maven 3.1+
- Apache Flink 1.20.1
- Apache Kafka (for data streaming)

## Project Structure

```
quotesjob/
├── src/
│   ├── main/         # Main application code
│   └── test/         # Test code
├── pom.xml           # Maven project configuration
└── target/          # Build output directory
```

## Dependencies

The project uses the following main dependencies:
- Apache Flink 1.20.1
- Flink Kafka Connector 3.4.0-1.20
- Log4j 2.17.1 for logging

## Building the Project

To build the project, run:

```bash
mvn clean package
```

This will create a fat JAR in the `target` directory that contains all necessary dependencies.

## Running the Application

The application can be run using the Flink cluster or in local mode. The main class is `gr.charos.literature.DataStreamJob`.

## Configuration

The project uses Maven for dependency management and build configuration. Key configurations can be found in the `pom.xml` file.

## License

This project is licensed under the Apache License, Version 2.0. See the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Contact

For any questions or issues, please open an issue in the project repository. 