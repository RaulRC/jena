/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder;

import java.util.HashMap;
import java.util.Map;
import org.apache.jena.arq.querybuilder.clauses.PrologClause;
import org.apache.jena.arq.querybuilder.handlers.HandlerBlock;
import org.apache.jena.arq.querybuilder.handlers.PrologHandler;
import org.apache.jena.graph.FrontsNode ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.impl.LiteralLabelFactory ;
import org.apache.jena.query.Query ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.ExprVar ;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.util.NodeFactoryExtra ;

/**
 * Base class for all QueryBuilders.
 * 
 * @param <T>
 *            The derived class type. Used for return types.
 */
public abstract class AbstractQueryBuilder<T extends AbstractQueryBuilder<T>>
		implements Cloneable, PrologClause<T> {

	// the query this builder is building
	protected Query query;
	// a map of vars to nodes for replacement during build.
	private Map<Var, Node> values;

	/**
	 * Make a Node from an object.
	 * <ul>
	 * <li>Will return Node.ANY if object is null.</li>
	 * <li>Will return the enclosed Node from a FrontsNode</li>
	 * <li>Will return the object if it is a Node.</li>
	 * <li>Will call NodeFactoryExtra.parseNode() using the currently defined
	 * prefixes if the object is a String</li>
	 * <li>Will create a literal representation if the parseNode() fails or for
	 * any other object type.</li>
	 * </ul>
	 * 
	 * Uses the internal query prefix mapping to resolve prefixes.
	 * 
	 * @param o
	 *            The object to convert. (may be null)
	 * @return The Node value.
	 */
	public Node makeNode(Object o) {
		return makeNode( o, query.getPrefixMapping() );
	}
	
	/**
	 * Make a node from an object while using the associated prefix mapping.
	 * <ul>
	 * <li>Will return Node.ANY if object is null.</li>
	 * <li>Will return the enclosed Node from a FrontsNode</li>
	 * <li>Will return the object if it is a Node.</li>
	 * <li>Will call NodeFactoryExtra.parseNode() using the currently defined
	 * prefixes if the object is a String</li>
	 * <li>Will create a literal representation if the parseNode() fails or for
	 * any other object type.</li>
	 * </ul>
	 * @param o The object to convert (may be null).
	 * @param pMapping The prefix mapping to use for prefix resolution.
	 * @return The Node value.
	 */
	public static Node makeNode(Object o, PrefixMapping pMapping) {
		if (o == null) {
			return Node.ANY;
		}
		if (o instanceof FrontsNode) {
			return ((FrontsNode) o).asNode();
		}

		if (o instanceof Node) {
			return (Node) o;
		}
		if (o instanceof String) {
			try {
				return NodeFactoryExtra.parseNode((String) o, PrefixMapFactory
						.createForInput(pMapping));
			} catch (RiotException e) {
				// expected in some cases -- do nothing
			}

		}
		return NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(o));
	}

	/**
	 * Make a Var from an object.
	 * <ul>
	 * <li>Will return Var.ANON if object is null.</li>
	 * <li>Will return null if the object is "*" or Node_RuleVariable.WILD</li>
	 * <li>Will return the object if it is a Var</li>
	 * <li>Will return resolve FrontsNode to Node and then resolve to Var</li>
	 * <li>Will return resolve Node if the Node implements Node_Variable,
	 * otherwise throws an NotAVariableException (instance of
	 * ARQInternalErrorException)</li>
	 * <li>Will return ?x if object is "?x"</li>
	 * <li>Will return ?x if object is "x"</li>
	 * <li>Will return the enclosed Var of a ExprVar</li>
	 * <li>For all other objects will return the "?" prefixed to the toString()
	 * value.</li>
	 * </ul>
	 * 
	 * @param o
	 *            The object to convert.
	 * @return the Var value.
	 * @throws ARQInternalErrorException
	 */
	public Var makeVar(Object o) throws ARQInternalErrorException {
		if (o == null) {
			return Var.ANON;
		}
		if (o instanceof Var) {
			return (Var) o;
		}
		Var retval = null;
		if (o instanceof FrontsNode) {
			retval = Var.alloc(((FrontsNode) o).asNode());
		} else if (o instanceof Node) {
			retval = Var.alloc((Node) o);
		} else if (o instanceof ExprVar) {
			retval = Var.alloc((ExprVar) o);
		} else {
			retval = Var.alloc(Var.canonical(o.toString()));
		}
		if ("*".equals(Var.canonical(retval.toString()))) {
			return null;
		}
		return retval;
	}

	/**
	 * Create a new query builder.
	 */
	protected AbstractQueryBuilder() {
		query = new Query();
		values = new HashMap<Var, Node>();
	}
	
	/**
	 * Get the HandlerBlock for this query builder.
	 * @return The associated handler block.
	 */
	public abstract HandlerBlock getHandlerBlock();
	
	@Override
	public final PrologHandler getPrologHandler() {
		return getHandlerBlock().getPrologHandler();
	}


	/**
	 * Set a variable replacement. During build all instances of var in the
	 * query will be replaced with value. If value is null the replacement is
	 * cleared.
	 * 
	 * @param var
	 *            The variable to replace
	 * @param value
	 *            The value to replace it with or null to remove the
	 *            replacement.
	 */
	public void setVar(Var var, Node value) {
		if (value == null) {
			values.remove(var);
		} else {
			values.put(var, value);
		}
	}

	/**
	 * Set a variable replacement. During build all instances of var in the
	 * query will be replaced with value. If value is null the replacement is
	 * cleared.
	 * 
	 * See {@link #makeVar} for conversion of the var param. See
	 * {@link #makeNode} for conversion of the value param.
	 * 
	 * @param var
	 *            The variable to replace.
	 * @param value
	 *            The value to replace it with or null to remove the
	 *            replacement.
	 */
	public void setVar(Object var, Object value) {
		if (value == null) {
			setVar(makeVar(var), null);
		} else {
			setVar(makeVar(var), makeNode(value));
		}
	}

	@Override
	public T addPrefix(String pfx, Resource uri) {
		return addPrefix(pfx, uri.getURI());
	}

	@Override
	public T addPrefix(String pfx, Node uri) {
		return addPrefix(pfx, uri.getURI());
	}

	@SuppressWarnings("unchecked")
	@Override
	public T addPrefix(String pfx, String uri) {
		getPrologHandler().addPrefix(pfx, uri);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T addPrefixes(Map<String, String> prefixes) {
		getPrologHandler().addPrefixes(prefixes);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T setBase(String base) {
		getPrologHandler().setBase(base);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T setBase(Object base) {
		setBase(makeNode(base).getURI());
		return (T) this;
	}

	@Override
	public String toString() {
		return buildString();
	}

	/**
	 * Build the query as a string.
	 * 
	 * @return the string representation of the query.
	 */
	public final String buildString() {
		return build().toString();
	}

	/**
	 * Build the query. Performs the var replacements as specified by
	 * setVar(var,node) calls.
	 * 
	 * @return The query.
	 */
	public final Query build() {
		Query q = new Query();
		
		// set the query type
		switch (query.getQueryType())
		{
		case Query.QueryTypeAsk:
			q.setQueryAskType();
			break;
		case Query.QueryTypeConstruct:
			q.setQueryConstructType();
			break;
		case Query.QueryTypeDescribe:
			q.setQueryDescribeType();
			break;
		case Query.QueryTypeSelect:
			q.setQuerySelectType();
			break;
		default:
			throw new IllegalStateException( "Internal query is not a known type: "+q.getQueryType());			
		}
		
		// use the HandlerBlock implementation to copy the data.
		HandlerBlock handlerBlock = new HandlerBlock(q);
		handlerBlock.addAll( getHandlerBlock() );
		
		// set the vars
		handlerBlock.setVars(values);
		
		//  make sure we have a query pattern before we start building.
		if (q.getQueryPattern() == null)
		{
			q.setQueryPattern( new ElementGroup() );
		}
		
		handlerBlock.build();

		return q;
	}

	/**
	 * Close the query.
	 * 
	 * This can be used when the query would not normally parse as is required
	 * by the Query.clone() method.
	 * 
	 * @param q2
	 *            The query to clone
	 * @return A clone of the q2 param.
	 */
	public static Query clone(Query q2) {
		Query retval = new Query();
		
		// set the query type
	    if (q2.isSelectType())
	    {
	    	retval.setQuerySelectType();
	    } else if (q2.isAskType()) {
	    	retval.setQueryAskType();
	    } else if (q2.isDescribeType())
	    {
	    	retval.setQueryDescribeType();
	    } else if (q2.isConstructType()) 
	    {
	    	retval.setQueryConstructType();
	    }
	    
	    // use the handler block to clone the data
	    HandlerBlock hb = new HandlerBlock( retval );
	    HandlerBlock hb2 = new HandlerBlock( q2 );
	    hb.addAll(hb2);
		
		return retval;
	}

	/**
	 * Rewrite a query replacing variables as specified in the values map.
	 * 
	 * @param q2
	 *            The query to rewrite
	 * @param values
	 *            a Mapping of var to node for replacement.
	 * @return The new query with the specified vars replaced.
	 */
	public static Query rewrite(Query q2, Map<Var, Node> values) {
		HandlerBlock hb = new HandlerBlock(q2);
		hb.setVars(values);
		return q2;
	}
}
