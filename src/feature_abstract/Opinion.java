package feature_abstract;

import java.util.HashSet;
import java.util.Iterator;

public class Opinion {
	protected String opinion;
	protected HashSet<String> related_opinion_words=new HashSet<String>();
	protected HashSet<String> related_target_words=new HashSet<String>();
	protected Integer freq;
	protected Integer polar;
	public Opinion(String s,Integer i){
		opinion=s;
		freq=0;
		polar=i;
	}
	public String display(){
		String s=opinion+"="+freq+" [";
		Iterator<String> it=related_opinion_words.iterator();
		while(it.hasNext())
			s=s+it.next()+",";
		if(!related_opinion_words.isEmpty())
			s=s.substring(0, s.length()-1);
		s+="] [";
		it=related_target_words.iterator();
		while(it.hasNext())
			s=s+it.next()+",";
		if(!related_target_words.isEmpty())
			s=s.substring(0, s.length()-1);
		s+="]";
		s=s+" polar:"+polar;
		System.out.println(s);
		return s;
	}
}
