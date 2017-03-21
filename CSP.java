import java.io.*;
import java.util.*;
import java.lang.*;

enum TypeFichier{TEXTE, HYBRIDE, DONNEES_COMPRESSEES, INCONNU};

public class CSP {
	public static boolean VERB=true;
	private static final int LETTRE=0,ESPACE=1,MOYENNE=2,ECART_TYPE=3;
	
	public static boolean estLeMeme(String s1, String s2){
		int manquant=0;
		int mauvais=0;
		try{
			System.out.print("Test entre "+s1+" et "+s2+ " : ");
			FileInputStream f1= new FileInputStream(s1);
			FileInputStream f2= new FileInputStream(s2);
			BufferedInputStream g1=new BufferedInputStream(f1);
			BufferedInputStream g2=new BufferedInputStream(f2);
			int i1=g1.read();
			int i2=g2.read();
			while(i1!=-1){
				if(i2!=-1){
					if(i1!=i2)mauvais++;
					i2=g2.read();
				}
				else manquant++;
				i1=g1.read();
			}
			while(g2.read()!=-1)manquant++;
			f1.close();
			f2.close();
			if(mauvais==0 && manquant==0){
				System.out.println("IDENTIQUES");
			}
			else{
				System.out.println("DIFFERENTS");
				System.out.println("Mauvais : "+mauvais);
				System.out.println("Manquant : "+manquant);
			}
		}
		catch(IOException e){
			System.out.println("FICHIER(S) INTROUVABLE(S)");
		}
		
		return (mauvais==0)&&(manquant==0);
	}
	
	public static double tauxComp(String depart, String arrivée){
		double taux=100-(double)tailleDe(arrivée)/tailleDe(depart)*100;
		System.out.printf("taux de compression : %.1f%s\n",taux,"%");
		return taux;
	}
	
	public static int tailleDe(String ad){
		int taille=0;
		try{
			FileInputStream f= new FileInputStream(ad);
			BufferedInputStream b=new BufferedInputStream(f);
			while(b.read()!=-1)taille++;
			b.close();
		}
		catch(IOException e){
			System.err.println(e);
		}
		return taille;
	}
	
	public static void testerTout(boolean sauver){
		try{
			PrintStream cons=System.out, log;
			if(sauver){
				log = new PrintStream("bilan2.log");
				System.setOut(log);
			}
			HuffmanComp hc=new HuffmanComp("");
			hc.testerTout(VERB);
			HuffmanComp8 hc8=new HuffmanComp8("");
			hc8.testerTout(VERB);
			RLE rc=new RLE("");
			rc.testerTout(VERB);
			BwtMtfHm8 b1=new BwtMtfHm8("");
			b1.testerTout(VERB);
			BwtMtfRle b2=new BwtMtfRle("");
			b2.testerTout(VERB);
			DicoComp16 dc=new DicoComp16("");
			dc.testerToutTexte(VERB);
			if(sauver)
				System.setOut(cons);
		}
		catch(IOException e){
			System.err.println(e);
		}
	}
	
	public static TypeFichier typeDe(String ad){
		double [] t=analyserFichier(ad);
		if(t==null)
			return TypeFichier.INCONNU;
		
		if(t[ESPACE]>10 && t[LETTRE]>60)
			return TypeFichier.TEXTE;
		if(t[ESPACE]>5 && t[LETTRE]>30)
			return TypeFichier.HYBRIDE;
		if(Math.abs(t[MOYENNE]-Byte.MAX_VALUE)<5 && Math.abs(t[ECART_TYPE]-Byte.MAX_VALUE/2-10)<3)
			return TypeFichier.DONNEES_COMPRESSEES;
		return TypeFichier.INCONNU;
	}
	
	private static double[] analyserFichier(String ad){
		try{
			int tailleMax=2000;
			int taille=tailleDe(ad);
			RandomAccessFile r=new RandomAccessFile(ad,"r");
			
			int c;
			int[] val=new int[tailleMax];
			double lettres=0, espaces=0, moyenne=0, ecartType=0;
			Set<Integer> s=new HashSet<Integer>();
			for(int i=0;i<tailleMax;i++){
				r.seek((long)(Math.random()*taille));
				c=r.read();
				s.add(c);
				moyenne+=c;
				val[i]=c;
				if((c>='a'&& c<='z')||(c>='A'&&c<='Z'))
					lettres++;
				if(Character.isSpaceChar(c))
					espaces++;
			}
			r.close();
			moyenne/=tailleMax;
			for(int i:val){
				ecartType+=Math.pow(i-moyenne,2);
			}
			ecartType=Math.sqrt(ecartType/(tailleMax-1));
			return new double[] {lettres/tailleMax*100,espaces/tailleMax*100,moyenne,ecartType};
		}
		catch(IOException e){
			System.out.println(e);
			return null;
		}
	}
	
	public static void conseiller(String ad){
		System.out.print("Algorithme conseillé pour "+ad+" : ");
		switch(typeDe(ad)){
		case TEXTE:
			System.out.println("Dictionnaire");
			break;
		case HYBRIDE:
			System.out.println("Huffman");
			break;
		case DONNEES_COMPRESSEES:
			System.out.println("RLE");
			break;
		case INCONNU:
			System.out.println("Huffman");
			break;
		}
	}
	
	public static void aide(){
		System.out.println("-------------------- ALGORITHMES --------------------");
		System.out.println();
		System.out.println("Commandes :");
		System.out.println("	Compresser           	: c[ID] source destination");
		System.out.println("	Décompresser           	: d[ID] source destination");
		System.out.println("	Estimer           	: e[ID] source");
		System.out.println("	Tester	      	     	: t[ID]");
		System.out.println("[ID] :");
		System.out.println("	Huffman16    		: [h]");
		System.out.println("	Huffman8     		: [h8]");
		System.out.println("	RLE          		: [rl]");
		System.out.println("	dictionnaire 		: [d]");
		System.out.println("	Huffman optimisé	: [rh]");
		System.out.println("	RLE optimisé		: [rle]");
		System.out.println("Exemples :");
		System.out.println("	ch8 test.txt test2.comp");
		System.out.println("	erl test.txt");
		System.out.println("	dd test2.comp test.txt");
		System.out.println("	trl");
		System.out.println("------------------- UTILITAIRES ---------------------");
		System.out.println();
		System.out.println("Comparer des fichiers     	: ident   fichier fichier2");
		System.out.println("Mesurer un fichier        	: taille  fichier");
		System.out.println("Conseiller un algorithme  	: conseil fichier");
		System.out.println("Activer le mode bavard    	: bav 1");
		System.out.println("Desactiver le mode bavard 	: bav 0");
		System.out.println("Donner le type du ficheir 	: type    fichier");
		System.out.println("Aide                      	: aide");
		System.out.println("Tout tester               	: t");
		System.out.println("Tout tester et sauver      	: ts");
		System.out.println("Renouveller les tests      	: rn");
		System.out.println("Renouveller les logs       	: rnlog");
		System.out.println("Lancer une commande systeme 	: sys     commande");
		System.out.println("Quitter               		: Q / q");
	}
	
	public static void main(String[] args){
		//aide();
		System.out.println("	======================================================");
		System.out.println("	|               COMPRESSION SANS PERTES              |");
		System.out.println("	|                  Projet de H.ROLLIN                |");
		System.out.println("	======================================================");
		System.out.println("		[aide] pour afficher les commandes");
		Scanner sc=new Scanner(System.in);
		boolean continuer=true;
		while(continuer){
			System.out.print(">> ");
			String commande=sc.nextLine();
			String[] syntaxe =commande.split(" ");
			if(syntaxe[0].equals("sys")){
				try{
					StringBuffer strB=new StringBuffer();
					for(int i=1;i<syntaxe.length;i++){
						strB.append(syntaxe[i]+" ");
					}
					Process p=Runtime.getRuntime().exec(strB.toString());
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		            String s;
					while ((s = br.readLine()) != null)
		                System.out.println(s);
		            p.destroy();
					//Runtime.getRuntime().exec(strB.toString());
				}
				catch(Exception e){
					System.out.println("ERREUR DU SYSTEME "+e);
				}
			}
			else{
				switch(syntaxe.length){
				case 0:
					System.out.println("SAISIE NON VALIDE");
					break;
				case 1:
					if(syntaxe[0].equals("q")||syntaxe[0].equals("Q")){
						continuer=false;
					}
					else if(syntaxe[0].equals("t")){
						testerTout(false);
					}
					else if(syntaxe[0].equals("ts")){
						long tps=-System.currentTimeMillis();
						System.out.print("Enregistrement dans \"bilan.log\"...");
						testerTout(true);
						tps+=System.currentTimeMillis();
						System.out.println("OK ("+tps+"ms)");
					}
					else if(syntaxe[0].equals("aide")){
						aide();
					}
					else if(syntaxe[0].equals("th")){
						HuffmanComp hc=new HuffmanComp("");
						hc.testerTout(VERB);
					}
					else if(syntaxe[0].equals("th8")){
						HuffmanComp8 hc=new HuffmanComp8("");
						hc.testerTout(VERB);
					}
					else if(syntaxe[0].equals("trl")){
						RLE rc=new RLE("");
						rc.testerTout(VERB);
					}
					else if(syntaxe[0].equals("td")){
						DicoComp16 dc=new DicoComp16("");
						dc.testerToutTexte(VERB);
					}
					else if(syntaxe[0].equals("trh")){
						BwtMtfHm8 bw=new BwtMtfHm8("");
						bw.testerTout(VERB);
					}
					else if(syntaxe[0].equals("trrl")){
						BwtMtfRle dc=new BwtMtfRle("");
						dc.testerTout(VERB);
					}
					else if(syntaxe[0].equals("rn")){
						try{
							Runtime.getRuntime().exec("cp -r te/test .");
							System.out.println("Dossier de test renouvellÃ©");
						}
						catch(Exception e){
							System.out.println("Erreur systeme "+e);
						}
						
					}
					else if(syntaxe[0].equals("rnlog")){
						try{
							Runtime.getRuntime().exec("rm bilan.log");
							System.out.println("log renouvellÃ©");
						}
						catch(IOException e){
							System.out.println("Erreur systeme");
						}
						
					}
					else{
						System.out.println("SAISIE NON VALIDE");
					}
					break;
				case 2:
					if(syntaxe[0].equals("type")){
						System.out.print("Type de "+syntaxe[1]);
						switch(typeDe(syntaxe[1])){
						case TEXTE:
							System.out.println(" : Texte");
							break;
						case HYBRIDE:
							System.out.println(" : Hybride");
							break;
						case DONNEES_COMPRESSEES:
							System.out.println(" : Données compressées");
							break;
						case INCONNU:
							System.out.println(" : Inconnu");
							break;
						}
					}
					else if(syntaxe[0].equals("bav")){
						if(syntaxe[1].equals("0")){
							VERB=false;
							System.out.println("Le mode bavard est désactivé");
						}
						else{
							VERB=true;
							System.out.println("Le mode bavard est activé");
						}
					}
					else if(syntaxe[0].equals("conseil")){
						conseiller(syntaxe[1]);
					}
					else if(syntaxe[0].equals("taille")){
						System.out.println("Taille de "+syntaxe[0]+" "+tailleDe(syntaxe[1]));
					}
					else if(syntaxe[0].equals("eh")){
						HuffmanComp hc=new HuffmanComp(syntaxe[1]);
						hc.estimationComp();
					}
					else if(syntaxe[0].equals("eh8")){
						HuffmanComp8 hc=new HuffmanComp8("");
						hc.estimationComp();
					}
					else if(syntaxe[0].equals("erl")){
						RLE rc=new RLE(syntaxe[1]);
						rc.estimationComp();
					}
					else if(syntaxe[0].equals("ed")){
						DicoComp16 dc=new DicoComp16(syntaxe[1]);
						dc.estimationComp();
					}
					else if(syntaxe[0].equals("erh")){
						BwtMtfHm8 rc=new BwtMtfHm8(syntaxe[1]);
						rc.estimationComp();
					}
					else if(syntaxe[0].equals("errl")){
						BwtMtfRle dc=new BwtMtfRle(syntaxe[1]);
						dc.estimationComp();
					}
					else{
						System.out.println("SAISIE NON VALIDE");
					}
					break;
				case 3:
					if(syntaxe[0].equals("ident")){
						estLeMeme(syntaxe[1],syntaxe[2]);
					}
					else if(syntaxe[0].equals("ch")){
						HuffmanComp hc=new HuffmanComp(syntaxe[1]);
						hc.compresser(syntaxe[2],VERB);
					}
					else if(syntaxe[0].equals("ch8")){
						HuffmanComp8 hc=new HuffmanComp8(syntaxe[1]);
						hc.compresser(syntaxe[2],VERB);
					}
					else if(syntaxe[0].equals("crl")){
						RLE rc=new RLE(syntaxe[1]);
						rc.compresser(syntaxe[2],VERB);
					}
					else if(syntaxe[0].equals("cd")){
						DicoComp16 dc=new DicoComp16(syntaxe[1]);
						dc.compresser(syntaxe[2],VERB);
					}
					else if(syntaxe[0].equals("crh")){
						BwtMtfHm8 rc=new BwtMtfHm8(syntaxe[1]);
						rc.compresser(syntaxe[2],VERB);
					}
					else if(syntaxe[0].equals("crrl")){
						BwtMtfRle dc=new BwtMtfRle(syntaxe[1]);
						dc.compresser(syntaxe[2],VERB);
					}
					else if(syntaxe[0].equals("dh")){
						HuffmanComp hc=new HuffmanComp(syntaxe[1]);
						hc.decompresser(syntaxe[2],VERB);
					}
					else if(syntaxe[0].equals("dh8")){
						HuffmanComp8 hc=new HuffmanComp8(syntaxe[1]);
						hc.decompresser(syntaxe[2],VERB);
					}
					else if(syntaxe[0].equals("drl")){
						RLE rc=new RLE(syntaxe[1]);
						rc.decompresser(syntaxe[2],VERB);
					}
					else if(syntaxe[0].equals("dd")){
						DicoComp16 dc=new DicoComp16(syntaxe[1]);
						dc.decompresser(syntaxe[2],VERB);
					}
					else if(syntaxe[0].equals("drh")){
						BwtMtfHm8 rc=new BwtMtfHm8(syntaxe[1]);
						rc.decompresser(syntaxe[2],VERB);
					}
					else if(syntaxe[0].equals("drrl")){
						BwtMtfRle dc=new BwtMtfRle(syntaxe[1]);
						dc.decompresser(syntaxe[2],VERB);
					}
					break;
				default:
					System.out.println("SAISIE NON VALIDE");
				}
			}
		}
		sc.close();
	}
}
