/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.stats;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lib.Tuple;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.sparql.util.Utils;

import com.hp.hpl.jena.tdb.index.TripleIndex;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.pgraph.NodeId;

public class StatsWriter
{
    public static Item gather(GraphTDB graph)
    {
        long count = 0 ;
        Map<NodeId, Integer> predicateIds = new HashMap<NodeId, Integer>(10000) ;
        Map<Node, Integer> predicates = new HashMap<Node, Integer>(10000) ;
        
        Item stats = Item.createList() ;
        ItemList statsList = stats.getList() ;
        statsList.add("stats") ;
        
        
        TripleIndex index = graph.getIndexSPO() ;
        Iterator<Tuple<NodeId>> iter = index.all() ;
        for ( ; iter.hasNext() ; )
        {
            Tuple<NodeId> tuple = iter.next(); 
            count++ ;
            NodeId nodeId = tuple.get(1) ;      // Predciate slot
            Integer n = predicateIds.get(nodeId) ;
            if ( n == null )
                predicateIds.put(nodeId,1) ;
            else
                predicateIds.put(nodeId, n+1) ;
        }
        
//        System.out.printf("Triples  %d\n", count) ;
//        System.out.println("NodeIds") ;
//        for ( NodeId p : predicateIds.keySet() )
//            System.out.printf("%s : %d\n",p, predicateIds.get(p) ) ;

//        System.out.println("Nodes") ;
        
        addPair(statsList, "timestamp", NodeFactory.nowAsDateTime()) ;
        addPair(statsList, "run@",  Utils.nowAsString()) ;
        addPair(statsList, "count", integer(count)) ;
        for ( NodeId p : predicateIds.keySet() )
        {
            Node n = graph.getNodeTable().retrieveNodeByNodeId(p) ; 
            predicates.put(n, predicateIds.get(p)) ;
            
            addPair(statsList, n, integer(predicateIds.get(p))) ;
//            System.out.printf("%s : %d\n",n, predicateIds.get(p) ) ;
        }
        return stats ;
    }
    
    
    // To Item.
    private static Node integer(Integer n)
    {
        return integer(Integer.toString(n)) ;
    }

    private static Node integer(long n)
    {
        return integer(Long.toString(n)) ;
    }

    
    private static Node integer(String lex)
    {
        return Node.createLiteral(lex, null, XSDDatatype.XSDinteger) ;
    }

    
    private static void addPair(ItemList list, String key, String value)
    {
        addPair(list, Item.createSymbol(key), Item.createNode(Node.createLiteral(value))) ;
    }
    
    private static void addPair(ItemList list, String key, Node node)
    {
        addPair(list, Item.createSymbol(key), Item.createNode(node)) ;
    }
    
    private static void addPair(ItemList list, Node key, Node value)
    {
        addPair(list, Item.createNode(key), Item.createNode(value)) ;
    }
    
    private static void addPair(ItemList list, Item key, Item value)
    {
        Item pair = make(key, value) ;
        list.add(pair) ;
    }
        
    private static Item make(Item... items)
    {
        Item list = Item.createList() ;
        for ( Item item : items)
            list.getList().add(item) ;
        return list ; 
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */