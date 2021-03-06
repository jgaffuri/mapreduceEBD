/**
 * 
 */
package eu.ec.eurostat.bd.azure;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

/**
 * @author julien Gaffuri
 *
 */
public class Test {
	//https://azure.microsoft.com/en-us/documentation/articles/storage-java-how-to-use-blob-storage/
	//https://github.com/azure/azure-storage-java
	//http://azure.github.io/azure-storage-java/

	public static void main(String[] args) {
		System.out.println("Start");
		Config.init();

		//https://estatbdstorage.file.core.windows.net/fileshare/test.txt

		try {
			// Retrieve storage account, blob client and container
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(Config.azureStorageConnectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference("containerfromjava");
			//CloudBlobContainer container = blobClient.getContainerReference("estatbdcluster-1");
			//CloudBlobContainer container = blobClient.getContainerReference("testblob");

			// Create the container if it does not exist.
			//container.createIfNotExists();

			// Configure container for public access 
			//BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
			//containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
			//container.uploadPermissions(containerPermissions);

			//Upload blob into container
			//CloudBlockBlob blob = container.getBlockBlobReference("miserables.txt");
			//CloudBlockBlob blob = container.getBlockBlobReference("example/data/gutenberg/miserables.txt");
			//CloudBlockBlob blob = container.getBlockBlobReference("eurostat.png");
			//CloudBlockBlob blob = container.getBlockBlobReference("test.txt");
			//File source = new File("H:\\desktop\\miserables.txt");
			//File source = new File("src\\main\\resources\\eurostat.png");
			//File source = new File("src\\main\\resources\\test.txt");
			//blob.upload(new FileInputStream(source), source.length());

			//List the blobs in a container
			for (ListBlobItem blobItem : container.listBlobs())
				System.out.println(blobItem.getUri());

			//Download all blobs in container
			/*for (ListBlobItem blobItem : container.listBlobs()) {
				if (!(blobItem instanceof CloudBlob)) continue;
				// Download the item and save it to a file with the same name.
				CloudBlob cblob = (CloudBlob) blobItem;
				cblob.download(new FileOutputStream("H:\\" + cblob.getName()));
			}*/


			// Delete blob.
			//CloudBlockBlob blob = container.getBlockBlobReference("eurostat.png");
			//blob.deleteIfExists();

			// Delete the blob container.
			//container.deleteIfExists();

		}
		catch (Exception e) { e.printStackTrace(); }

		System.out.println("End");
	}

}
