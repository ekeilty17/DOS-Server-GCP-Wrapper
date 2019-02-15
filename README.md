# DOS GCS Loader 
Uploads information about files in a public [GCP Bucket](https://console.cloud.google.com/storage/browser/genomics-public-data/1000-genomes/bam/?_ga=2.252890444.-472133816.1533309090&_gac=1.81252837.1533310626.Cj0KCQjw-o_bBRCOARIsAM5NbIN8kuD7tf7SIZHrCioTk1HgIWCMdntRn5ibl7CTVZqKpFlGDK6O630aAg_FEALw_wcB) into a DOS Server.

## Usage

First have a dos server running on http://localhost:8101/. These instructions are tested against the
[DNAstack DOS server](https://github.com/DNAstack/GA4GH-DOS-Server) created under Google Summer of Code.

Run the GCS data loader:
```
DOS_SERVER_URL=http://localhost:8101 \
DOS_SERVER_USERNAME=dosadmin \
DOS_SERVER_PASSWORD=dosadmin \
mvn exec:java
```

By default, this imports the GCS public data objects from the 1000 Genomes project.
To see if it worked, execute:
```
$ curl http://localhost:8101/dataobjects
$ curl http://localhost:8101/databundles
```
This should display the objects that have been added to the database.
