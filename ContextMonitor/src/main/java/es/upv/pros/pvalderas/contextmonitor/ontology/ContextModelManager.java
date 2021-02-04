package es.upv.pros.pvalderas.contextmonitor.ontology;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.mindswap.pellet.jena.PelletInfGraph;
import org.mindswap.pellet.jena.PelletReasoner;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;


@Component
public class ContextModelManager{
	
	public final String  ontologyName= ContextOntology.getOntologyName();
	public final String ontologyPath= ContextOntology.getOntologyPath(); 
	
	public final String prefixURI="http://www.pros.com/";	
	
	public final String header = "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
	"PREFIX pros:   <http://www.pros.com/>\n" +
	"PREFIX owl:  <http://www.w3.org/2002/07/owl#>\n" +
	"PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>\n";	
	
	public PelletReasoner jenaReasoner;
	public PelletInfGraph graph;
	public Model model;
	public OntModel ontModel;
	public InfModel infModel;
	public Dataset dataset;
	public String absolutePath;
		
	public void loadOntology(){
		String directory = ontologyPath+"tdb";
		
		File dir= new File (directory);
		emptyDir(dir);
		 
		// open TDB dataset
		dataset = TDBFactory.createDataset(directory);
		 
		// assume we want the default model, or we could get a named model here
		 model = dataset.getDefaultModel();
		
		// read the input file - only needs to be done once
		model = FileManager.get().readModel( model, ontologyPath + ontologyName , "RDF/XML" );//"RDF/XML-ABBREV" );
		ontModel= ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, model);  //OWL_DL_MEM_RDFS_INF
		
		PelletReasoner reasoner = new PelletReasoner();
		infModel = ModelFactory.createInfModel(reasoner, model);
		System.out.println("Context Ontology Loaded");
			 
	}  
	
	public boolean checkCondition(String condition){
		return new SPARQLEngine(model).executeQueryASK(condition);	
	}
	
	
	/**This method prints all the information stored in the model as tuples 
	 * 
	 */
	public void printAllTuples(){
		 ResultSet results =new SPARQLEngine(model).executeQuerySELECT("SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
		 ResultSetFormatter.out(results) ;
	}

	

	
	private boolean emptyDir(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i = 0; i < children.length; i++) {
	            boolean success = emptyDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }

	    return dir.delete(); // The directory is empty now and can be deleted.
	}
	
	
	
	public String getOntologyPath(){
		return this.ontologyPath+this.ontologyName;
	}
	
	 	
	/** This method is called for starting a transaction
	 * @param transactionType must be WRITE or READ
	 */
	public void transaction_begin(String transactionType){
		if (transactionType.equals("READ"))
			dataset.begin(ReadWrite.READ) ;
		else dataset.begin(ReadWrite.WRITE) ;  
	}
	
	/** This method must be called for finishing a transaction and committing the changes
	 * 
	 */
	public void transaction_commit(){
		dataset.commit() ;
	}
	

	public void saveOntologyInFile(String path) throws FileNotFoundException{
			dataset.close();
			model.write(new FileOutputStream(path), "RDF/XML");
	}
	
	public ArrayList<String> getObjectPropertyIDs(String individualID, String attributeID) {
		
		ArrayList<String> values = new ArrayList<String>();
		
		 try {	
			dataset.begin( ReadWrite.READ);  
		    Individual ind= ontModel.getIndividual(prefixURI + individualID);
			Property prop= ontModel.getProperty(prefixURI + attributeID);
			try{
				String uri;
				 Iterator<RDFNode> iter = ind.listPropertyValues(prop);
		         while (iter.hasNext()) {
		            RDFNode node = iter.next();
		            uri= node.asResource().getURI();
		            values.add(uri);
		         }
		} catch(Exception e){
			System.out.println("The context object property identifiers are not correct, ind: " + prefixURI + individualID + " att: " + prop);
		}  
		dataset.close();   
		} finally {
	        dataset.end();
	    }
		
		return values;
		   
	}

	public String getObjectPropertyID(String individualID, String attributeID) {
		
		String attributeValue="";
		try {	
			 
			dataset.begin( ReadWrite.READ);	 
		    Individual ind= ontModel.getIndividual(prefixURI + individualID);
			Property prop= ontModel.getProperty(prefixURI + attributeID);
	
			try{
				attributeValue= ind.getProperty(prop).asTriple().getObject().toString();
				//Quitamos el prefijo de la ontologia
				attributeValue=attributeValue.split("#")[1];
			} catch(Exception e){
				System.out.println("The context object property identifiers are not correct, ind: " + prefixURI + individualID + " att: " + prop + " att value " + attributeValue );
			}
	    
			dataset.close();   
	    
		} finally {
		    dataset.end();
		}
		
		return attributeValue;   
	}
	
	
	public String getAttributeValue(String individualID, String attributeID) {
		
		String attributeValue="";
		 try {	
			 
			dataset.begin( ReadWrite.READ);
			Individual ind= ontModel.getIndividual(prefixURI + individualID);
			Property prop= ontModel.getProperty(prefixURI + attributeID);

			try{
				attributeValue= ind.getPropertyValue(prop).asLiteral().getLexicalForm();
			} catch(Exception e){
				System.out.println("The context property identifiers are not correct, ind: " + individualID + " att: " + attributeID);
			}
		
			dataset.close();   
        
		} finally {
	        dataset.end();
	    }
		
		return attributeValue;  
	}

	public int getAttributeValueInt(String individualID, String attributeID) {
		
		int attributeValue=0;
		try {	
			 dataset.begin( ReadWrite.READ);
			 Individual ind= ontModel.getIndividual(prefixURI + individualID);
			 Property prop= ontModel.getProperty(prefixURI + attributeID);
			 try{
				 attributeValue= ind.getPropertyValue(prop).asLiteral().getInt();
			 } catch(Exception e){
				System.out.println("The context property identifiers are not correct, ind: " + individualID + "att: " + attributeID);
			 }
			 dataset.close();   
	    } finally {dataset.end();}
		
		return attributeValue;  
	}
	
	public boolean getAttributeValueBool(String individualID, String attributeID) {
		
		boolean attributeValue=false;
		 try {	
			 dataset.begin( ReadWrite.READ);
			 Individual ind= ontModel.getIndividual(prefixURI + individualID);
			 Property prop= ontModel.getProperty(prefixURI + attributeID);
			try{
				attributeValue= ind.getPropertyValue(prop).asLiteral().getBoolean();
			} catch(Exception e){
				System.out.println("The context property identifiers are not correct, ind: " + individualID + "att: " + attributeID);		
			}
			dataset.close();   
	    } finally {dataset.end();}
		
		return attributeValue;  
	}
	
	public Iterator<? extends OntResource> retrieveInstances(String classID){
	
		OntClass c=ontModel.getOntClass(prefixURI + classID);
	
		if (c==null) return null;
		Iterator<? extends OntResource> it= c.listInstances();
	
		return it;	
	}
	
	
	public void setContextObjProp(String individualID, String attributeID, String newValue) {
		try {	
			dataset.begin( ReadWrite.WRITE); 
			Individual ind= ontModel.getIndividual(prefixURI + individualID);
			Property prop= ontModel.getProperty(prefixURI + attributeID);
			try{
				Literal l= ontModel.createTypedLiteral(newValue);
				ind.setPropertyValue(prop, l);
			} catch(Exception e){
				System.out.println("The context property identifiers are not correct, ind: " + individualID + "att: " + attributeID);	
			}
			dataset.commit();
		    dataset.close();   
		} finally {dataset.end(); }    
	}
		
	public void addContextObjProp(String individualID, String attributeID, String newValue) {
		 try {		 
			 dataset.begin( ReadWrite.WRITE);
			 Individual ind= ontModel.getIndividual(prefixURI + individualID);
			 Property prop= ontModel.getProperty(prefixURI + attributeID);
			 Individual indToAdd= ontModel.getIndividual(prefixURI + newValue);
			 ind.addProperty(prop, indToAdd);
		} catch(Exception e){
			System.out.println("The object property identifiers are not correct, ind: " + individualID + "att: " + attributeID);	
		}
		dataset.commit(); 
	}
	
	public void setContextProp(String individualID, String attributeID, String newValue) {
		try {	
			dataset.begin( ReadWrite.WRITE);
			Individual ind= ontModel.getIndividual(prefixURI + individualID);
			Property prop= ontModel.getProperty(prefixURI + attributeID);
	
			try{
				Literal l= ontModel.createTypedLiteral(newValue);
				ind.setPropertyValue(prop, l);			
			} catch(Exception e){
				System.out.println("The context property identifiers are not correct, ind: " + individualID + "att: " + attributeID);	
			}
			dataset.commit();
		    dataset.close();   
	   
		} finally {dataset.end(); }	
	}
	
	public void setContextPropInt(String individualID, String attributeID, int newValue) {
		try {	
			dataset.begin( ReadWrite.WRITE);
			Individual ind= ontModel.getIndividual(prefixURI + individualID);
			Property prop= ontModel.getProperty(prefixURI + attributeID);
			
			try{
				Literal l= ontModel.createTypedLiteral(newValue);
				ind.setPropertyValue(prop, l);
		
			} catch(Exception e){
				System.out.println("The context property identifiers are not correct, ind: " + individualID + "att: " + attributeID);	
			}
			dataset.commit();
		    dataset.close();   
	   
		} finally {dataset.end(); }
	}
	
	public void setContextPropBool(String individualID, String attributeID, boolean newValue) {
		try {	
			dataset.begin( ReadWrite.WRITE);
			Individual ind= ontModel.getIndividual(prefixURI + individualID);
			Property prop= ontModel.getProperty(prefixURI + attributeID);
			
			try{
				Literal l= ontModel.createTypedLiteral(newValue);
				ind.setPropertyValue(prop, l);
			} catch(Exception e){
				System.out.println("The context property identifiers are not correct, ind: " + individualID + "att: " + attributeID);	
			}
			dataset.commit();
		    dataset.close();    
		} finally {
			dataset.end(); 
		}
	}
	
	public void addInstance(String classID, String individualID){
		try {
			dataset.begin( ReadWrite.WRITE);
			Resource cls=ontModel.getResource(prefixURI + classID);
			ontModel.createIndividual(prefixURI + individualID, cls);
			dataset.commit();
		    dataset.close();    
		} finally {
			dataset.end(); 
		}
	}
		
	

}
