package feature_abstract;
//import java.io.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.w3c.dom.NodeList;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.LabeledWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;

public class Read_Custom_Review {
	
	protected static String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	protected static LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);		
	protected static TokenizerFactory<CoreLabel> tokenizerFactory =
            PTBTokenizer.factory(new CoreLabelTokenFactory(), "");	
	
	protected static List<Sentence>  All_sentences=new ArrayList<Sentence>();
	
	protected static HashMap<String,Opinion> LEXICON=new HashMap<String,Opinion>();
	protected static HashMap<String,Opinion> OPINION=new HashMap<String,Opinion>();
	protected static HashMap<String,Opinion> OPINION_NEW=new HashMap<String,Opinion>();
	protected static HashMap<String,Feature> FEATURE=new HashMap<String,Feature>();
	protected static HashSet<String> STOP_WORD=new HashSet<String>(){{add("i");/*add("phone");add("iphone");add("smartphone");*/add("thing");add("anything");add("product");add("problem");add("deal");}};
	protected static HashSet<String> STOP_TAG=new HashSet<String>(){{add("PRP");add("PRP$");add(null);}};
	protected static HashSet<String> JJ=new HashSet<String>(){{add("JJ");}};//add("JJR");add("JJS");}};
	protected static HashSet<String> NN=new HashSet<String>(){{add("NN");}};//add("NNS");}};	
	protected static HashSet<String> MR=new HashSet<String>(){{add("amod");add("nusbj");}};//add("prep");add("nsubj");add("csubj");add("xsubj");add("dobj");add("iobj");}};
	protected static HashSet<String> CONJ=new HashSet<String>();
	
	protected static List<Entry<String,Feature>> Feature_sorted;
	protected static List<Vector<Integer>> sentence_vec;
	protected static List<Vector<Integer>> context_vec;
	protected static List<HashSet<String>> clusters=new ArrayList<HashSet<String>>();
	
	protected static Feature_tree tree;
	public static /*List<Sentence> */void read_custome_review(String path,int max_review_num)
	{
		//int max_review_num=300;
		try{
			FileInputStream fis = new FileInputStream(path);   
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");   
			BufferedReader br=new BufferedReader(isr);
			String s=null;
			int i=0;
			while((s=br.readLine())!=null&&i<max_review_num){
				if(s.startsWith("Text:\t")){
					s=s.substring(6).toLowerCase();
					String[] sentences=s.split("\\.|!|\\?|\t");
					for(String s1:sentences){
						if(s1.length()<5)
							continue;
						Sentence sentence=new Sentence(s1,lp,tokenizerFactory);
						/*List<Tree> Nodes=sentence.parse.preOrderNodeList();
						for(Tree node:Nodes){
							if(node.isLeaf())
								continue;
							if(node.label().toString().charAt(0)=='N')
							{
								String noun="";
								//System.out.println(node.label().toString()+":"+node.toString()+"\n");
								List<Tree> leaf_words=node.getLeaves();
								for(Tree leaf:leaf_words)
									noun=noun+leaf.label().toString()+' ';
								noun=noun.substring(0, noun.length()-1);
								sentence.nouns.add(noun);
							}
						}
						System.out.println(sentence.parse.toString());*/
						//r1_and_r4(sentence);
						All_sentences.add(sentence);
					}
					i++;
				}
			}
			br.close();
			isr.close();
			fis.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		//return All_sentences;
	}
	public static void read_opinion_lexicon(String path_pos,String path_neg){
		try{
			FileReader fr1=new FileReader(path_pos);
			BufferedReader br1=new BufferedReader(fr1);
			FileReader fr2=new FileReader(path_neg);
			BufferedReader br2=new BufferedReader(fr2);
			String s=null;
			while((s=br1.readLine())!=null){
				if(s.isEmpty()||s.startsWith(";"))
					continue;
				Opinion op=new Opinion(s,1);
				LEXICON.put(s, op);
			}
			while((s=br2.readLine())!=null){
				if(s.isEmpty()||s.startsWith(";"))
					continue;
				Opinion op=new Opinion(s,-1);
				LEXICON.put(s, op);
			}
			br1.close();
			fr1.close();
			br2.close();
			fr2.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public static List<Integer> r1_and_r4(Sentence sent){
		Integer new_feature_num=0,new_opinion_num=0;
		Iterator<TypedDependency> it=sent.dependence.iterator();
		while(it.hasNext()){
			TypedDependency o_dep=it.next();
			if(STOP_TAG.contains(o_dep.dep().tag())){
				STOP_WORD.add(o_dep.dep().value());
				continue;
			}
			if(STOP_TAG.contains(o_dep.gov().tag())){
				STOP_WORD.add(o_dep.gov().value());
				continue;
			}
			if(STOP_WORD.contains(o_dep.dep().value())||STOP_WORD.contains(o_dep.gov().value()))
				continue;
			String reln=o_dep.reln().toString();
			String[] dep_word_and_tag=o_dep.dep().toString().split("/");
			String[] gov_word_and_tag=o_dep.gov().toString().split("/");
			if(dep_word_and_tag.length<2||gov_word_and_tag.length<2)
				continue;
			if(MR.contains(reln)&&OPINION.containsKey(dep_word_and_tag[0])){
					//---- O->O-DEP->T, t=T ----//
//				if(NN.contains(gov_word_and_tag[1])
//						&&!(FEATURE.containsKey(gov_word_and_tag[0])))
//					{FEATURE.put(gov_word_and_tag[0],0);new_feature_num++;}
				if(NN.contains(o_dep.gov().tag())){
					if(!FEATURE.containsKey(o_dep.gov().value()))
						{FEATURE.put(o_dep.gov().value(), new Feature(o_dep.gov().value()));new_feature_num++;}
					Feature f=FEATURE.get(o_dep.gov().value());
					f.related_opinion_words.add(o_dep.dep().value());
					f.freq++;
					FEATURE.put(o_dep.gov().value(), f);
				}
					//---- O->O->DEP->H<-T-DEP<-T, t=T ----//
				int H_id=o_dep.gov().hashCode()-1;
				Iterator<TypedDependency> iter = sent.gov_of_which.get(H_id).iterator();
				while(iter.hasNext()){
					TypedDependency t_dep=iter.next();
					if(!MR.contains(t_dep.reln().toString()))
						continue;
					if(STOP_TAG.contains(t_dep.dep().tag())){
						STOP_WORD.add(t_dep.dep().value());
						continue;
					}
					if(STOP_WORD.contains(t_dep.dep().value()))
						continue;
					String[] t_word_and_tag=t_dep.dep().toString().split("/");
					if(t_word_and_tag.length<2)
						continue;
//					if(NN.contains(t_word_and_tag[1])
//							&&!(FEATURE.containsKey(t_word_and_tag[0])))
//						{FEATURE.put(t_word_and_tag[0],0);new_feature_num++;}
					if(NN.contains(t_dep.dep().tag())){
						if(!FEATURE.containsKey(t_dep.dep().value()))
							{FEATURE.put(t_dep.dep().value(), new Feature(t_dep.dep().value()));new_feature_num++;}
						Feature f=FEATURE.get(t_dep.dep().value());
						f.related_opinion_words.add(o_dep.dep().value());
						f.freq++;
						FEATURE.put(t_dep.dep().value(), f);
					}
				}
			}
			//---- Oi -> O-dep -> Oj ----//
			if(CONJ.contains(reln.toString())){
				if(JJ.contains(dep_word_and_tag[1])
						&&JJ.contains(gov_word_and_tag[1])){
//					if(OPINION.containsKey(dep_word_and_tag[0])&&!OPINION.containsKey(gov_word_and_tag[0])){	
//						{OPINION.put(gov_word_and_tag[0], 0);new_opinion_num++;}
//					else if(!OPINION.containsKey(dep_word_and_tag[0])&&OPINION.containsKey(gov_word_and_tag[0]))
//						{OPINION.put(dep_word_and_tag[0], 0);new_opinion_num++;}
					if(OPINION.containsKey(o_dep.dep().value())){
						if(!OPINION.containsKey(o_dep.gov().value()))
							OPINION.put(o_dep.gov().value(), new Opinion(o_dep.gov().value(),0));
						Opinion op=OPINION.get(o_dep.gov().value());
						op.related_opinion_words.add(o_dep.dep().value());
						op.freq++;
						OPINION.put(o_dep.gov().value(), op);
						new_opinion_num++;
					}
					else if(OPINION.containsKey(o_dep.gov().value())){
						if(!OPINION.containsKey(o_dep.dep().value())){
							OPINION.put(o_dep.dep().value(), new Opinion(o_dep.dep().value(),0));
							new_opinion_num++;
						}							
						Opinion op=OPINION.get(o_dep.dep().value());
						op.related_opinion_words.add(o_dep.gov().value());
						op.freq++;
						OPINION.put(o_dep.dep().value(), op);
					}
				}
			}
			//---- Oi -> Oi-dep -> H <- Oj-dep <- Oj ----//
			if(JJ.contains(dep_word_and_tag[1])&&OPINION.containsKey(dep_word_and_tag[0])){
				int H_id=o_dep.gov().hashCode()-1;
				Iterator<TypedDependency> iter = sent.gov_of_which.get(H_id).iterator();
				while(iter.hasNext()){
					TypedDependency t_dep=iter.next();
					if(!(t_dep.reln().toString().equals(o_dep.reln().toString())))
						continue;
					if(t_dep.dep().value().equals(o_dep.dep().value()))
						continue;

					if(STOP_TAG.contains(t_dep.dep().tag())){
						STOP_WORD.add(t_dep.dep().value());
						continue;
					}
					if(STOP_WORD.contains(t_dep.dep().value()))
						continue;
					
					String[] t_word_and_tag=t_dep.dep().toString().split("/");
					if(t_word_and_tag.length<2)
						continue;
//					if(JJ.contains(t_word_and_tag[1])
//							&&!(OPINION.containsKey(t_word_and_tag[0])))
//						{OPINION.put(t_word_and_tag[0], 0);new_opinion_num++;}
					if(JJ.contains(t_dep.dep().tag())){
						if(!OPINION.containsKey(t_dep.dep().value())){
							OPINION.put(t_dep.dep().value(), new Opinion(t_dep.dep().value(),0));
							new_opinion_num++;
						}							
						Opinion op=OPINION.get(t_dep.dep().value());
						op.related_opinion_words.add(o_dep.dep().value());
						op.freq++;
						OPINION.put(t_dep.dep().value(), op);
					}
				}
			}
		}
		List<Integer> new_word_num=new ArrayList<Integer>();
		new_word_num.add(new_feature_num);
		new_word_num.add(new_opinion_num);
		return new_word_num;
	}
	public static List<Integer> r3_and_r2(Sentence sent){
		Integer new_feature_num=0,new_opinion_num=0;
		Iterator<TypedDependency> it=sent.dependence.iterator();
		while(it.hasNext()){
			TypedDependency dep_1=it.next();
			if(STOP_TAG.contains(dep_1.dep().tag())){
				STOP_WORD.add(dep_1.dep().value());
				continue;
			}
			if(STOP_TAG.contains(dep_1.gov().tag())){
				STOP_WORD.add(dep_1.gov().value());
				continue;
			}
			if(STOP_WORD.contains(dep_1.dep().value())||STOP_WORD.contains(dep_1.gov().value()))
				continue;
			String reln=dep_1.reln().toString();
			String[] dep_word_and_tag=dep_1.dep().toString().split("/");
			String[] gov_word_and_tag=dep_1.gov().toString().split("/");
			if(dep_word_and_tag.length<2||gov_word_and_tag.length<2)
				continue;
			//---- Ti -> {CONJ} -> Tj, t==Ti ----// R31
			if(CONJ.contains(reln.toString())){
				if(NN.contains(dep_word_and_tag[1])
						&&NN.contains(gov_word_and_tag[1])){
//					if(FEATURE.containsKey(dep_word_and_tag[0])&&!FEATURE.containsKey(gov_word_and_tag[0]))	
//						{FEATURE.put(gov_word_and_tag[0],0);new_feature_num++;}
//					else if(!FEATURE.containsKey(dep_word_and_tag[0])&&FEATURE.containsKey(gov_word_and_tag[0]))
//						{FEATURE.put(dep_word_and_tag[0],0);new_feature_num++;}
					if(FEATURE.containsKey(dep_1.dep().value())){
						if(!FEATURE.containsKey(dep_1.gov().value()))
							{FEATURE.put(dep_1.gov().value(), new Feature(dep_1.gov().value()));new_feature_num++;}
						Feature f=FEATURE.get(dep_1.gov().value());
						f.related_target_words.add(dep_1.dep().value());
						f.freq++;
						FEATURE.put(dep_1.gov().value(), f);
					}
					else if(FEATURE.containsKey(dep_1.gov().value())){
						if(!FEATURE.containsKey(dep_1.dep().value()))
							{FEATURE.put(dep_1.dep().value(), new Feature(dep_1.dep().value()));new_feature_num++;}
						Feature f=FEATURE.get(dep_1.dep().value());
						f.related_target_words.add(dep_1.gov().value());
						f.freq++;
						FEATURE.put(dep_1.dep().value(), f);
					}
				}
			}
			//---- Ti -> Ti_dep -> H <- Tj_dep <- Tj, Ti_dep=Tj_dep, t=Ti ----// R32
			if(NN.contains(dep_word_and_tag[1])&&FEATURE.containsKey(dep_word_and_tag[0])){
				int H_id=dep_1.gov().hashCode()-1;
				Iterator<TypedDependency> iter = sent.gov_of_which.get(H_id).iterator();
				while(iter.hasNext()){
					TypedDependency dep_2=iter.next();
					if(STOP_TAG.contains(dep_2.dep().tag())){
						STOP_WORD.add(dep_2.dep().value());
						continue;
					}
					if(STOP_WORD.contains(dep_2.dep().value()))
						continue;
					if(!(dep_2.reln().toString().equals(dep_1.reln().toString())))
						continue;
					if(dep_2.dep().value().equals(dep_1.dep().value()))
						continue;
					String[] t_word_and_tag=dep_2.dep().toString().split("/");
					if(t_word_and_tag.length<2)
						continue;
//					if(NN.contains(t_word_and_tag[1])
//							&&!(FEATURE.containsKey(t_word_and_tag[0])))
//						{FEATURE.put(t_word_and_tag[0],0);new_feature_num++;}
					if(NN.contains(dep_2.dep().tag())){
						if(!FEATURE.containsKey(dep_2.dep().value()))
							{FEATURE.put(dep_2.dep().value(), new Feature(dep_2.dep().value()));new_feature_num++;}
						Feature f=FEATURE.get(dep_2.dep().value());
						f.related_target_words.add(dep_1.dep().value());
						f.freq++;
						FEATURE.put(dep_2.dep().value(), f);
					}
				}
			}
			
			//----- R21 R22 ----//
			if(MR.contains(reln)&&FEATURE.containsKey(dep_word_and_tag[0])){
				//---- O->O-DEP->T, o=O ----//
//				if(JJ.contains(gov_word_and_tag[1])
//					&&!(OPINION.containsKey(gov_word_and_tag[0])))
//					{OPINION.put(gov_word_and_tag[0],0);new_opinion_num++;}
				if(JJ.contains(dep_1.gov().tag())){
					if(!OPINION.containsKey(dep_1.gov().value())){
						OPINION.put(dep_1.gov().value(), new Opinion(dep_1.gov().value(),0));
						new_opinion_num++;
					}
					Opinion op=OPINION.get(dep_1.gov().value());
					op.related_target_words.add(dep_1.dep().value());
					op.freq++;
					OPINION.put(dep_1.gov().value(), op);
				}
				//---- O->O->DEP->H<-T-DEP<-T, o=O ----//
				int H_id=dep_1.gov().hashCode()-1;
				Iterator<TypedDependency> iter = sent.gov_of_which.get(H_id).iterator();
				while(iter.hasNext()){
					TypedDependency dep_2=iter.next();
					if(!MR.contains(dep_2.reln().toString()))
						continue;
					if(dep_1.dep().value().equals(dep_2.dep().value()))
						continue;
					if(STOP_TAG.contains(dep_2.dep().tag())){
						STOP_WORD.add(dep_2.dep().value());
						continue;
					}
					if(STOP_WORD.contains(dep_2.dep().value()))
						continue;
					if(dep_2.dep().value().equals(dep_1.dep().value()))
						continue;
					String[] o_word_and_tag=dep_2.dep().toString().split("/");
					if(o_word_and_tag.length<2)
						continue;
//					if(JJ.contains(o_word_and_tag[1])
//						&&!(OPINION.containsKey(o_word_and_tag[0])))
//						{OPINION.put(o_word_and_tag[0],0);new_opinion_num++;}
					if(JJ.contains(dep_2.dep().tag())){
						if(!OPINION.containsKey(dep_2.dep().value())){
							OPINION.put(dep_2.dep().value(), new Opinion(dep_2.dep().value(),0));
							new_opinion_num++;
						}
						Opinion op=OPINION.get(dep_2.dep().value());
						op.related_target_words.add(dep_1.dep().value());
						op.freq++;
						OPINION.put(dep_2.dep().value(), op);
					}
				}
			}
		}
		List<Integer> new_word_num=new ArrayList<Integer>();
		new_word_num.add(new_feature_num);
		new_word_num.add(new_opinion_num);
		return new_word_num;
	}
	public static void r11(Sentence sent){
		
		Iterator<TypedDependency> it=sent.dependence.iterator();
		while(it.hasNext()){
			TypedDependency o_dep=it.next();
			if(STOP_TAG.contains(o_dep.dep().tag())){
				STOP_WORD.add(o_dep.dep().value());
				continue;
			}
			if(STOP_TAG.contains(o_dep.gov().tag())){
				STOP_WORD.add(o_dep.gov().value());
				continue;
			}
			if(STOP_WORD.contains(o_dep.dep().value())||STOP_WORD.contains(o_dep.gov().value()))
				continue;
			String reln=o_dep.reln().toString();
			if(o_dep.dep().tag()==null||o_dep.gov().tag()==null)
				continue;
			if(MR.contains(reln)&&OPINION.containsKey(o_dep.dep().value())){
					//---- O->O-DEP->T, t=T ----//
				if(NN.contains(o_dep.gov().tag())){
					if(!FEATURE.containsKey(o_dep.gov().value()))
						{FEATURE.put(o_dep.gov().value(), new Feature(o_dep.gov().value()));}
					Feature f=FEATURE.get(o_dep.gov().value());
					f.related_opinion_words.add(o_dep.dep().value());
					f.freq++;
					FEATURE.put(o_dep.gov().value(), f);
				}
			}
		}
	}
	public static void r11_and_r12(Sentence sent){
		Iterator<TypedDependency> it=sent.dependence.iterator();
		while(it.hasNext()){
			TypedDependency o_dep=it.next();
			if(STOP_TAG.contains(o_dep.dep().tag())){
				STOP_WORD.add(o_dep.dep().value());
				continue;
			}
			if(STOP_TAG.contains(o_dep.gov().tag())){
				STOP_WORD.add(o_dep.gov().value());
				continue;
			}
			if(STOP_WORD.contains(o_dep.dep().value())||STOP_WORD.contains(o_dep.gov().value()))
				continue;
			String reln=o_dep.reln().toString();
			if(o_dep.dep().tag()==null||o_dep.gov().tag()==null)
				continue;
			if(MR.contains(reln)&&OPINION.containsKey(o_dep.dep().value())){
					//---- O->O-DEP->T, t=T ----//
				if(NN.contains(o_dep.gov().tag())){
					if(!FEATURE.containsKey(o_dep.gov().value()))
						{FEATURE.put(o_dep.gov().value(), new Feature(o_dep.gov().value()));}
					Feature f=FEATURE.get(o_dep.gov().value());
					f.related_opinion_words.add(o_dep.dep().value());
					f.freq++;
					FEATURE.put(o_dep.gov().value(), f);
				}
					//---- O->O->DEP->H<-T-DEP<-T, t=T ----//
				int H_id=o_dep.gov().index()-1;
				if(H_id<0)
					continue;
				Iterator<TypedDependency> iter = sent.gov_of_which.get(H_id).iterator();
				while(iter.hasNext()){
					TypedDependency t_dep=iter.next();
					if(!MR.contains(t_dep.reln().toString()))
						continue;
					if(STOP_TAG.contains(t_dep.dep().tag())){
						STOP_WORD.add(t_dep.dep().value());
						continue;
					}
					if(STOP_WORD.contains(t_dep.dep().value()))
						continue;
					if(t_dep.dep().tag()==null)
						continue;
					if(NN.contains(t_dep.dep().tag())){
						if(!FEATURE.containsKey(t_dep.dep().value()))
							{FEATURE.put(t_dep.dep().value(), new Feature(t_dep.dep().value()));}
						Feature f=FEATURE.get(t_dep.dep().value());
						f.related_opinion_words.add(o_dep.dep().value());
						f.freq++;
						FEATURE.put(t_dep.dep().value(), f);
					}
				}
			}
		}
	}
	public static String prun1(){
		String logout="";
		Iterator<Sentence> it_sent=All_sentences.iterator();
		while(it_sent.hasNext()){
			Sentence sent=it_sent.next();
			Iterator<CoreLabel> it_word=sent.rawWords.iterator();
			int target_no=0;
			CoreLabel real_target=null;
			while(it_word.hasNext()){
				CoreLabel word=it_word.next();
				if(!(FEATURE.containsKey(word.value())))
					continue;
				if(word.index()<1)
					continue;
				target_no++;
				if(target_no==2){
					if(word.value().equals(real_target.value())){
						target_no=1;
						continue;
					}
					boolean flag_both_remain=false;
					Iterator<TypedDependency> it_depend=sent.gov_of_which.get(word.index()-1).iterator();
					while(it_depend.hasNext()){
						TypedDependency depend=it_depend.next();
						if(depend.dep().value().equals(real_target.value())&&JJ.contains(depend.reln().toString())){
							flag_both_remain=true;
							break;
						}
					}
					if(!flag_both_remain){
						it_depend=sent.gov_of_which.get(real_target.index()-1).iterator();
						while(it_depend.hasNext()){
							TypedDependency depend=it_depend.next();
							if(depend.dep().value().equals(word.value())&&JJ.contains(depend.reln().toString())){
								flag_both_remain=true;
								break;
							}
						}
					}
					if(!flag_both_remain){
						if(FEATURE.get(real_target.value()).freq>FEATURE.get(word.value()).freq)
							{System.out.println(word.value()+"="+FEATURE.get(word.value()).freq+" removed");
							logout=logout+word.value()+"="+FEATURE.get(word.value()).freq+" removed\n";
							FEATURE.remove(word.value());}
						else{
							{System.out.println(real_target.value()+"="+FEATURE.get(real_target.value()).freq+" removed");
							logout=logout+real_target.value()+"="+FEATURE.get(real_target.value()).freq+" removed\n";
							FEATURE.remove(real_target.value());}
							real_target=word;
						}
					}
					else{
						if(FEATURE.get(real_target.value()).freq<FEATURE.get(word.value()).freq)
							real_target=word;
					}
					target_no=1;
				}
				else
					real_target=word;
			}
		}
		return logout;
	}
	public static void sub_feature_abstract(){
		Iterator<Sentence> it_sent=All_sentences.iterator();
		while(it_sent.hasNext()){
			Sentence sent=it_sent.next();
			Iterator<TypedDependency> it_depend=sent.dependence.iterator();
			while(it_depend.hasNext()){
				TypedDependency depend=it_depend.next();
				if(STOP_WORD.contains(depend.dep().value())||STOP_WORD.contains(depend.gov().value()))
					continue;
				if(depend.dep().tag()==null||depend.gov().tag()==null)
					continue;
				//---- master->compound->slave ----//
				if(depend.reln().toString().equals("compound")&&FEATURE.containsKey(depend.dep().value())&&FEATURE.containsKey(depend.gov().value())){
					Feature f_master=FEATURE.get(depend.dep().value());
					Feature f_slave=FEATURE.get(depend.gov().value());
					if(f_master.freq<=10||f_slave.freq<=10)
						continue;
					if(!f_master.slave_feature_comp.containsKey(f_slave.feature)){
						f_master.slave_feature_comp.put(f_slave.feature, 0);
						f_slave.master_feature_comp.put(f_master.feature, 0);
					}
					Integer freq_slave=f_master.slave_feature_comp.get(f_slave.feature);
					freq_slave++;
					f_master.slave_feature_comp.put(f_slave.feature,freq_slave);
					FEATURE.put(f_master.feature, f_master);
					f_slave.master_feature_comp.put(f_master.feature,freq_slave);
					FEATURE.put(f_slave.feature, f_slave);
				}
				
				//---- master->nmod:of->slave ----//
				if(depend.reln().toString().equals("nmod:of")
						&&NN.contains(depend.dep().tag())
						&&NN.contains(depend.gov().tag())
						&&FEATURE.containsKey(depend.dep().value())
						&&FEATURE.containsKey(depend.gov().value())){
					//there should be some filters, such that the frequency of slave and master cannot have a huge difference, and the frequency of the master should not be too low
					Feature f_master=FEATURE.get(depend.dep().value());
					Feature f_slave=FEATURE.get(depend.gov().value());
					if(f_master.freq<=10||f_slave.freq<=10)
						continue;
					if(!f_master.slave_feature_of.containsKey(f_slave.feature)){
						f_master.slave_feature_of.put(f_slave.feature, 0);
						f_slave.master_feature_of.put(f_master.feature, 0);
					}
					Integer freq_slave=f_master.slave_feature_of.get(f_slave.feature);
					freq_slave++;
					f_master.slave_feature_of.put(f_slave.feature,freq_slave);
					FEATURE.put(f_master.feature, f_master);
					f_slave.master_feature_of.put(f_master.feature,freq_slave);
					FEATURE.put(f_slave.feature, f_slave);
					
					//---- master->nmod:of->slave1<-conj->slave2 ----//
					int slave1_id=depend.gov().index();
					if(slave1_id<0)
						continue;
					Iterator<TypedDependency> it_dep_slave=sent.dependence.iterator();
					while(it_dep_slave.hasNext()){
						TypedDependency dep_slave=it_dep_slave.next();
						if(CONJ.contains(dep_slave.reln().toString())&&NN.contains(dep_slave.dep().tag())&&NN.contains(dep_slave.gov().tag())){
							if(dep_slave.dep().index()==slave1_id&&FEATURE.containsKey(dep_slave.gov().value())){
								Feature f_slave2=FEATURE.get(dep_slave.gov().value());
								if(f_slave2.freq<=10)
									continue;
								if(!f_master.slave_feature_of.containsKey(f_slave2.feature)){
									f_master.slave_feature_of.put(f_slave2.feature, 0);
									f_slave2.master_feature_of.put(f_master.feature, 0);
								}
								Integer freq_slave2=f_master.slave_feature_of.get(f_slave2.feature);
								freq_slave2++;
								f_master.slave_feature_of.put(f_slave2.feature,freq_slave2);
								FEATURE.put(f_master.feature, f_master);
								f_slave2.master_feature_of.put(f_master.feature,freq_slave2);
								FEATURE.put(f_slave2.feature, f_slave2);
								continue;
							}
							if(dep_slave.gov().index()==slave1_id&&FEATURE.containsKey(dep_slave.dep().value())){
								Feature f_slave2=FEATURE.get(dep_slave.dep().value());
								if(f_slave2.freq<=10)
									continue;
								if(!f_master.slave_feature_of.containsKey(f_slave2.feature)){
									f_master.slave_feature_of.put(f_slave2.feature, 0);
									f_slave2.master_feature_of.put(f_master.feature, 0);
								}
								Integer freq_slave2=f_master.slave_feature_of.get(f_slave2.feature);
								freq_slave2++;
								f_master.slave_feature_of.put(f_slave2.feature,freq_slave2);
								FEATURE.put(f_master.feature, f_master);
								f_slave2.master_feature_of.put(f_master.feature,freq_slave2);
								FEATURE.put(f_slave2.feature, f_slave2);
								continue;								
							}
						}
					}
				}
			}
		}
		//----intersection of slave_comp and slave_of----//
		Iterator<Entry<String,Feature>> it_feature=FEATURE.entrySet().iterator();
		while(it_feature.hasNext()){
			Entry<String,Feature> ent=it_feature.next();
			ent.getValue().compute_intersection();
			ent.getValue().compute_union();
		}
	}
	
	public static class Feature_tree {
		protected Feature_tree parent;
		protected String name;
		protected int level;
		protected HashSet<Feature_tree> slaves=new HashSet<Feature_tree>();
		protected Feature node;
		public Feature_tree(Feature root){
			parent=null;
			name=root.feature;
			node=root;
			level=1;
		}
		public void get_children(){
			Iterator<Entry<String,Integer>> it=node.slave_feature_intsec.entrySet().iterator();
			//Iterator<Entry<String,Integer>> it=node.slave_feature_union.entrySet().iterator();
			while(it.hasNext()){
				Entry<String,Integer> ent=it.next();
				String slave_key=ent.getKey();
				//----kick of slaves have low frequency as well as an empty slave list----//
				if(FEATURE.get(ent.getKey()).slave_feature_intsec.isEmpty()&&(ent.getValue()<5||FEATURE.get(ent.getKey()).freq<20))
					continue;
				//----a slave can't be the parent or ancestor of itself----//
				if(slave_key.equals(name))
					continue;
				boolean isancestor=false;
				Feature_tree temp=parent;
				while(temp!=null){
					if(slave_key.equals(temp.name)){
						isancestor=true;
						break;
					}
					temp=temp.parent;
				}
				if(isancestor)
					continue;
				//----a slave of M can't has a master N which is the slave of M, in other word, we only add direct child----//
				/*boolean notdirect=false;
				Iterator<Entry<String,Integer>> iter=FEATURE.get(ent.getKey()).master_feature_intsec.entrySet().iterator();
				while(iter.hasNext()){
					Entry<String,Integer> ent1=iter.next();
					if(FEATURE.get(ent1.getKey()).master_feature_intsec.containsKey(ent.getKey())){
						notdirect=true;
						break;
					}
				}
				if(notdirect)
					continue;*/
				Feature_tree son=new Feature_tree(FEATURE.get(slave_key));
				son.parent=this;
				son.level=level+1;
				son.get_children();
				slaves.add(son);
			}
		}
		public String display(){
			String s="";
			s=s+"[root: "+name+", children:";
			Iterator<Feature_tree> it_children=slaves.iterator();
			while(it_children.hasNext())
				s=s+" "+it_children.next().name+",";
			if(!slaves.isEmpty())
				s=s.substring(0, s.length()-1);
			s=s+"]\n";
			it_children=slaves.iterator();
			while(it_children.hasNext())
				s=s+it_children.next().display();
			return s;
		}
		public HashSet<Feature_tree> find_children(String child_name){
			HashSet<Feature_tree> children=new HashSet<Feature_tree>();
			Iterator<Feature_tree> it=slaves.iterator();
			while(it.hasNext()){
				Feature_tree child=it.next();
				if(child.name.equals(child_name))
					children.add(child);
				children.addAll(child.find_children(child_name));
			}
			return children;
		}
		public Feature_tree find_son(String child_name){
			Iterator<Feature_tree> it=slaves.iterator();
			while(it.hasNext()){
				Feature_tree child=it.next();
				if(child.name.equals(child_name))
					return child;
			}
			return null;
		}
		public boolean equals(Feature_tree t){
			if(!name.equals(t.name))
				return false;
			boolean b;
			Iterator<Feature_tree> it=slaves.iterator();
			if(slaves.size()!=t.slaves.size())
				return false;
			while(it.hasNext()){
				Feature_tree child=it.next();
				Feature_tree t_child=t.find_son(child.name);
				if(t_child==null)
					return false;
				if(!t_child.equals(child))
					return false;
			}
			return true;
		}
	}
	public static void make_tree(){
		FEATURE.get("phone").master_feature_comp.clear();
		FEATURE.get("phone").master_feature_of.clear();
		FEATURE.get("phone").master_feature_intsec.clear();
		FEATURE.get("phone").master_feature_union.clear();
		tree=new Feature_tree(FEATURE.get("phone"));
		tree.get_children();
		//----tree prun----//
	}
	public static void sort_feature(){
		Feature_sorted =
			    new ArrayList<Entry<String,Feature>>(FEATURE.entrySet());
		Collections.sort(Feature_sorted, new Comparator<Entry<String,Feature>>() { 
			public int compare(Entry<String,Feature> o1,Entry<String,Feature> o2) {      
				return (o2.getValue().freq - o1.getValue().freq); 
			}
		}); 
	}
	public static void get_sentence_vec(){
		sort_feature();
		sentence_vec=new ArrayList<Vector<Integer>>();
		Iterator<Sentence> it=All_sentences.iterator();
		Integer I=0;
		while(it.hasNext()){
			Sentence s=it.next();
			Vector<Integer> vec=new Vector<Integer>(FEATURE.size());
			for(int k=0;k<FEATURE.size();k++){
				if(s.text.indexOf(Feature_sorted.get(k).getKey())!=-1)
					I=1;
				else
					I=0;
				vec.add(I);
			}
			sentence_vec.add(vec);			
		}
	}
	public static void get_context_vec(){
		get_sentence_vec();
		context_vec=new ArrayList<Vector<Integer>>();
		for(int k=0;k<Feature_sorted.size();k++){
			Vector<Integer> vec=new Vector<Integer>(Feature_sorted.size());
			for(int m=0;m<Feature_sorted.size();m++) vec.addElement(0);
			Iterator<Vector<Integer>> it=sentence_vec.iterator();
			while(it.hasNext()){
				Vector<Integer> sent_vec=it.next();
				if(sent_vec.get(k)==1)
					vec=vector_add(vec,sent_vec);
			}
			vec.set(k,0);
			context_vec.add(vec);
		}
	}
	public static void write_input_matrix(){
		HashMap<String,Integer> hm=new HashMap<String,Integer>();
		Iterator<Entry<String,Feature>> iter=Feature_sorted.iterator();
		int k=0;
		while(iter.hasNext()){
			hm.put(iter.next().getKey(), k);
			k++;
		}
		try{
			Iterator<Feature_tree> it=tree.slaves.iterator();
			FileWriter fw=new FileWriter("Input_Matrix.txt");
			BufferedWriter bufw=new BufferedWriter(fw);
			bufw.write(tree.slaves.size()+" "+context_vec.get(0).size()+'\n');		
			while(it.hasNext()){
				String name=it.next().name;
				int id=hm.get(name);
				Vector<Integer> vec=context_vec.get(id);
				Iterator<Integer> it_vec=vec.iterator();
				while(it_vec.hasNext())
					bufw.write(it_vec.next().toString()+' ');
				bufw.write('\n');
			}
			bufw.close();
			fw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public static Vector<Integer> vector_add(Vector<Integer> a,Vector<Integer> b){
		if(a.size()!=b.size()){
			System.out.println("dimension mismatch for adder!");
			return null;
		}
		Vector<Integer> vec=new Vector<Integer>(a.size());
		for(int k=0;k<a.size();k++)
			vec.add(a.get(k)+b.get(k));
		return vec;
	}
	public static String clust_feature(){
		String method="direct";//"graph";"agglo";"rbr";"bagglo";"br";
		Integer clust_num=3;
		for(int k=0;k<clust_num;k++){
			HashSet<String> hs=new HashSet<String>();
			clusters.add(hs);
		}
		
		String command="vcluster -clmethod="+method+" Input_Matrix.txt "+clust_num.toString();
		CommandUtil util = new CommandUtil();
        util.executeCommand(command);
        printList(util.getStdoutList());
        System.out.println("--------------------");
        printList(util.getErroroutList());
        //HashMap<String,Integer> feature_clust=new HashMap<String,Integer>();
        String outfile="Input_Matrix.txt.clustering."+clust_num.toString();
        try{
        	FileReader fr=new FileReader(outfile);
        	BufferedReader bufr=new BufferedReader(fr);
        	String s=null;
        	int i=0;
        	int clust_id;
        	Iterator<Feature_tree> iter=tree.slaves.iterator();
        	while((clust_id=bufr.read())!=-1){
        		if(i%3==0){
        			clust_id=clust_id-(int) '0';
        			clusters.get(clust_id).add(iter.next().name);
        			//feature_clust.put(iter.next().name, clust_id);
        		}
        		i++;
        	}
        	bufr.close();
        	fr.close();
        }
        catch(Exception e){
        	e.printStackTrace();
        }
        String result="";
        for(Integer k=0;k<clust_num;k++){
        	result=result+"clust "+k.toString()+":";
        	Iterator<String> it=clusters.get(k).iterator();
        	while(it.hasNext())
        		result=result+" "+it.next();
        	result+='\n';
        }
        return result;
	}
	public static void printList(List<String> list){
        for (String string : list) {
            System.out.println(string);
        }
    }
	public static void main(String[] args){
		
		SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy年MM月dd日HH时mm分ss秒" );
        String create_time = sdf.format(new Date());
		//----for local machine----//
		int max_review_num=30;
		String folder="E:/Tsinghua/毕设/project1/remote_service/";

		//----for remote service----//
//		int max_review_num=30000;
//		String folder="./";
		
		String path=folder+"reviews.txt";
		String path_pos=folder+"dict/positive-words.txt";
		String path_neg=folder+"dict/negative-words.txt";		        
		String log_out=folder+"result/result_"+create_time+".txt";
		if(args.length>0)
			max_review_num=Integer.parseInt(args[0]);
		if(args.length>1)
			path=args[1];
		   
		
		/*HashMap<String,Integer> OPINION=*/read_opinion_lexicon(path_pos,path_neg);
		OPINION.putAll(LEXICON);
		long read_begin=System.currentTimeMillis();
		
		File pre_read_All_sentences=new File(folder+"All_sentences.dat");
		if(!pre_read_All_sentences.exists()){
			read_custome_review(path,max_review_num);
			try{
				FileOutputStream fs=new FileOutputStream(pre_read_All_sentences);
				ObjectOutputStream os=new ObjectOutputStream(fs);
				Iterator<Sentence> it_sent=All_sentences.iterator();
				while(it_sent.hasNext()){
					os.writeObject(it_sent.next());
					os.flush();
				}
				os.close();
				fs.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		else{
			try{
				FileInputStream fi=new FileInputStream(pre_read_All_sentences);
				ObjectInputStream oi=new ObjectInputStream(fi);
				try{
					while(true)
						All_sentences.add((Sentence) oi.readObject());
				}
				catch(EOFException e){
					oi.close();
					fi.close();
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		long read_end=System.currentTimeMillis();
		System.out.println("----read "+All_sentences.size()+" sentences time use: "+((double)(read_end-read_begin))/1000+" s----");

		long extract_begin=System.currentTimeMillis();
		//---- double propagation with all 8 rules----//
//		Iterator<Sentence> it_sent;
//		int new_feature=0,new_opinion=0;
//		boolean sth_new=false;
//		do{
//			sth_new=false;
//			
//			new_feature=0;new_opinion=0;
//			it_sent= All_sentences.iterator();
//			while(it_sent.hasNext()){
//				List<Integer> new_word_num=r1_and_r4(it_sent.next());
//				new_feature+=new_word_num.get(0);
//				new_opinion+=new_word_num.get(1);
//			}
//			System.out.println("r1 and r4 new feature and opinion: "+new_feature+" "+new_opinion);
//			if(new_feature>0||new_opinion>0)
//				sth_new=true;
//			
//			new_feature=0;new_opinion=0;
//			it_sent=All_sentences.iterator();
//			while(it_sent.hasNext()){
//				List<Integer> new_word_num=r3_and_r2(it_sent.next());
//				new_feature+=new_word_num.get(0);
//				new_opinion+=new_word_num.get(1);
//			}
//			System.out.println("r3 and r2 new feature and opinion: "+new_feature+" "+new_opinion);
//			if(new_feature>0||new_opinion>0)
//				sth_new=true;
//		}while(sth_new);

		//---- no propagation with r11 O->O_DEP->T,t=T----//
		Iterator<Sentence> it_sent=All_sentences.iterator();
		while(it_sent.hasNext())
			r11(it_sent.next());
		
		//---- no propagation with r11&r12----//
//		Iterator<Sentence> it_sent=All_sentences.iterator();
//		while(it_sent.hasNext())
//			r11_and_r12(it_sent.next());
		
		sub_feature_abstract();
		
		make_tree();
		
		get_context_vec();
		
		long extract_end=System.currentTimeMillis();
		System.out.println("----extract opinions and targets time use: "+((double)(extract_end-extract_begin))/1000+" s----");
		write_input_matrix();
		Iterator<HashMap.Entry<String,Opinion>> it_op=OPINION.entrySet().iterator();
		while(it_op.hasNext()){
			Entry<String,Opinion> ent=it_op.next();
			if(!LEXICON.containsKey(ent.getKey()))
				OPINION_NEW.put(ent.getKey(),ent.getValue());
		}
		/*it_sent=All_sentences.iterator();
		while(it_sent.hasNext()){
			Iterator<LabeledWord> it_words=it_sent.next().parse.labeledYield().iterator();
			while(it_words.hasNext()){
				String wd=it_words.next().value();
				Integer freq=FEATURE.get(wd);
				if(freq==null)
					continue;
				FEATURE.put(wd, freq+1);
			}
		}*/
		

		
		/*Iterator<Entry<String,Integer>> it=FEATURE.entrySet().iterator();
		while(it.hasNext())
			System.out.println(it.next());*/
		//---- Feature sort by frequency ----//
		try{
			FileWriter log_write=new FileWriter(log_out);
			BufferedWriter bufw=new BufferedWriter(log_write);
			bufw.write("review path:"+path+"\n");
			List<HashMap.Entry<String, Feature>> features =
				    new ArrayList<HashMap.Entry<String, Feature>>(FEATURE.entrySet());
			Collections.sort(features, new Comparator<Map.Entry<String, Feature>>() { 
				public int compare(Map.Entry<String, Feature> o1, Map.Entry<String, Feature> o2) {      
					return (o2.getValue().freq - o1.getValue().freq); 
					//return (o1.getKey()).toString().compareTo(o2.getKey());
				}
			}); 
			Iterator<HashMap.Entry<String, Feature>> iter=features.iterator();
			while(iter.hasNext())
				bufw.write(iter.next().getValue().display()+"\n");
			
			bufw.write("the tree:\n"+tree.display()+"\n");
			bufw.write("cluster for the second floor: "+clust_feature());
			//----prun1----//
//			String s_prun1=prun1();
//			bufw.write(s_prun1);
//			List<HashMap.Entry<String, Feature>> features_prun1 =
//				    new ArrayList<HashMap.Entry<String, Feature>>(FEATURE.entrySet());
//			Collections.sort(features_prun1, new Comparator<Map.Entry<String, Feature>>() { 
//				public int compare(Map.Entry<String, Feature> o1, Map.Entry<String, Feature> o2) {      
//					return (o2.getValue().freq - o1.getValue().freq); 
//					//return (o1.getKey()).toString().compareTo(o2.getKey());
//				}
//			}); 
//			iter=features_prun1.iterator();
//			while(iter.hasNext())
//				bufw.write(iter.next().getValue().display()+"\n");
			
			//----new opinion words----//
			System.out.println("new opinion words:\n");
			bufw.write("new opinion words:\n");
			List<HashMap.Entry<String, Opinion>> opinions =
				    new ArrayList<HashMap.Entry<String, Opinion>>(OPINION_NEW.entrySet());
			Collections.sort(opinions, new Comparator<Map.Entry<String, Opinion>>() { 
				public int compare(Map.Entry<String, Opinion> o1, Map.Entry<String, Opinion> o2) {      
					return (o2.getValue().freq - o1.getValue().freq); 
					//return (o1.getKey()).toString().compareTo(o2.getKey());
				}
			}); 
			it_op=opinions.iterator();
			while(it_op.hasNext())
				bufw.write(it_op.next().getValue().display()+"\n");
			bufw.close();
			log_write.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
