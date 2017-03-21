import java.io.*;
import java.util.*;

public class HuffmanComp extends Compression{
	private Map<Short,String> tableCodage;
	// table <motif 16bit : cha�ne de 0 et de 1
	private Map<String,Short> tableDecodage;
	// reciproque de la pr�cedante
	private Map<Short,Integer> nombreOccurences;
	// nombre d'apparition du motif dans le fichier
	private Noeud<Short> racineArbre;
	// arbre de Huffman
	private int tailleFichier;
	// taille du fichier
	public final static int TAILLE_ECR=16;
	// taille du motif (en bit)
	private long temps=0;
	private byte dernierCarac=-1;
	// dernier caractere dans le cas de fichiers de taille impaire (-1 sinon)

	
	//========== CONSTRUCTEURS =================
	
	public HuffmanComp(){
		nombreOccurences=new HashMap<Short,Integer>();
		tableCodage= new HashMap<Short,String>();
		tableDecodage= new HashMap<String,Short>();
		racineArbre=null;
		tailleFichier=0;
		_depart=new String();
	}
	
	public void raz(){
		nombreOccurences=new HashMap<Short,Integer>();
		tableCodage= new HashMap<Short,String>();
		tableDecodage= new HashMap<String,Short>();
		racineArbre=null;
		tailleFichier=0;
		_depart=new String();
	}
	
	public HuffmanComp(String adresseFichierDepart){
		this();
		_depart=adresseFichierDepart;
	}
	
	// pour obtenir la table des fr�quences
	public void lireFichier(boolean verbeux){
		if(verbeux)System.out.print(">> Lecture du fichier...   ");
		try{
			// on cr�� 2 flux d'entr�e, pour g�rer le cas d'un fichier de taille impaire :
			BufferedInputStream fluxLectureBuff= new BufferedInputStream(new FileInputStream(_depart));
			DataInputStream fluxLectureData=new DataInputStream(fluxLectureBuff);
			boolean aTermin�=false;
			short caracLu=0;
			// on lance la boucle de lecture qui compte l'occurences des groupes de 16 bits  :
			// prendre comme crit�re d'arret le lancement d'une exeception est ici plus rapide qu'un for
			while(!aTermin�){
				try{
					caracLu=fluxLectureData.readShort();
					// on marque le flux BufferedInputStream pour revenir en arri�re sans utiliser un flux Random Access
					fluxLectureBuff.mark(1);
					if(!nombreOccurences.containsKey(caracLu)) nombreOccurences.put(caracLu,1);
					else nombreOccurences.put(caracLu,nombreOccurences.get(caracLu)+1);
				}
				catch(EOFException e){//crit�re d'arret :
					aTermin�=true;
				}	
			}
			
			File fichier=new File(_depart);
			tailleFichier=(int)fichier.length();
			// dans le cas d'un fichier impair :
			if(tailleFichier%2==1){
				fluxLectureBuff.reset();
				dernierCarac=(byte)fluxLectureBuff.read();
			}
			
			if(verbeux)System.out.println("OK");
			fluxLectureData.close();
			fluxLectureBuff.close();
		}
		catch(IOException e){
			System.err.println("Erreur de lecture "+e);
			}
	}
	
	//========== MONTAGE ================
	
	// pour monter l'abre binaire
	public void monterArbre(boolean v){
		if(v)System.out.print(">> Montage de l'arbre...   ");
		// cr�ation de l'arbre comme une liste d'un noeud pas valeur
		List<Noeud<Short>> arbre=new ArrayList<Noeud<Short>>();
		Set<Short> shortsPresents=nombreOccurences.keySet();
		for(Short k : shortsPresents){
			Noeud<Short> n = new Noeud<Short>(k,nombreOccurences.get(k));
			arbre.add(n);
		}
		// on le trie une premi�re fois :
		Collections.sort(arbre, new NoeudComparator());
		// on regroupe les noeuds :
		while(arbre.size()>1){
			// on regroupe les 2 premiers :
			Noeud<Short> n= new Noeud<Short>(arbre.get(0),arbre.get(1));
			arbre.remove(0);
			arbre.remove(0);
			// on replace le noeud � sa place. 2 cas particuliers :
			if(arbre.size()==0){
				arbre.add(n);
			}
			else if(n.poid>arbre.get(arbre.size()-1).poid){
				arbre.add(arbre.size(),n);
			}
			else{
				// pour trouver sa place on commence par une dichotomie :
				int a=1, b=arbre.size(),c=0;
				int p1=0,p2=n.poid;
				while(b-a>10){
					c=a+(int)((b-a)/2.);
					p1=arbre.get(c).poid;
					if(p1<=p2)a=c;
					else b=c;
				}
				// puis par test successifs :
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
	
	// pour obtenir les tables
	public void chargerListe(boolean v){
		
		Set<Short> s=tableCodage.keySet();
		tableDecodage=new HashMap<String,Short>();
		for(short k : s){
			tableDecodage.put(tableCodage.get(k),k);
		}
		if(v)System.out.println("OK");
	}
	
	// fonction r�cusrcive pour obtenir le code binaire associ� � l'arbre :
	public void lireArbre(Noeud<Short> n, String str){
		if(n.valeur!=n.DEF){
			tableCodage.put(n.valeur, str);
		}
		else{
			lireArbre(n.droite,str+"1");
			lireArbre(n.gauche,str+"0");
		}
	}
	
	//=========  INTERFACE  ==============
	public double estimationComp(){
		if(nombreOccurences.isEmpty())lireFichier(true);
		if(tableCodage.isEmpty())chargerListe(true);
		int tailleIni=tailleFichier*8;
		int tailleFin=0;
		Set<Short> s=tableCodage.keySet();
		for(short k : s){
			tailleFin+=tableCodage.get(k).length()*nombreOccurences.get(k);
		}
		double d=tailleFin*1.0/tailleIni;
		System.out.println("-------- PREVISIONS---------");
		System.out.println("Non compress�         : "+tailleIni+" bit "+tailleIni/8./1000+" Ko");
		System.out.println("Compress�             : "+tailleFin+" bit "+tailleFin/8./1000+" Ko");
		System.out.printf("Taux de compression   : %.2f%s\n",(1-d)*100,"%");
		return d;
	}

	// pour compresser :
	public void compresser(String fin, boolean v){
		temps=-System.currentTimeMillis();
		if(v){
			System.out.println("-------- COMPRESSION--------");
			System.out.println("De                    : "+_depart);
			System.out.println("Vers                  : "+fin);
			System.out.println("Methode               : Huffman");
		}
		lireFichier(v);
		monterArbre(v);
		if(v)System.out.print(">> Lecture de l'arbre...   ");
		lireArbre(racineArbre,"");
		chargerListe(v);
		try{
			if(v)System.out.print(">> Encodage...             ");
			DataInputStream fluxLecture=new DataInputStream(new BufferedInputStream(new FileInputStream(_depart)));
			StringBuffer cha�neBinaire=new StringBuffer();
			boolean aTermin�=false;
			short shortLu=0;
			// on relit le fichier en ajoutant la cha�ne de 0 et 1 corespondant au code
			while(!aTermin�){
				try{
					shortLu=fluxLecture.readShort();
					cha�neBinaire.append(tableCodage.get(shortLu));
				}
				catch(IOException e){
					aTermin�=true;
					// crit�re d'arret plus rapide
				}
			}
			if(v)System.out.println("OK");
			fluxLecture.close();
			
			if(v)System.out.print(">> Cr�ation de l'objet...  ");
			short[] liste=new short[cha�neBinaire.length()/(TAILLE_ECR)+1];
			int nombreBitsEcrits=0;
			int nombreEcritures=0;
			// donnees :
			while(cha�neBinaire.length()>nombreBitsEcrits+TAILLE_ECR){
				liste[nombreEcritures]=string2Short(cha�neBinaire.substring(nombreBitsEcrits, nombreBitsEcrits+TAILLE_ECR));
				nombreBitsEcrits+=TAILLE_ECR;
				nombreEcritures++;
			}
			liste[nombreEcritures]=string2Short(cha�neBinaire.substring(nombreBitsEcrits));
			// nombre de bit en trop :
			int tailleDernierEntier=TAILLE_ECR-cha�neBinaire.length()%TAILLE_ECR;
			// table de decodage :
			int tailleTable=tableDecodage.size();// taille de la table
			int[] valeur16b=new int[tailleTable];// valeurs en clair
			byte[] nbBitValeur16b=new byte[tailleTable];// nb de bit de la valeur cod�e associ�e
			short[] code=new short[tailleTable];// valeur cod�e associ�e
			Set<String> setValeur16=tableDecodage.keySet();
			Iterator<String> iteratorValeur16=setValeur16.iterator();
			for(int i2=0;i2<tailleTable;i2++){
				String s=iteratorValeur16.next();
				valeur16b[i2]=Integer.parseInt(s,2);
				nbBitValeur16b[i2]=(byte)s.length();
				code[i2]=tableDecodage.get(s);
			}
			HuffmanCompEcrit hme=new HuffmanCompEcrit(valeur16b,nbBitValeur16b,code,liste,tailleFichier,tailleDernierEntier,dernierCarac);
			if(v)System.out.println("OK ");
			
			// il suffit de l'�crire
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
			System.err.println(e);
		}
	}
	
	public void decompresser(String fin,boolean v){
		temps=-System.currentTimeMillis();
		if(v){
			System.out.println("-------- DECOMPRESSION------");
			System.out.println("De                    : "+_depart);
			System.out.println("Vers                  : "+fin);
			System.out.println("Methode               : Huffman");
		}
		try{
			// on lit l'objet :
			if(v)System.out.print(">> Lecture...              ");
			ObjectInputStream fluxLecture=new ObjectInputStream(new BufferedInputStream(new FileInputStream(_depart)));
			HuffmanCompEcrit hme=(HuffmanCompEcrit) fluxLecture.readObject();
			fluxLecture.close();
			if(v)System.out.println("OK");
			
			// on change la forme de ses donn�es :
			if(v)System.out.print(">> Mise en cha�ne...       ");
			int tailleTable=hme.decI.length;
			tableDecodage=new HashMap<String,Short>();// 2x plus rapide que TreeMap
			try{
				for(int i=0;i<tailleTable;i++){
					String CaracDecod�e=int2bin(hme.decI[i],hme.decT[i]);
					tableDecodage.put(CaracDecod�e, hme.decS[i]);
				}
			}
			catch(Exception e){
				// crit�re d'arret
			}
			StringBuffer Cha�neCod�e=new StringBuffer();
			String code16b;
			for(short i:hme.d){
				// on recup�re 16 bits de donn�e et on les remet sous une bonne forme
				code16b=Integer.toBinaryString(i);
				if(i<0)code16b=code16b.substring(16);
				while(code16b.length()<TAILLE_ECR){
					code16b="0"+code16b;
				}
				Cha�neCod�e.append(code16b);
			}
			// on enl�ve les derniers bits non porteur d'information :
			Cha�neCod�e.delete(Cha�neCod�e.length()-TAILLE_ECR,Cha�neCod�e.length()-TAILLE_ECR+hme.td);
			if(v)System.out.println("OK");
			if(v)System.out.print(">> Ecriture...             ");
			
			// decodage :
			DataOutputStream fluxEcriture=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fin)));
			int t=0;
			Set<String> clef=tableDecodage.keySet();
			// on mesure la taille minimale du code de Huffman
			// on choisit 30 comme depart, car cela representerait un algorythme tr�s mauvais (on est sur 16bits)
			int tailleMin=30; 
			for(String s:clef){
				if(s.length()<tailleMin)tailleMin=s.length();
			}
			// on decode et on �crit
			boolean aTermin�=false;
			while(!aTermin�){
				int j=tailleMin;
				try{
					// on commence � chercher une entr�e avec la taille minimale
					while(tableDecodage.get(Cha�neCod�e.substring(t, t+j))==null){
						j++;
					}
					fluxEcriture.writeShort(tableDecodage.get(Cha�neCod�e.substring(t, t+j)));
				}
				catch(StringIndexOutOfBoundsException e){
					aTermin�=true;
				}
				t+=j;
			}
			// on rajoute les caract�res finaux stock�s en clair
			if(hme.p!=-1){
				fluxEcriture.write(hme.p);
			}
			if(v)System.out.println("OK");
			temps+=System.currentTimeMillis();
			System.out.println("Fin de la decompression en "+temps+" ms");
			fluxEcriture.close();
		}
		catch(IOException e){
			System.err.println(e+" ");
		}
		catch(ClassNotFoundException e){
			System.out.println(e);
		}
	}
	
	//============ GETTERS & SETTERS ================
	
	public Noeud<Short> getArbre(){
		return racineArbre;
	}
	
	public Map<Short,Integer> tableOccurence(){
		return nombreOccurences;
	}
	
	public Map<String,Short> tableDecodage(){
		return tableDecodage;
	}
	
	public int getOccur(int val){
		return nombreOccurences.get(val);
	}

	// converti une cha�ne de 0 et de 1 en short
	// la cha�ne est vue avec une convention de signe de type unsigned
	public static short string2Short(String str){
		if(str.length()==TAILLE_ECR){
			if(str.charAt(0)=='0')return Short.parseShort(str,2);
			else{
				return (short) (Short.parseShort(str.substring(1),2)-Short.MAX_VALUE-1);
			}
		}
		return Short.parseShort(str,2);
	}

	//=========== TEST ==============
	
	public static void main(String[] args){
		HuffmanComp hc=new HuffmanComp("");
		hc.tester("test/k.html", "test/2k.html", true);
		//hc.testerTout(true);
	}
}

class HuffmanCompEcrit implements Serializable{
	// les noms des variables sont petits car ils apparaissent dans le fichier compress�
	int[] decI;//chaine binaire
	byte[] decT;//taille de la cha�ne (pour les 0 devant)
	short[] decS;//valeur associ�e
	short[] d;//table de donnees
	int td;//taille dernier int
	double t;//taille
	byte p;//pour la parit�
	HuffmanCompEcrit(int[] cha�neBinaire,byte[] tailleCha�ne,short[] valeurCha�ne, short[] donn�es, double taille,int tdi, byte dernierByte){
		decI=cha�neBinaire;
		decT=tailleCha�ne;
		decS=valeurCha�ne;
		d=donn�es;
		t=taille;
		td=tdi;
		p=dernierByte;
	}

}
