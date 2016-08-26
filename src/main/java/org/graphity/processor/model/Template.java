/*
 * Copyright 2016 Martynas Jusevičius <martynas@graphity.org>.
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
package org.graphity.processor.model;

import com.sun.jersey.api.uri.UriTemplate;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.core.CacheControl;
import org.apache.jena.ontology.OntClass;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public interface Template extends OntClass
{
    
    UriTemplate getPath();
    
    String getSkolemTemplate();

    String getFragmentTemplate();
    
    org.apache.jena.rdf.model.Resource getQuery();
    
    org.apache.jena.rdf.model.Resource getUpdate();
    
    Double getPriority();
    
    List<Argument> getArguments();
   
    List<Locale> getLanguages();
    
    org.apache.jena.rdf.model.Resource getLoadClass();
    
    CacheControl getCacheControl();
    
}
