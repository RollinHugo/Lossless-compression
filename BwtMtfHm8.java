
public class BwtMtfHm8 extends Compression{
	public BwtMtfHm8(String ad){
		_depart=ad;
	}
	
	public void compresser(String fin, boolean v){
		System.out.println("----DEBUT DE COMPRESSION COMBINÈES---");
		long temps=-System.currentTimeMillis();
		// ce n'est qu'une combinaison de 3 classes.
		BurrowsWheelerComp bwc=new BurrowsWheelerComp(_depart);
		bwc.compresser(_depart+"1", false);
		MTF mtfc=new MTF(_depart+"1");
		mtfc.compresser(_depart+"2", false);
		HuffmanComp8 hc8=new HuffmanComp8(_depart+"2");
		hc8.compresser(_depart+"c", false);
		// on supprime les fichiers temporaires
		try{
			Runtime.getRuntime().exec("rm "+_depart+"1");
			Runtime.getRuntime().exec("rm "+_depart+"2");
			System.out.println("Sup ok");
		}
		catch(Exception e){
			System.out.println("Erreur sup "+e);
		}
		CSP.tauxComp(_depart, _depart+"c");
		System.out.println("----FIN DE COMPRESSION COMBINÈES---");
		temps+=System.currentTimeMillis();
		System.out.println("Fin de la compression en "+temps+" ms");
	}
	public void decompresser(String fin, boolean v){
		System.out.println("----DEBUT DE DÈCOMPRESSION COMBINÈES---");
		long temps=-System.currentTimeMillis();
		// mÍme mÈthode que pour la compression
		HuffmanComp8 bwc=new HuffmanComp8(_depart);
		bwc.decompresser(_depart+"1", false);
		MTF mtfc=new MTF(_depart+"1");
		mtfc.decompresser(_depart+"2", false);
		BurrowsWheelerComp hc8=new BurrowsWheelerComp(_depart+"2");
		hc8.decompresser(fin, false);
		try{
			Runtime.getRuntime().exec("rm "+_depart+"1");
			Runtime.getRuntime().exec("rm "+_depart+"2");
			System.out.println(">> Supression des fichiers temporaires OK");
		}
		catch(Exception e){
			System.out.println("Erreur supression "+e);
		}
		System.out.println("----FIN DE DÈCOMPRESSION COMBINÈES---");
		temps+=System.currentTimeMillis();
		System.out.println("Fin de la compression en "+temps+" ms");
	}
	
	public void raz(){
		
	}
	
	public static void main(String[] args){
		BwtMtfHm8 b=new BwtMtfHm8("");
		b.tester("test/h.html","test/2h.html",true);
	}
}
