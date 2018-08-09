# DOS-Server-GCP-Wrapper
Uploads data from a public [GCP Bucket](https://console.cloud.google.com/storage/browser/genomics-public-data/1000-genomes/bam/?_ga=2.252890444.-472133816.1533309090&_gac=1.81252837.1533310626.Cj0KCQjw-o_bBRCOARIsAM5NbIN8kuD7tf7SIZHrCioTk1HgIWCMdntRn5ibl7CTVZqKpFlGDK6O630aAg_FEALw_wcB) into a DOS Server.

## Usage

First have a dos server running on http://localhost:8080/. My implementation can be found [here](https://github.com/ekeilty17/GA4GH-DOS-Server). **Note:** Make sure the KeyCloak security is turned off.

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
