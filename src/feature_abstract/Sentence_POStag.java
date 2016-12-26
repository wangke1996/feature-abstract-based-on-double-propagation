package feature_abstract;


import java.util.List;
import java.io.StringReader;

import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class Sentence_POStag {
	public Tree POS_tag(String sentence){
		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
		
	    TokenizerFactory<CoreLabel> tokenizerFactory =
	            PTBTokenizer.factory(new CoreLabelTokenFactory(), "");	    
	    Tokenizer<CoreLabel> tok =
	            tokenizerFactory.getTokenizer(new StringReader(sentence));
	    List<CoreLabel> rawWords = tok.tokenize();
	    Tree Parse = lp.apply(rawWords);
	    //Parse.pennPrint();
	   /* TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
	    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	    GrammaticalStructure gs = gsf.newGrammaticalStructure(Parse);
	    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
	    TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
	    tp.printTree(Parse);*/
		return Parse;
	}
}
