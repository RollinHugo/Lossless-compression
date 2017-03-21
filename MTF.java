import java.util.*;
import java.io.*;

public class MTF extends Compression{
	// la table mesure la distance par rapport au caractère actuel
	private List<Byte> tableDistance;
	
	public MTF(String ad){
		_depart=ad;
		tableDistance=new ArrayList<Byte>();
		for(int b=Byte.MIN_VALUE;b<=Byte.MAX_VALUE;b++)
			tableDistance.add((byte)b);
	}
	
	public void compresser(String fin, boolean v){
		if(v){
			System.out.println("-------- COMPRESSION--------");
			System.out.println("De                    : "+_depart);
			System.out.println("Vers                  : "+fin);
			System.out.println("Methode               : MTF");
		}
		long tps=-System.currentTimeMillis();
		try{
			if(v)System.out.print(">> Compression...          ");
			BufferedInputStream fluxLecture =new BufferedInputStream(new FileInputStream(_depart));
			DataOutputStream fluxEcriture=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fin)));
			
			// on code :
			int intLu=0;
			while((intLu=fluxLecture.read())!=-1){
				byte byteLu=(byte)intLu;
				// on récupére l'index de l'octet dans la table
				int index=tableDistance.indexOf(byteLu);
				// on met à jour le tableau des index
				tableDistance.remove((int)index);
				tableDistance.add(0, byteLu);
				fluxEcriture.writeByte(index+Byte.MIN_VALUE);
			}
			fluxEcriture.close();
			fluxLecture.close();
			if(v)System.out.println("OK");
			tps+=System.currentTimeMillis();
			System.out.println("Fin de la compression en "+tps+" ms");
			CSP.tauxComp(_depart, fin);
		}
		catch(IOException e){
			System.out.println(e);
		}
	}
	
	public void decompresser(String fin, boolean v){
		if(v){
			System.out.println("------- DECOMPRESSION-------");
			System.out.println("De                    : "+_depart);
			System.out.println("Vers                  : "+fin);
			System.out.println("Methode               : MTF");
		}
		long tps=-System.currentTimeMillis();
		try{
			
			// on fait l'exact opposé de la compression
			
			if(v)System.out.print(">> Décompression...        ");
			DataInputStream fluxLecture=new DataInputStream(new BufferedInputStream(new FileInputStream(_depart)));
			BufferedOutputStream fluxEcriture=new BufferedOutputStream(new FileOutputStream(fin));
			
			try{
				while(true){
					int index=fluxLecture.readByte()-Byte.MIN_VALUE;
					byte byteLu=tableDistance.get(index);
					tableDistance.remove(index);
					tableDistance.add(0, byteLu);
					fluxEcriture.write(byteLu);
				}
			}
			catch(EOFException e){
			}
			
			fluxEcriture.close();
			fluxLecture.close();
			if(v)System.out.println("OK");
			tps+=System.currentTimeMillis();
			System.out.println("Fin de la décompression en "+tps+" ms");
		}
		catch(IOException e){
			System.out.println(e);
		}
	}
	public double estimationComp(){
		System.out.println("Pas de changement de taille notable");
		return 1.;
	}
	
	public void raz(){
		_depart=null;
		tableDistance=new ArrayList<Byte>();
		for(int b=Byte.MIN_VALUE;b<=Byte.MAX_VALUE;b++)
			tableDistance.add((byte)b);
	}
	public static void main(String[] args){
		MTF b=new MTF("");
		b.testerTout(true);
	}
	
}
