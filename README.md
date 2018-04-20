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
To run with default settings:  
`java -jar maven-searcher-with-deps.jar`

This will run the tool with the default settings (# of threads and # of URLs searched). 
The results of the (regex) search will be written to file **results.txt**

To run with default optional arguments (order of arguments matters):  
`java -jar maven-searcher-with-deps.jar [# of threads] [# of URLs to search] [regex search]`

For example:  
`java -jar website-searcher-with-deps.jar 5 10 "(?s).*(facebook|twitter).*"`
