package eu.ec.eurostat.bd.proximus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BruSHP {

	public static void main(String[] args) {
		System.out.println("Start");

		String folder = "H:/geodata/BE_bruxelles_urbis/adm_3d/";
		ArrayList<File> files = getFiles(new File(folder));

		for(File f : files){
			String c = f.getName().replaceAll("UrbAdm3D_", "").replaceAll("_SHP.zip", "");
			System.out.println(c);
			unZip(f.getAbsolutePath(), folder+c);
		}


		System.out.println("End");
	}


	public static ArrayList<File> getFiles(File folder) {
		ArrayList<File> files = new ArrayList<File>();
		for (File file : folder.listFiles())
			if (file.isDirectory())
				files.addAll(getFiles(file));
			else
				files.add(file);
		return files;
	}


	public static void unZip(String zipFile, String outputFolder){
		try{
			byte[] buffer = new byte[1024];

			//output directory
			File folder = new File(outputFolder);
			if(!folder.exists()) folder.mkdir();

			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();
			while(ze!=null){
				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);
				fos.close();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}

}
