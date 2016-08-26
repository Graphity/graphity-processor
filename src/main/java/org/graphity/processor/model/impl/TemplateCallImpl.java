/*
 * Copyright 2015 Martynas Jusevičius <martynas@graphity.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.graphity.processor.model.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.ontology.ConversionException;
import org.apache.jena.ontology.impl.OntResourceImpl;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;
import org.graphity.processor.exception.SitemapException;
import org.graphity.processor.model.Argument;
import org.graphity.processor.model.Template;
import org.graphity.processor.model.TemplateCall;
import org.graphity.processor.query.QueryBuilder;
import org.graphity.processor.update.ModifyBuilder;
import org.graphity.processor.vocabulary.GP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.util.SPTextUtil;
import org.topbraid.spin.vocabulary.SP;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class TemplateCallImpl extends OntResourceImpl implements TemplateCall
{
    
    private static final Logger log = LoggerFactory.getLogger(TemplateCallImpl.class);

    public static Implementation factory = new Implementation() 
    {
        
        @Override
        public EnhNode wrap(Node node, EnhGraph enhGraph)
        {
            if (canWrap(node, enhGraph))
            {
                return new TemplateCallImpl(node, enhGraph);
            }
            else {
                throw new ConversionException( "Cannot convert node " + node.toString() + " to OntClass: it does not have rdf:type owl:Class or equivalent");
            }
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg)
        {
            if (eg == null) throw new IllegalArgumentException("EnhGraph cannot be null");

            /*
            // node will support being an OntClass facet if it has rdf:type owl:Class or equivalent
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, TemplateCall.class );
            */
            
            return eg.asGraph().contains(node, RDF.type.asNode(), GP.TemplateCall.asNode());            
        }
    };

    public TemplateCallImpl(Node node, EnhGraph graph)
    {
        super(node, graph);
    }
    
    @Override
    public final Template getTemplate()
    {
        return getPropertyResourceValue(GP.template).as(Template.class);
    }

    @Override
    public final Double getPrecedence()
    {
        return getProperty(GP.priority).getDouble();
    }

    @Override
    public Map<Argument,RDFNode> getArgumentsMap()
    {
        Map<Argument,RDFNode> map = new HashMap<>();
        Template template = getTemplate();
        if (template != null)
        {
            for (Argument ad : template.getArguments())
            {
                Property argProperty = ad.getPredicate();
                if (argProperty == null)
                {
                    if (log.isErrorEnabled()) log.error("Parameter of template '{}' does not have a predicate", template.getURI());
                    throw new SitemapException("Parameter of template '" + template.getURI() +"' does not have a predicate");
                }

                Statement valueS = getProperty(argProperty);
                if (valueS != null) map.put(ad, valueS.getObject());
            }
        }

        return map;
    }

    @Override
    public QueryBuilder getQueryBuilder(URI base)
    {
        Resource queryOrTemplateCall = getTemplate().getQuery();
        if (queryOrTemplateCall == null)
        {
            if (log.isErrorEnabled()) log.error("Query not defined for template '{}' (gp:query missing)", getTemplate().getURI());
            throw new SitemapException("Query not defined for template '" + getTemplate().getURI() +"'");
        }

        return getQueryBuilder(queryOrTemplateCall, base, getTemplate().getQuery().getModel());
    }

    
    public QueryBuilder getQueryBuilder(Resource queryOrTemplateCall, URI base, Model commandModel)
    {
	if (queryOrTemplateCall == null) throw new IllegalArgumentException("Query Resource cannot be null");
	if (commandModel == null) throw new IllegalArgumentException("Model cannot be null");
        
        SPTextUtil.ensureSPINRDFExists(commandModel);        
        org.topbraid.spin.model.TemplateCall spinTemplateCall = SPINFactory.asTemplateCall(queryOrTemplateCall);
        if (spinTemplateCall != null)
            return QueryBuilder.fromQuery(getQuery(spinTemplateCall, base), commandModel);
        else
        {
            org.topbraid.spin.model.Query query = SPINFactory.asQuery(queryOrTemplateCall);
            if (query == null)
            {
                if (log.isErrorEnabled()) log.error("Class '{}' gp:query value '{}' is not a SPIN Query or TemplateCall", getTemplate().getURI(), queryOrTemplateCall);
                throw new SitemapException("Class '" + getTemplate().getURI() + "' gp:query value '" + queryOrTemplateCall + "' not a SPIN Query or TemplateCall");
            }
            
            return QueryBuilder.fromQuery(getQuery(query, base), commandModel);
        }
    }

    public Query getQuery(org.topbraid.spin.model.TemplateCall spinTemplateCall, URI base)
    {
	if (spinTemplateCall == null) throw new IllegalArgumentException("TemplateCall cannot be null");
	if (base == null) throw new IllegalArgumentException("URI cannot be null");

        return new ParameterizedSparqlString(spinTemplateCall.getQueryString(), null, base.toString()).asQuery();
    }

    public Query getQuery(org.topbraid.spin.model.Query query, URI base)
    {
	if (query == null) throw new IllegalArgumentException("Query cannot be null");
	if (base == null) throw new IllegalArgumentException("URI cannot be null");

        Statement textStmt = query.getRequiredProperty(SP.text);
        if (textStmt == null || !textStmt.getObject().isLiteral())
        {
            if (log.isErrorEnabled()) log.error("SPARQL string not defined for query '{}' (sp:text missing or not a string)", query);
            throw new SitemapException("SPARQL string not defined for query '" + query + "'");                
        }

        return new ParameterizedSparqlString(textStmt.getString(), null, base.toString()).asQuery();
    }
    
    @Override
    public ModifyBuilder getModifyBuilder(URI base)
    {
        Resource updateOrTemplateCall = getTemplate().getUpdate();
        if (updateOrTemplateCall == null)
        {
            if (log.isErrorEnabled()) log.error("Update not defined for template '{}' (gp:update missing)", getTemplate().getURI());
            throw new SitemapException("Update not defined for template '" + getTemplate().getURI() +"'");
        }

        return getModifyBuilder(updateOrTemplateCall, base, getTemplate().getUpdate().getModel());
    }
            
    public ModifyBuilder getModifyBuilder(Resource updateOrTemplateCall, URI base, Model commandModel)
    {
	if (updateOrTemplateCall == null) throw new IllegalArgumentException("Resource cannot be null");
	if (commandModel == null) throw new IllegalArgumentException("Model cannot be null");

        SPTextUtil.ensureSPINRDFExists(commandModel);
        org.topbraid.spin.model.TemplateCall spinTemplateCall = SPINFactory.asTemplateCall(updateOrTemplateCall);        
        if (spinTemplateCall != null)
            return ModifyBuilder.fromUpdate(getUpdateRequest(spinTemplateCall, base).getOperations().get(0), commandModel);
        else
        {
            org.topbraid.spin.model.update.Update update = SPINFactory.asUpdate(updateOrTemplateCall);
            if (update == null)
            {
                if (log.isErrorEnabled()) log.error("Class '{}' gp:update value '{}' is not a SPIN Query or TemplateCall", getTemplate().getURI(), updateOrTemplateCall);
                throw new SitemapException("Class '" + getTemplate().getURI() + "' gp:query value '" + updateOrTemplateCall + "' not a SPIN Query or TemplateCall");
            }
            
            return ModifyBuilder.fromUpdate(getUpdateRequest(update, base).getOperations().get(0), commandModel);
        }
    }

    public UpdateRequest getUpdateRequest(org.topbraid.spin.model.update.Update update, URI base)
    {
	if (update == null) throw new IllegalArgumentException("Resource cannot be null");
	if (base == null) throw new IllegalArgumentException("URI cannot be null");

        Statement textStmt = update.getRequiredProperty(SP.text);
        if (textStmt == null || !textStmt.getObject().isLiteral())
        {
            if (log.isErrorEnabled()) log.error("SPARQL string not defined for update '{}' (sp:text missing or not a string)", update);
            throw new SitemapException("SPARQL string not defined for update '" + update + "'");                
        }

        return new ParameterizedSparqlString(textStmt.getString(), null, base.toString()).asUpdate();
    }

    public UpdateRequest getUpdateRequest(org.topbraid.spin.model.TemplateCall spinTemplateCall, URI base)
    {
	if (spinTemplateCall == null) throw new IllegalArgumentException("Resource cannot be null");
	if (base == null) throw new IllegalArgumentException("URI cannot be null");

        return new ParameterizedSparqlString(spinTemplateCall.getQueryString(), null, base.toString()).asUpdate();
    }
    
    /*
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(getTemplate().getPath());
        hash = 59 * hash + Objects.hashCode(getPrecedence());
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final TemplateCall other = (TemplateCall) obj;
        if (!Objects.equals(getTemplate().getPath(), other.getTemplate().getPath())) return false;
        if (!Objects.equals(getPrecedence(), other.getPrecedence())) return false;
        return true;
    }
    */
    
    @Override
    public String toString()
    {
        return new StringBuilder().
        append("[<").
        append(getTemplate().getURI()).
        append(">, ").
        append(Double.toString(getPrecedence())).
        append("]").
        toString();
    }

    @Override
    public int compareTo(Object obj)
    {
        TemplateCall templateCall = (TemplateCall)obj;
        Double diff = templateCall.getPrecedence() - getPrecedence();
        return diff.intValue();
    }
    
}
