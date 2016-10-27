/*
 * Copyright 2008-2009 the original 赵永春(zyc@hasor.net).
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
package net.hasor.search.server.rsf.service;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.hasor.core.AppContext;
import net.hasor.core.InjectMembers;
import net.hasor.rsf.RsfOptionSet;
import net.hasor.rsf.rpc.objects.warp.RsfRequestLocal;
import net.hasor.search.domain.SearchDocument;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @version : 2015年1月16日
 * @author 赵永春(zyc@hasor.net)
 */
public abstract class AbstractSearchService implements InjectMembers {
    protected Logger        logger     = LoggerFactory.getLogger(getClass());
    private RsfRequestLocal rsfRequest = new RsfRequestLocal();
    private CoreContainer   container  = null;
    @Override
    public void doInject(AppContext appContext) {
        this.container = appContext.getInstance(CoreContainer.class);
        if (this.container == null) {
            throw new NullPointerException();
        }
    }
    protected RsfOptionSet getRsfOptionSet() {
        return this.rsfRequest;
    }
    protected SolrClient getSolrClient() {
        String coreName = this.rsfRequest.getBindInfo().getBindGroup();
        return new EmbeddedSolrServer(this.container, coreName);
    }
    //
    protected SearchDocument convetTo(SolrDocument solrDocument) {
        Set<Map.Entry<String, Object>> docDataEntrySet = solrDocument.entrySet();
        SearchDocument document = new SearchDocument();
        for (Map.Entry<String, Object> entry : docDataEntrySet) {
            document.setField(entry.getKey(), entry.getValue());
        }
        List<SolrDocument> solrList = solrDocument.getChildDocuments();
        if (solrList != null) {
            for (SolrDocument solrDoc : solrList) {
                document.addChildDocument(convetTo(solrDoc));
            }
        }
        return document;
    }
    protected SolrInputDocument convetTo(SearchDocument searchDocument) {
        Set<Map.Entry<String, Object>> docDataEntrySet = searchDocument.entrySet();
        SolrInputDocument document = new SolrInputDocument();
        for (Map.Entry<String, Object> entry : docDataEntrySet) {
            document.setField(entry.getKey(), entry.getValue());
        }
        List<SearchDocument> searchList = searchDocument.getChildDocuments();
        if (searchList != null) {
            for (SearchDocument searchDoc : searchList) {
                document.addChildDocument(convetTo(searchDoc));
            }
        }
        return document;
    }
    protected SolrInputDocument convetTo(Map<String, ?> docMap) {
        SolrInputDocument document = new SolrInputDocument();
        for (Map.Entry<String, ?> entry : docMap.entrySet()) {
            document.setField(entry.getKey(), entry.getValue());
        }
        return document;
    }
}