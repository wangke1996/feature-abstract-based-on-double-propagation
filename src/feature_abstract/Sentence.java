package feature_abstract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.io.Serializable;
import java.io.StringReader;

import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
public class Sentence implements Serializable{
	protected String text;
	//protected HashSet<String> nouns=new HashSet<String>();
	//protected String[] tags;//parse.taggedYield() or parse.taggedLabeledYield()   .get(index).tag().toString()
	//protected String[] words;//parse.yield()
	protected List<CoreLabel> rawWords;
	protected Tree parse;
	protected List<TypedDependency> dependence;
	protected List<HashSet<TypedDependency>> gov_of_which;//words[i] is the gov of those dependency in gov_of_which[i]
	public void set_text(String s,LexicalizedParser lp,TokenizerFactory<CoreLabel> tokenizerFactory){
		text=s;
		//nouns.clear();
		POS_tag(text,lp,tokenizerFactory);
	};
	public String get_text(){
		return text;
	}
	public Sentence(String s,LexicalizedParser lp,TokenizerFactory<CoreLabel> tokenizerFactory){
		text=s;
		//nouns.clear();
		POS_tag(s,lp,tokenizerFactory);
		word_dependency_record();
		//words=s.split(" |,|\"|:");
	}
	public void word_dependency_record(){
		gov_of_which=new ArrayList<HashSet<TypedDependency>>(parse.yield().size());
		for(int i=0;i<parse.yield().size();i++){
			gov_of_which.add(new HashSet<TypedDependency>());
		}
		Iterator<TypedDependency> it=dependence.iterator();		
		int word_id;
		while(it.hasNext()){
			TypedDependency depend=it.next();
			word_id=depend.gov().index()-1;
			if(word_id<0)
				continue;
			HashSet<TypedDependency> TD=gov_of_which.get(word_id);
			TD.add(depend);
			gov_of_which.set(word_id, TD);
		}
	}
	public void POS_tag(String sentence,LexicalizedParser lp,TokenizerFactory<CoreLabel> tokenizerFactory){
//		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
//		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
//		
//	    TokenizerFactory<CoreLabel> tokenizerFactory =
//	            PTBTokenizer.factory(new CoreLabelTokenFactory(), "");	    
	    Tokenizer<CoreLabel> tok =
	            tokenizerFactory.getTokenizer(new StringReader(sentence));
	    rawWords = tok.tokenize();
	    parse = lp.apply(rawWords);
	    
	    //parse.pennPrint();
	    TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
	    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	    dependence = gs.typedDependenciesCCprocessed();
//	    TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
//	    tp.printTree(parse);
	}
}
