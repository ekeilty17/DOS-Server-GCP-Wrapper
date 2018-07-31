# DOS-Server-GCP-Wrapper
Uploads data from a public GCP Bucket into a DOS Server

## Run

First have a dos server running on `localhost:8080`. My implementation can be found [here](https://github.com/ekeilty17/GA4GH-DOS-Server).

Make sure you are usig Java 1.8
```
javac -version
```

Use the Maven plugin
```

mvn clean spring-boot:run

```

To see if it worked, execute:
```
$ curl http://localhost:8080/dataobjects
$ curl http://localhost:8080/databundles
```
This should display the objects that have been added to the database
