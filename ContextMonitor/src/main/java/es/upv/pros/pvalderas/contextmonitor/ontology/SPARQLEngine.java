package es.upv.pros.pvalderas.contextmonitor.ontology;

import com.clarkparsia.pellet.sparqldl.jena.SparqlDLExecutionFactory;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;


public class SPARQLEngine {
	
	private Model model;
	
	public SPARQLEngine(Model model){
		this.model=model;
	}

	/**
	 * @param queryString: is a SELECT query in SPARQL, e.g.
	 * SELECT ?x" WHERE { ?x  <http://www.pros.com/name> ?value FILTER (REGEX(?value, "Juan"))
	 * @return
	 */
	public ResultSet executeQuerySELECT(String queryString)
	{
		Query query = QueryFactory.create(queryString, Syntax.syntaxARQ ) ;		
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ; 

		ResultSet rs = qexec.execSelect() ; 		
		return rs;
	}
	
	/**
	 * @param queryString: is an ASK query in SPARQL, e.g.
	 * PREFIX pros: <http://www.pros.com/> ASK {?x pros:name "Juan"; pros:surname "Rodrigo"
	 *  FILTER (REGEX(?value, "Juan") and (?surname)}
	 * 
	 * @return
	 */
	public boolean executeQueryASK(String queryString){
		return true;
		
		/*Query query = QueryFactory.create(queryString, Syntax.syntaxARQ ) ;		
		QueryExecution qexec = SparqlDLExecutionFactory.create( query, model );     

		boolean rs = qexec.execAsk(); 		
	
		return rs;*/
	
	}
	
	public void execQuery(String sparqlQueryString, Dataset dataset)
    {
        Query query = QueryFactory.create(sparqlQueryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
        try {
            ResultSet results = qexec.execSelect() ;
            for ( ; results.hasNext() ; )
            {
                QuerySolution soln = results.nextSolution() ;
                int count = soln.getLiteral("count").getInt() ;
            }
          } finally { qexec.close() ; }
    }
	

	public ResultSet queryPellet(String query){	
		Query q = QueryFactory.create( query );
		QueryExecution qe = SparqlDLExecutionFactory.create( q, model );
		ResultSet rs = qe.execSelect();
		
		ResultSetFormatter.out(rs); 
		
		return rs;

	}
}
