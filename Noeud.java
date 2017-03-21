import java.util.*;

public class Noeud<T> {
	public Noeud<T> droite;
	public Noeud<T> gauche;
	public int poid;
	public T valeur;
	public final T DEF=null;
	
	public Noeud(Noeud<T> tdroite, Noeud<T> tgauche){
		droite=tdroite;
		gauche=tgauche;
		valeur=DEF;
		if(droite!=null && gauche!=null)
			poid=droite.poid+gauche.poid;
		else poid=0;
	}
	
	public Noeud(T tvaleur, int frequence){
		poid=frequence;
		valeur=tvaleur;
		droite=null;
		gauche=null;
	}
	
	public String toString(){
		return ""+poid;
	}
}

class NoeudComparator<T> implements Comparator<Noeud<T>>{
	public int compare(Noeud<T> n1, Noeud<T> n2){
		if(n1.poid>n2.poid)return 1;
		if(n1.poid==n2.poid)return 0;
		return -1;
	}
}
