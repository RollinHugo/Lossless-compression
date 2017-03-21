import java.io.*;
import java.util.*;

public class HuffmanComp8 extends Compression{
	private Map<Byte,String> tableCodage;
	// table <motif 16bit : chaîne de 0 et de 1
	private Map<String,Byte> tableDecodage;
	// reciproque de la précedante
	private Map<Byte,Integer> occurences;
	// nombre d'apparition du motif dans le fichier
	private Noeud<Byte> racineArbre;
	// arbre de Huffman
	private int tailleFichier;
	// taille du fichier
	public final static int TAILLE_ECR=8;
	// taille du motif (en bit)
	private long temps=0;
	
	public HuffmanComp8(){
		occurences=new HashMap<Byte,Integer>();
		tableCodage= new HashMap<Byte,String>();
		tableDecodage= new HashMap<String,Byte>();
		racineArbre=null;
		tailleFichier=0;
		_depart=new String();
	}
	
	public void raz(){
		occurences=new HashMap<Byte,Integer>();
		tableCodage= new HashMap<Byte,String>();
		tableDecodage= new HashMap<String,Byte>();
		racineArbre=null;
		tailleFichier=0;
		_depart=new String();
	}
	
	public HuffmanComp8(String depart){
		this();
		_depart=depart;
	}
	 
	// compte le nombre d'apparition de chaque motif
	public void lireFichier(boolean v)throws IOException{
		if(v)System.out.print(">> Lecture du fichier...   ");
		BufferedInputStream b=new BufferedInputStream(new FileInputStream(_depart));
		occurences=new HashMap<Byte,Integer>();
		int intLu=0;
		byte byteLu;
		while((intLu=b.read())!=-1){
			byteLu=(byte)intLu;
			if(!occurences.containsKey(byteLu))occurences.put(byteLu, 1);
			else occurences.put(byteLu, occurences.get(byteLu)+1);
		}
		b.close();
		if(v)System.out.println("OK");
	}
	
	// monte l'arbre de Huffman
	public void monterArbre(boolean v){
		if(v)System.out.print(">> Montage de l'arbre...   ");
		// création de l'arbre, comme d'une liste de noeuds :
		List<Noeud<Byte>> arbre=new ArrayList<Noeud<Byte>>();
		Set<Byte> valClef=occurences.keySet();
		for(byte k : valClef){
			Noeud<Byte> n = new Noeud<Byte>(k,occurences.get(k));
			arbre.add(n);
		}
		//premier tri complet :
		Collections.sort(arbre, new NoeudComparator());
		while(arbre.size()>1){
			// on regroupe les deux premiers noeuds
			Noeud<Byte> n= new Noeud<Byte>(arbre.get(0),arbre.get(1));
			arbre.remove(0);
			arbre.remove(0);
			// on replace le nouveau noeud à sa place
			if(arbre.size()==0){
				arbre.add(n);
			}
			else if(n.poid>arbre.get(arbre.size()-1).poid){
				arbre.add(arbre.size(),n);
			}
			else{
				// on commence par une dichotomie :
				int a=1, b=arbre.size(),c=0;
				int p1=0,p2=n.poid;
				while(b-a>10){
					c=a+(int)((b-a)/2.);
					p1=arbre.get(c).poid;
					if(p1<=p2)a=c;
					else b=c;
				}
				// puis on itère et on test :
				int index=-1;
				a--;
				while(a<arbre.size() && index==-1){
					if(arbre.get(a).poid>=p2)index=a;
					else a++;
				}
				arbre.add(index,n);
			}
		}
		racineArbre=arbre.get(0);
		if(v)System.out.println("OK");
	}
	
	// pour former les tables
	public void chargerListe(boolean v){
		if(v)System.out.print(">> Lecture de l'arbre...   ");
		Set<Byte> s=tableCodage.keySet();
		tableDecodage=new HashMap<String,Byte>();
		for(byte k : s){
			tableDecodage.put(tableCodage.get(k),k);
		}
		if(v)System.out.println("OK");
	}
	
	// on associe recursivement les feuilles de l'arbre avec une chaîne de 0 et de 1
	public void lireArbre(Noeud<Byte> n, String str){
		if(n.valeur!=n.DEF){
			tableCodage.put(n.valeur, str);
		}
		else{
			lireArbre(n.droite,str+"1");
			lireArbre(n.gauche,str+"0");
		}
	}
	
	public void compresser(String fin, boolean v){
		if(v){
			System.out.println("-------- COMPRESSION--------");
			System.out.println("De                    : "+_depart);
			System.out.println("Vers                  : "+fin);
			System.out.println("Methode               : Huffman 8b");
		}
		temps=-System.currentTimeMillis();
		try{
			lireFichier(v);
			monterArbre(v);
			lireArbre(racineArbre,"");
			chargerListe(v);
			
			// on encode le fichier :
			if(v)System.out.print(">> Encodage...             ");
			BufferedInputStream fluxLecture= new BufferedInputStream(new FileInputStream(_depart));
			StringBuffer chaîneCompressée=new StringBuffer();
			int buff;
			while((buff=fluxLecture.read())!=-1){
				chaîneCompressée.append(tableCodage.get((byte)buff));
			}
			fluxLecture.close();
			if(v)System.out.println("OK");
			
			// on créé l'objet :
			if(v)System.out.print(">> Création de l'objet...  ");
			byte[] liste=new byte[chaîneCompressée.length()/(TAILLE_ECR)+1];
			int nombreBitsEcrits=0;
			int nombreEcritures=0;
			
			// on coupe la chaîne en octet :
			while(chaîneCompressée.length()>nombreBitsEcrits+TAILLE_ECR){
				liste[nombreEcritures]=string2Byte(chaîneCompressée.substring(nombreBitsEcrits, nombreBitsEcrits+TAILLE_ECR));
				nombreBitsEcrits+=TAILLE_ECR;
				nombreEcritures++;
			}
			if(nombreEcritures<liste.length && nombreBitsEcrits<chaîneCompressée.length())
				liste[nombreEcritures]=string2Byte(chaîneCompressée.substring(nombreBitsEcrits));
			
			// on note le nombre de bit porteurs dans le dernier octet :
			int tailleDernierEntier=TAILLE_ECR-chaîneCompressée.length()%TAILLE_ECR;
			
			// on code la table de decodage :
			int t=tableDecodage.size();
			int[] valeur8b=new int[t];
			byte[] nbBitValeur8b=new byte[t];
			byte[] code=new byte[t];
			Set<String> clef=tableDecodage.keySet();
			Iterator<String> it=clef.iterator();
			for(int i2=0;i2<t;i2++){
				String s=it.next();
				valeur8b[i2]=Integer.parseInt(s,2);
				nbBitValeur8b[i2]=(byte)s.length(); // suppose que la taille est <128
				code[i2]=tableDecodage.get(s);
			}
			HuffmanCompEcrit8 hme=new HuffmanCompEcrit8(valeur8b,nbBitValeur8b,code,liste,tailleFichier,tailleDernierEntier);

			// on l'écrit
			if(v)System.out.println("OK ");
			if(v)System.out.print(">> Ecriture...             ");
			ObjectOutputStream fluxEcriture=new ObjectOutputStream(new FileOutputStream(fin));
			fluxEcriture.writeObject(hme);
			fluxEcriture.close();
			if(v)System.out.println("OK");
			temps+=System.currentTimeMillis();
			System.out.println("Fin de la compression en "+temps+" ms");
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
			System.out.println("Methode               : Huffman 8b");
		}
		temps=-System.currentTimeMillis();
		try{
			// on relit l'objet
			if(v)System.out.print(">> Lecture...              ");
			ObjectInputStream fluxLecture=new ObjectInputStream(new BufferedInputStream(new FileInputStream(_depart)));
			HuffmanCompEcrit8 hc8=(HuffmanCompEcrit8) fluxLecture.readObject();
			fluxLecture.close();
			if(v)System.out.println("OK");
			
			// on réarrange les informations :
			if(v)System.out.print(">> Mise en chaîne...       ");
			int tailleTable=hc8.decI.length;
			tableDecodage=new HashMap<String,Byte>();// 2x plus rapide que TreeMap
			try{
				for(int i=0;i<tailleTable;i++){
					String s=int2bin(hc8.decI[i],hc8.decT[i]);
					tableDecodage.put(s, hc8.decS[i]);
				}
			}catch(Exception e){
				// critère d'arret
			}
			StringBuffer ChaîneCodée=new StringBuffer();
			String code8b;
			for(byte i:hc8.d){
				// on remet en forme 8 bits de données :
				code8b=Integer.toBinaryString(i);
				if(i<0)code8b=code8b.substring(24);
				while(code8b.length()<TAILLE_ECR){
					code8b="0"+code8b;
				}
				ChaîneCodée.append(code8b);
			}
			
			// comme java utilise les deux conventions de signes, il faut différencier pour gérer les derniers bits non porteurs
			if(ChaîneCodée.charAt(ChaîneCodée.length()-TAILLE_ECR)=='1')
				ChaîneCodée.delete(ChaîneCodée.length()-TAILLE_ECR+1,ChaîneCodée.length()-TAILLE_ECR+hc8.td+1);
			else
				ChaîneCodée.delete(ChaîneCodée.length()-TAILLE_ECR,ChaîneCodée.length()-TAILLE_ECR+hc8.td);
			if(v)System.out.println("OK");
			if(v)System.out.print(">> Ecriture...             ");
			
			// on décode et on écrit
			DataOutputStream fluxEcriture=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fin)));
			int t=0;
			
			// on cherche la plus petite chaîne qui encode un octet :
			Set<String> clef=tableDecodage.keySet();
			int tailleMin=30;
			for(String s:clef){
				if(s.length()<tailleMin)tailleMin=s.length();
			}
			
			// on decode en cherchant toujours depuis le nombre de bit minimal
			boolean aTerminé=false;
			while(!aTerminé){
				int j=tailleMin;
				try{
					while(tableDecodage.get(ChaîneCodée.substring(t, t+j))==null){
						j++;
					}
					fluxEcriture.writeByte(tableDecodage.get(ChaîneCodée.substring(t, t+j)));
				}
				catch(StringIndexOutOfBoundsException e){
					aTerminé=true;
				}
				t+=j;
			}
			if(v)System.out.println("OK");
			temps+=System.currentTimeMillis();
			System.out.println("Fin de la décompression en "+temps+" ms");
			fluxEcriture.close();
		}
		catch(ClassNotFoundException e){
			System.out.println(e);
		}
		catch(IOException e){
			System.out.println(e);
		}
	}
	
	public static void main(String[] args){
		HuffmanComp8 hc8=new HuffmanComp8("");
		hc8.testerTout(true);
	}
}

class HuffmanCompEcrit8 implements Serializable{
	// les noms des variables sont petits car ils apparaissent dans le fichier compressé
	int[] decI;//chaine binaire
	byte[] decT;//taille de la chaîne (pour les 0 devant)
	byte[] decS;//valeur associÃ©e
	byte[] d;//table de donnees
	int td;//taille dernier int (le nom apparait dans le fichier)
	double t;//taille
	HuffmanCompEcrit8(int[] chaîneBinaire,byte[] tailleChaîne,byte[] valeurChaîne, byte[] données, double taille,int tdi){
		decI=chaîneBinaire;
		decT=tailleChaîne;
		decS=valeurChaîne;
		d=données;
		t=taille;
		td=tdi;
	}
}

