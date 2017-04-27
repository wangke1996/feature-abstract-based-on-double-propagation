package feature_abstract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Feature implements Serializable{
	public String feature;
	protected HashSet<String> related_opinion_words;//=new HashSet<String>();
	protected HashSet<String> related_target_words;//=new HashSet<String>();
	public HashMap<String,Integer> slave_feature_comp;//=new HashMap<String,Integer>();
	public HashMap<String,Integer> slave_feature_of;//=new HashMap<String,Integer>();
	public HashMap<String,Integer> slave_feature_intsec;//=new HashMap<String,Integer>();
	public HashMap<String,Integer> slave_feature_union;//=new HashMap<String,Integer>();
	public HashMap<String,Integer> master_feature_comp;//=new HashMap<String,Integer>();
	public HashMap<String,Integer> master_feature_of;//=new HashMap<String,Integer>();
	public HashMap<String,Integer> master_feature_intsec;//=new HashMap<String,Integer>();
	public HashMap<String,Integer> master_feature_union;//=new HashMap<String,Integer>();
	public Integer freq;
	public Feature(String s){
		feature=s;
		freq=0;
		related_opinion_words=new HashSet<String>();
		related_target_words=new HashSet<String>();
		slave_feature_comp=new HashMap<String,Integer>();
		slave_feature_of=new HashMap<String,Integer>();
		slave_feature_intsec=new HashMap<String,Integer>();
		slave_feature_union=new HashMap<String,Integer>();
		master_feature_comp=new HashMap<String,Integer>();
		master_feature_of=new HashMap<String,Integer>();
		master_feature_intsec=new HashMap<String,Integer>();
		master_feature_union=new HashMap<String,Integer>();
	}
	public void compute_intersection(){
		Iterator<Entry<String,Integer>> it=slave_feature_comp.entrySet().iterator();
		while(it.hasNext()){
			Entry<String,Integer> ent_slave_comp=it.next();
			if(!slave_feature_of.containsKey(ent_slave_comp.getKey()))
				continue;
			Integer freq=slave_feature_of.get(ent_slave_comp.getKey())+ent_slave_comp.getValue();
			slave_feature_intsec.put(ent_slave_comp.getKey(), freq);
		}
		it=master_feature_comp.entrySet().iterator();
		while(it.hasNext()){
			Entry<String,Integer> ent_master_comp=it.next();
			if(!master_feature_of.containsKey(ent_master_comp.getKey()))
				continue;
			Integer freq=master_feature_of.get(ent_master_comp.getKey())+ent_master_comp.getValue();
			master_feature_intsec.put(ent_master_comp.getKey(), freq);
		}
	}
	public void compute_union(){
		slave_feature_union.putAll(slave_feature_of);
		Iterator<Entry<String,Integer>> it=slave_feature_comp.entrySet().iterator();
		while(it.hasNext()){
			Entry<String,Integer> ent_slave_comp=it.next();
			if(!slave_feature_union.containsKey(ent_slave_comp.getKey())){
				slave_feature_union.put(ent_slave_comp.getKey(), ent_slave_comp.getValue());
				continue;
			}
			Integer freq=slave_feature_of.get(ent_slave_comp.getKey())+ent_slave_comp.getValue();
			slave_feature_union.put(ent_slave_comp.getKey(), freq);
		}
		master_feature_union.putAll(master_feature_of);
		it=master_feature_comp.entrySet().iterator();
		while(it.hasNext()){
			Entry<String,Integer> ent_master_comp=it.next();
			if(!master_feature_union.containsKey(ent_master_comp.getKey())){
				master_feature_union.put(ent_master_comp.getKey(), ent_master_comp.getValue());
				continue;
			}
			Integer freq=master_feature_of.get(ent_master_comp.getKey())+ent_master_comp.getValue();
			master_feature_union.put(ent_master_comp.getKey(), freq);
		}
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
		s+="]\n\tslave features by compound: [";
		
		//----sort slaves----//
		List<HashMap.Entry<String, Integer>> slaves_comp =
			    new ArrayList<HashMap.Entry<String, Integer>>(slave_feature_comp.entrySet());
		Collections.sort(slaves_comp, new Comparator<Map.Entry<String, Integer>>() { 
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
				return (o2.getValue() - o1.getValue()); 
				//return (o1.getKey()).toString().compareTo(o2.getKey());
			}
		}); 
		
		Iterator<HashMap.Entry<String,Integer>> iter=slaves_comp.iterator();
		while(iter.hasNext()){
			HashMap.Entry<String,Integer> ent=iter.next();
			s=s+ent.getKey()+"="+ent.getValue()+",";
		}
		if(!slave_feature_comp.isEmpty())
			s=s.substring(0,s.length()-1);
		
		s+="]\n\tslave features by of: [";
		List<HashMap.Entry<String, Integer>> slaves_of =
			    new ArrayList<HashMap.Entry<String, Integer>>(slave_feature_of.entrySet());
		Collections.sort(slaves_of, new Comparator<Map.Entry<String, Integer>>() { 
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
				return (o2.getValue() - o1.getValue()); 
				//return (o1.getKey()).toString().compareTo(o2.getKey());
			}
		}); 
		
		iter=slaves_of.iterator();
		while(iter.hasNext()){
			HashMap.Entry<String,Integer> ent=iter.next();
			s=s+ent.getKey()+"="+ent.getValue()+",";
		}
		if(!slave_feature_of.isEmpty())
			s=s.substring(0,s.length()-1);
		
		
		s+="]\n\tslave features by intsec: [";
		List<HashMap.Entry<String, Integer>> slaves_intsec =
			    new ArrayList<HashMap.Entry<String, Integer>>(slave_feature_intsec.entrySet());
		Collections.sort(slaves_intsec, new Comparator<Map.Entry<String, Integer>>() { 
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
				return (o2.getValue() - o1.getValue()); 
				//return (o1.getKey()).toString().compareTo(o2.getKey());
			}
		}); 
		
		iter=slaves_intsec.iterator();
		while(iter.hasNext()){
			HashMap.Entry<String,Integer> ent=iter.next();
			s=s+ent.getKey()+"="+ent.getValue()+",";
		}
		if(!slave_feature_intsec.isEmpty())
			s=s.substring(0,s.length()-1);
		
		s+="]\n\tslave features by unionset: [";
		List<HashMap.Entry<String, Integer>> slaves_union =
			    new ArrayList<HashMap.Entry<String, Integer>>(slave_feature_union.entrySet());
		Collections.sort(slaves_union, new Comparator<Map.Entry<String, Integer>>() { 
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
				return (o2.getValue() - o1.getValue()); 
				//return (o1.getKey()).toString().compareTo(o2.getKey());
			}
		}); 
		
		iter=slaves_union.iterator();
		while(iter.hasNext()){
			HashMap.Entry<String,Integer> ent=iter.next();
			s=s+ent.getKey()+"="+ent.getValue()+",";
		}
		if(!slave_feature_union.isEmpty())
			s=s.substring(0,s.length()-1);
		
		s+="]\n\tmaster features by compund: [";
		
		//----sort masters----//
		List<HashMap.Entry<String, Integer>> masters_comp =
			    new ArrayList<HashMap.Entry<String, Integer>>(master_feature_comp.entrySet());
		Collections.sort(masters_comp, new Comparator<Map.Entry<String, Integer>>() { 
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
				return (o2.getValue() - o1.getValue()); 
				//return (o1.getKey()).toString().compareTo(o2.getKey());
			}
		}); 
		
		iter=masters_comp.iterator();
		while(iter.hasNext()){
			HashMap.Entry<String,Integer> ent=iter.next();
			s=s+ent.getKey()+"="+ent.getValue()+",";			
		}
		if(!master_feature_comp.isEmpty())
			s=s.substring(0,s.length()-1);
		
		s+="]\n\tmaster features by of: [";
		List<HashMap.Entry<String, Integer>> masters_of =
			    new ArrayList<HashMap.Entry<String, Integer>>(master_feature_of.entrySet());
		Collections.sort(masters_of, new Comparator<Map.Entry<String, Integer>>() { 
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
				return (o2.getValue() - o1.getValue()); 
				//return (o1.getKey()).toString().compareTo(o2.getKey());
			}
		}); 
		
		iter=masters_of.iterator();
		while(iter.hasNext()){
			HashMap.Entry<String,Integer> ent=iter.next();
			s=s+ent.getKey()+"="+ent.getValue()+",";			
		}
		if(!master_feature_of.isEmpty())
			s=s.substring(0,s.length()-1);
		

		s+="]\n\tmaster features by intsec: [";
		List<HashMap.Entry<String, Integer>> masters_intsec =
			    new ArrayList<HashMap.Entry<String, Integer>>(master_feature_intsec.entrySet());
		Collections.sort(masters_intsec, new Comparator<Map.Entry<String, Integer>>() { 
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
				return (o2.getValue() - o1.getValue()); 
				//return (o1.getKey()).toString().compareTo(o2.getKey());
			}
		}); 
		
		iter=masters_intsec.iterator();
		while(iter.hasNext()){
			HashMap.Entry<String,Integer> ent=iter.next();
			s=s+ent.getKey()+"="+ent.getValue()+",";			
		}
		if(!master_feature_intsec.isEmpty())
			s=s.substring(0,s.length()-1);
		
		s+="]\n\tmaster features by unionset: [";
		List<HashMap.Entry<String, Integer>> masters_union =
			    new ArrayList<HashMap.Entry<String, Integer>>(master_feature_union.entrySet());
		Collections.sort(masters_union, new Comparator<Map.Entry<String, Integer>>() { 
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
				return (o2.getValue() - o1.getValue()); 
				//return (o1.getKey()).toString().compareTo(o2.getKey());
			}
		}); 
		
		iter=masters_union.iterator();
		while(iter.hasNext()){
			HashMap.Entry<String,Integer> ent=iter.next();
			s=s+ent.getKey()+"="+ent.getValue()+",";			
		}
		if(!master_feature_union.isEmpty())
			s=s.substring(0,s.length()-1);
		
		s+="]";
		System.out.println(s);
		return s;
	}
	
}
