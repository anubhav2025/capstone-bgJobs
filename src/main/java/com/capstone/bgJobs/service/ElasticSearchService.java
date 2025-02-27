package com.capstone.bgJobs.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import com.capstone.bgJobs.enums.ToolTypes;
import com.capstone.bgJobs.model.Finding;
import com.capstone.bgJobs.model.FindingSeverity;
import com.capstone.bgJobs.model.FindingState;
import com.capstone.bgJobs.model.SearchFindingsResult;
import com.capstone.bgJobs.model.Tenant;
import com.capstone.bgJobs.repository.TenantRepository;

@Service
public class ElasticSearchService {

    private final ElasticsearchClient esClient;
    private final TenantRepository tenantRepository;

    public ElasticSearchService(ElasticsearchClient esClient, TenantRepository tenantRepository) {
        this.esClient = esClient;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Save a single Finding document in Elasticsearch.
     * (If you store tenant-based data in separate indices, 
     *  ensure you pick the right index per tenant.)
     */
    public void saveFinding(String tenantId, Finding finding) {
        try {
            Tenant tenant = tenantRepository.findByTenantId(tenantId);
            if (tenant == null) {
                throw new RuntimeException("Tenant not found for tenantId=" + tenantId);
            }

            esClient.index(i -> i
                .index(tenant.getEsIndex())  // store in the tenant's index
                .id(finding.getId())
                .document(finding)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Finding> searchFindingsById(String tenantId, String findingId) {
        try {
            Tenant tenant = tenantRepository.findByTenantId(tenantId);
            if (tenant == null) {
                throw new RuntimeException("Tenant not found for tenantId=" + tenantId);
            }

            SearchResponse<Finding> response = esClient.search(s -> s
                    .index(tenant.getEsIndex())
                    .query(q -> q.term(t -> t
                            .field("_id")
                            .value(findingId)
                    ))
                    .size(1),
                Finding.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Search findings for a given tenant (tenantId) 
     * while optionally filtering by toolType, severity, and state.
     */
    public SearchFindingsResult searchFindings(
            String tenantId,
            ToolTypes toolType,
            FindingSeverity severity,
            FindingState state,
            int page,
            int size
    ) {
        try {
            // 1) Fetch the Tenant to get esIndex
            Tenant tenant = tenantRepository.findByTenantId(tenantId);
            if (tenant == null) {
                throw new RuntimeException("Tenant not found for tenantId=" + tenantId);
            }

            // 2) Execute the search in the tenant's ES index
            SearchResponse<Finding> response = esClient.search(s -> s
                    .index(tenant.getEsIndex())  // use the tenant-specific index
                    .query(q -> q.bool(buildBoolQuery(toolType, severity, state)))
                    .sort(sort -> sort.field(f -> f
                        .field("updatedAt")  // or whichever field you want to sort on
                        .order(SortOrder.Desc)
                    ))
                    .from(page * size)
                    .size(size),
                Finding.class
            );

            // 3) Build a result object
            long total = response.hits().total() != null
                         ? response.hits().total().value()
                         : 0;

            List<Finding> results = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            return new SearchFindingsResult(results, total);

        } catch (Exception e) {
            e.printStackTrace();
            return new SearchFindingsResult(List.of(), 0L);
        }
    }

    /**
     * Helper to build the "bool" query with optional filters on toolType, severity, and state.
     */
    private BoolQuery buildBoolQuery(ToolTypes toolType, FindingSeverity severity, FindingState state) {
        return BoolQuery.of(b -> {
            if (toolType != null) {
                // If you store toolType as a keyword field:
                b.must(m -> m.term(t -> t.field("toolType.keyword").value(toolType.name())));
            }
            if (severity != null) {
                b.must(m -> m.term(t -> t.field("severity.keyword").value(severity.name())));
            }
            if (state != null) {
                b.must(m -> m.term(t -> t.field("state.keyword").value(state.name())));
            }
            return b;
        });
    }
}
