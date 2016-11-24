package eu.ec.eurostat.bd.proximus;

import java.io.File;
import java.util.ArrayList;

import eu.ec.eurostat.ShapeFile;

public class BruSHP {

	public static void main(String[] args) {
		System.out.println("Start");

		File[] folders = new File("H:/geodata/BE_bruxelles_urbis/adm_3d/unzipped/").listFiles();

		//create new file
		ShapeFile merge = new ShapeFile(new ShapeFile("H:/geodata/BE_bruxelles_urbis/adm_3d/unzipped/merge.shp").getSchema(), "H:/geodata/BE_bruxelles_urbis/adm_3d/unzipped/","merge3.shp", false, false);

		System.out.println(folders.length);
		for(File folder : folders){
			if (folder.isDirectory()){
				if(folder.listFiles().length==0) continue;
				System.out.println(folder+"/UrbAdm_Bu_Ground_3D.shp");
				merge.add(folder+"/UrbAdm_Bu_Ground_3D.shp");
			} //else System.err.println(folder);
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

}
