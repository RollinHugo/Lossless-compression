import java.util.*;
import java.io.*;

public class RLE extends Compression{
	private List<Byte> _neutre=null;// caractère(s) de demarquation
	private static int _octet=1;// taille de la chaîne qui se repète, en octets
	
	public RLE(String ad){
		raz();
		_depart=ad;
	}
	
	public void raz(){
		_neutre=null;
		_octet=1;
	}
	
	public void chercherNeutre(boolean v) throws IOException{
		if(v)System.out.print(">> Trouver le neutre...    ");
		BufferedInputStream fluxLecture=new BufferedInputStream(new FileInputStream(_depart));
		
		// on stocke les caractères déjà vus dans une table
		Set<List<Byte>> dejaVu=new HashSet<List<Byte>>();
		byte[] buff=new byte[_octet];
		while(fluxLecture.read(buff)==_octet){
			List<Byte> l=new ArrayList<Byte>();
			for(byte by:buff)
				l.add(by);
			dejaVu.add(l);
		}
		fluxLecture.close();
		
		// on forme une liste de taille octet qui est initialisée partout à Byte.MIN_VALUE
		List<Byte> l=new ArrayList<Byte>();
		for(int i=0;i<_octet;i++){
			l.add(Byte.MIN_VALUE);
		}
		// on essaye de trouver une valeur qui n'est pas dans le fichier
		try{
			while(dejaVu.contains(l)){
				l=incrementer(l);
			}
			_neutre=l;
			if(v)System.out.print("OK ( [");
			for(byte by:l)
				System.out.print(" "+by);
			System.out.println(" ] )");
		}
		catch(NullPointerException e){
			// si on n'a pas trouver un neutre de taille octet, on essaye avec octet+1
			System.out.println(e.getMessage());
			_octet++;
			chercherNeutre(v);
		}
	}
	
	public void compresser(String fin, boolean v){
		long temps=-System.currentTimeMillis();
		if(v){
			System.out.println("-------- COMPRESSION--------");
			System.out.println("De                    : "+_depart);
			System.out.println("Vers                  : "+fin);
			System.out.println("Methode               : Repetition");
		}
		try{
			chercherNeutre(v);
			
			if(v)System.out.print(">> Transcription...        ");
			BufferedInputStream fluxLecture=new BufferedInputStream(new FileInputStream(_depart));
			BufferedOutputStream fluxEcriture=new BufferedOutputStream(new FileOutputStream(fin));
			
			// on ecrit le nombre d'octet du motif, puis le neutre
			fluxEcriture.write(_octet);
			for(byte by:_neutre){
				fluxEcriture.write(by);
			}
			
			int nb=0;
			byte[] buff=new byte[_octet];// tampons qui sère à la lecture
			byte[] prebuff=new byte[_octet];// sauvegarde du précedant, pour gérer le EOF
			
			// l'algorithme doit avoir tourné une fois avant sa première fois => mark
			fluxLecture.mark(_octet);
			fluxLecture.read(buff);
			List<Byte> lu=new ArrayList<Byte>(),dernier=new ArrayList<Byte>();
			for(byte by:buff){
				lu.add(by);
				dernier.add(by);
			}
			fluxLecture.reset();
			int nbLu=0;
			
			// codage
			while((nbLu=fluxLecture.read(buff))==_octet){
				lu=new ArrayList<Byte>();
				for(byte by:buff)
					lu.add(by);
				if(dernier.equals(lu) && nb<Byte.MAX_VALUE-1){// la chaîne n'est pas finie
					nb++;
				}
				else{// la chaîne est finie
					ecrire(dernier,nb,fluxEcriture);
					for(int i=0;i<lu.size();i++){
						dernier.set(i, lu.get(i));
					}
					nb=1;
				}
				for(int i=0;i<_octet;i++){
					prebuff[i]=buff[i];
				}
			}
			// quand le fichier est fini, il faut vider les tampons, et vérifiers tous les cas possibles
			if(nbLu!=-1){ 
				fluxEcriture.write(prebuff);
				if(nbLu!=_octet)
					for(int i=0;i<nbLu;i++)
						fluxEcriture.write(buff[i]);
			}
			else{
				ecrire(dernier,nb-1,fluxEcriture);
			}
			
			fluxLecture.close();
			fluxEcriture.close();
			System.out.println("OK");
			temps+=System.currentTimeMillis();
			System.out.println("Fin de la compression en "+temps+" ms");
			CSP.tauxComp(_depart, fin);
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	
	// ecrit les informations necessaires quelque soit le nombre de répétitions
	public void ecrire(List<Byte> carac, int nb, BufferedOutputStream b2) throws IOException{
		if(nb==1)
			for(Byte by:carac)
				b2.write(by);
		else if(nb==2){
			for(Byte by:carac)
				b2.write(by);
			for(Byte by:carac)
				b2.write(by);
		}
		else{
			for(Byte by:_neutre)
				b2.write(by);
			b2.write(nb);
			for(Byte by:carac)
				b2.write((byte)by);
		}
	}
	
	// incremente un List<Byte> avec le même système que les décimaux (retenue...)
	// lance NullPointerException quand la borne supérieure est dépacée.
	public List<Byte> incrementer(List<Byte> l) throws NullPointerException {
		for(int i=0;i<l.size();i++){
			if(l.get(i)==Byte.MAX_VALUE){
				l.set(i, Byte.MIN_VALUE);
			}
			else{
				l.set(i, (byte)(l.get(i)+1));
				return l;
			}
		}
		throw new NullPointerException("Aucun ("+_octet+")");
	}
	
	public void decompresser(String fin, boolean v){
		long temps=-System.currentTimeMillis();
		if(v){
			System.out.println("-------- DECOMPRESSION------");
			System.out.println("De                    : "+_depart);
			System.out.println("Vers                  : "+fin);
			System.out.println("Methode               : Repetition");
		}
		try{
			if(v)System.out.print(">> Nombre d'octet...       ");
			BufferedInputStream fluxLecture=new BufferedInputStream(new FileInputStream(_depart));
			BufferedOutputStream fluxEcriture=new BufferedOutputStream(new FileOutputStream(fin));
			
			// on lit les informations de l'entête
			_octet=fluxLecture.read();
			if(v)System.out.println("OK ("+_octet+")");
			if(v)System.out.print(">> Neutre...               ");
			byte[] buff=new byte[_octet];
			fluxLecture.read(buff);
			byte[] neutre=new byte[_octet];
			if(v)System.out.print("OK ( [");
			for(int i=0;i<_octet;i++){
				neutre[i]=buff[i];
				System.out.print(" "+neutre[i]);
			}
			System.out.println(" ] )");
			
			// on traduit
			if(v)System.out.print(">> Transcription...        ");
			int nbLu=0;
			while((nbLu=fluxLecture.read(buff))==_octet){
				int i=0;
				boolean different=false;
				while(i<_octet && !different){
					if(buff[i]!=neutre[i])
						different=true;
					i++;
				}
				if(different){
					fluxEcriture.write(buff);
				}
				else{
					int nb=fluxLecture.read();
					fluxLecture.read(buff);
					for(int j=0;j<nb;j++)
						fluxEcriture.write(buff);
				}
			}
			// on gère la fin du fichier
			if(nbLu==-1){
				fluxEcriture.write(buff);
			}
			else if(nbLu!=_octet)
				for(int i=0;i<nbLu;i++)
					fluxEcriture.write(buff[i]);
			
			fluxLecture.close();
			fluxEcriture.close();
			if(v) System.out.println("OK");
			temps+=System.currentTimeMillis();
			System.out.println("Fin de la decompression en "+temps+" ms");
			
		}
		catch(IOException e){
			System.out.println(e);
		}
	}
	
	
	public static void main(String[] f){
		RLE rc=new RLE("test/h.html");
		//rc.tester("test/s.so", "test/2s.so", true);
		rc.testerTout(true);
		
	}
	
}
