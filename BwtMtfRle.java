
public class BwtMtfRle extends Compression{
	public BwtMtfRle(String ad){
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
		RLE rle=new RLE(_depart+"2");
		rle.compresser(_depart+"c", false);
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
		RLE rle=new RLE(_depart);
		rle.decompresser(_depart+"1", false);
		MTF mtfc=new MTF(_depart+"1");
		mtfc.decompresser(_depart+"2", false);
		BurrowsWheelerComp bwc=new BurrowsWheelerComp(_depart+"2");
		bwc.decompresser(fin, false);
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
		BwtMtfRle b=new BwtMtfRle("");
		b.tester("test/h.html","test/2h.html",true);
	}
}
