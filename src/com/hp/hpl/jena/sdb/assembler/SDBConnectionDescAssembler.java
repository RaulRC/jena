/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.assembler;



import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.query.util.GraphUtils;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.Access;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SDBConnectionDescAssembler extends AssemblerBase implements Assembler
{

    @Override
    public Object open(Assembler a, Resource root, Mode mode)
    {
        SDBConnectionDesc sDesc = new SDBConnectionDesc() ;
        
        sDesc.type      = GraphUtils.getStringValue(root, AssemblerVocab.pSDBtype) ;
        sDesc.host      = GraphUtils.getStringValue(root, AssemblerVocab.pSDBhost) ;
        sDesc.argStr    = GraphUtils.getStringValue(root, AssemblerVocab.pSDBargStr) ;
        sDesc.name      = GraphUtils.getStringValue(root, AssemblerVocab.pSDBname) ;
        sDesc.user      = GraphUtils.getStringValue(root, AssemblerVocab.pSDBuser) ;
        sDesc.password  = GraphUtils.getStringValue(root, AssemblerVocab.pSDBpassword) ;
        sDesc.driver    = GraphUtils.getStringValue(root, AssemblerVocab.pDriver) ;
        sDesc.jdbcURL   = GraphUtils.getStringValue(root, AssemblerVocab.pJDBC) ;
        sDesc.rdbType   = GraphUtils.getStringValue(root, AssemblerVocab.pRDBtype) ;
        sDesc.label     = GraphUtils.getStringValue(root, RDFS.label) ;
        
        if ( sDesc.jdbcURL == null && sDesc.user == null )
            sDesc.user = Access.getUser() ;
        if ( sDesc.jdbcURL == null && sDesc.password == null )
            sDesc.password = Access.getPassword() ;
        return sDesc ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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