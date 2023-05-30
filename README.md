# clover_db_extractor

The project extracts the line coverage information from clover.db into a XML file.

Use `mvn clean install assembly:single` to generate a single portable .jar file. Use `java -jar file.jar -f pathToDB -o pathToXML` to export the contents into an XML file.
