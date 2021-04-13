/*
 * Copyright 2016 Martynas Jusevičius <martynas@atomgraph.com>.
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
package com.atomgraph.server.mapper;

import com.atomgraph.core.MediaTypes;
import com.atomgraph.processor.exception.OntologyException;
import com.atomgraph.processor.model.TemplateCall;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class OntologyExceptionMapper extends ExceptionMapperBase implements ExceptionMapper<OntologyException>
{

    @Inject
    public OntologyExceptionMapper(Ontology ontology, Optional<TemplateCall> templateCall, MediaTypes mediaTypes)
    {
        super(ontology, templateCall, mediaTypes);
    }
    
    @Override
    public Response toResponse(OntologyException ex)
    {
        return getResponseBuilder(DatasetFactory.create(toResource(ex, Response.Status.INTERNAL_SERVER_ERROR,
                        ResourceFactory.createResource("http://www.w3.org/2011/http-statusCodes#InternalServerError")).
                    getModel())).
                status(Response.Status.INTERNAL_SERVER_ERROR).
                build();
    }

}
