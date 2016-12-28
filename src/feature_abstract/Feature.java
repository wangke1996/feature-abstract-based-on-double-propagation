package feature_abstract;

import java.util.HashSet;
import java.util.Iterator;

public class Feature {
	protected String feature;
	protected HashSet<String> related_opinion_words=new HashSet<String>();
	protected HashSet<String> related_target_words=new HashSet<String>();
	protected HashSet<String> slave_feature=new HashSet<String>();
	protected HashSet<String> master_feature=new HashSet<String>();
	protected Integer freq;
	public Feature(String s){
		feature=s;
		freq=0;
	}
	public String display(){
		String s=feature+"="+freq+"\n\trelated opinion words: [";
		Iterator<String> it=related_opinion_words.iterator();
		while(it.hasNext())
			s=s+it.next()+",";
		if(!related_opinion_words.isEmpty())
			s=s.substring(0, s.length()-1);
		s+="]\n\trelated target words: [";
		it=related_target_words.iterator();
		while(it.hasNext())
			s=s+it.next()+",";
		if(!related_target_words.isEmpty())
			s=s.substring(0, s.length()-1);
		s+="]\n\tslave features: [";
		it=slave_feature.iterator();
		while(it.hasNext())
			s=s+it.next()+",";
		if(!slave_feature.isEmpty())
			s=s.substring(0,s.length()-1);
		s+="]\n\tmaster features: [";
		it=master_feature.iterator();
		while(it.hasNext())
			s=s+it.next()+",";
		if(!master_feature.isEmpty())
			s=s.substring(0,s.length()-1);
		s+="]";
		System.out.println(s);
		return s;
	}
	
}
