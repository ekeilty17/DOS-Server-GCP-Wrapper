# DOS GCS Loader 
Uploads information about files in a public [GCP Bucket](https://console.cloud.google.com/storage/browser/genomics-public-data/1000-genomes/bam/?_ga=2.252890444.-472133816.1533309090&_gac=1.81252837.1533310626.Cj0KCQjw-o_bBRCOARIsAM5NbIN8kuD7tf7SIZHrCioTk1HgIWCMdntRn5ibl7CTVZqKpFlGDK6O630aAg_FEALw_wcB) into a DOS Server.

## Usage

First have a DRS server running on http://localhost:8086/. These instructions are tested against the
[DNAstack DRS server](https://github.com/DNAstack/drs-server).

Run the the data loader for the appropriate bucket or container:
#### Azure Blob Storage
```
export AZURE_SUBSCRIPTION_ID=<subscription id>
export STORAGE_ACCOUNT_NAME=<storage account name>
export CONTAINER_NAME=<container_name>

DRS_SERVER_URL="http://localhost:8086" \
DRS_SERVER_USERNAME="" \
DRS_SERVER_PASSWORD="" \
AZURE_STORAGE_ACCOUNT=`az storage account list --query "[?name=='${STORAGE_ACCOUNT_NAME}'].{id:id}" --output tsv` \
AZURE_CONNECTION_STRING=`az storage account show-connection-string --name ${STORAGE_ACCOUNT_NAME} -o tsv` \
mvn exec:java -Dexec.args="https://${STORAGE_ACCOUNT_NAME}.blob.core.windows.net/${CONTAINER_NAME}"
```
*Note that the AZURE_STORAGE_ACCOUNT environment variable should be set to the resource id of the storage account, NOT the storage account name.  The above example shows how to get this information using the az CLI.
#### Google Cloud Storage
```
DRS_SERVER_URL="http://localhost:8086" \
DRS_SERVER_USERNAME="" \
DRS_SERVER_PASSWORD="" \
GOOGLE_APPLICATION_CREDENTIALS=<path to credentials.json> \
mvn exec:java -Dexec.args=gs://<bucket name>
```

## Tests
Ensure all the environment variables above are set, and then run ```mvn test```
e.g.:
```
export AZURE_SUBSCRIPTION_ID=<subscription id>
export STORAGE_ACCOUNT_NAME=<storage account name>
export CONTAINER_NAME=<container_name>

GOOGLE_APPLICATION_CREDENTIALS=<path to credentials.json> \ 
DRS_SERVER_URL="http://localhost:8086" \
DRS_SERVER_USERNAME="" \
DRS_SERVER_PASSWORD="" \
AZURE_STORAGE_ACCOUNT=`az storage account list --query "[?name=='${STORAGE_ACCOUNT_NAME}'].{id:id}" --output tsv` \
AZURE_CONNECTION_STRING=`az storage account show-connection-string --name ${STORAGE_ACCOUNT_NAME} -o tsv` \
AZURE_TEST_CONTAINER_URL="https://${STORAGE_ACCOUNT_NAME}.blob.core.windows.net/${CONTAINER_NAME}" \
mvn test
```

### Example: Load 1000 Genomes Data
```
DRS_SERVER_URL=http://localhost:8086 \
DRS_SERVER_USERNAME="" \
DRS_SERVER_PASSWORD="" \
mvn exec:java -Dexec.args='gs://genomics-public-data 1000-genomes/bam'

DRS_SERVER_URL=http://localhost:8086 \
DRS_SERVER_USERNAME="" \
DRS_SERVER_PASSWORD="" \
mvn exec:java -Dexec.args='gs://genomics-public-data 1000-genomes/vcf'
```
