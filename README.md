# Website Searcher

Implementation of this project specification:
https://s3.amazonaws.com/fieldlens-public/Website+Searcher.html

## Building
Requirements:
- Java 8
- Maven 3+

Run command
`mvn clean package`

This will build:
- **target/maven-searcher.jar** - Jar without any dependencies
- **target/maven-searcher-with-deps.jar** - Jar with all dependencies included

## Running
`java -jar maven-searcher-with-deps.jar`
