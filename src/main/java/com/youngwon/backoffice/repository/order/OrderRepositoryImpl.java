package com.youngwon.backoffice.repository.order;

import com.youngwon.backoffice.dto.order.query.OrderQueryCond;
import com.youngwon.backoffice.dto.order.query.OrderRow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<OrderRow> searchOrderRows(Long shopId, OrderQueryCond cond, Pageable pageable) {
        QueryParts parts = buildWhere(shopId, cond);

        String dir = cond != null && cond.isOrderedAtAsc() ? "asc" : "desc";

        String idJpql = """
                select o.id
                from Order o
                """ + parts.whereClause + """
                order by o.orderedAt %s, o.id %s
                """.formatted(dir, dir);

        TypedQuery<Long> idQuery = em.createQuery(idJpql, Long.class);
        applyParams(idQuery, parts.params);

        idQuery.setFirstResult((int) pageable.getOffset());
        idQuery.setMaxResults(pageable.getPageSize());

        List<Long> ids = idQuery.getResultList();
        if (ids.isEmpty()) {
            long total = count(shopId, parts);
            return new PageImpl<>(List.of(), pageable, total);
        }

        String rowJpql = """
                select new com.youngwon.backoffice.dto.order.OrderRow(
                    o.id,
                    o.orderNo,
                    o.salesChannel,
                    o.externalRef,
                    o.status,
                    o.orderedAt,
                    o.customerName,
                    o.customerPhone,
                    o.grossAmount.amount,
                    o.deductionAmount.amount,
                    o.settlementAmount.amount,
                    o.refundAmount.amount
                )
                from Order o
                where o.shopId = :shopId
                  and o.id in :ids
                order by o.orderedAt %s, o.id %s
                """.formatted(dir, dir);

        TypedQuery<OrderRow> rowQuery = em.createQuery(rowJpql, OrderRow.class);
        rowQuery.setParameter("shopId", shopId);
        rowQuery.setParameter("ids", ids);

        List<OrderRow> content = rowQuery.getResultList();

        long total = count(shopId, parts);
        return new PageImpl<>(content, pageable, total);
    }

    private long count(Long shopId, QueryParts parts) {
        String countJpql = """
                select count(o.id)
                from Order o
                """ + parts.whereClause;

        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
        applyParams(countQuery, parts.params);
        return countQuery.getSingleResult();
    }

    private QueryParts buildWhere(Long shopId, OrderQueryCond cond) {
        StringBuilder where = new StringBuilder("where o.shopId = :shopId\n");
        Map<String, Object> params = new HashMap<>();
        params.put("shopId", shopId);

        if (cond == null) {
            return new QueryParts(where.toString(), params);
        }

        if (cond.getFrom() != null) {
            where.append("  and o.orderedAt >= :from\n");
            params.put("from", cond.getFrom());
        }
        if (cond.getTo() != null) {
            where.append("  and o.orderedAt < :to\n");
            params.put("to", cond.getTo());
        }
        if (cond.getStatus() != null) {
            where.append("  and o.status = :status\n");
            params.put("status", cond.getStatus());
        }
        if (cond.getSalesChannel() != null) {
            where.append("  and o.salesChannel = :salesChannel\n");
            params.put("salesChannel", cond.getSalesChannel());
        }

        if (cond.getKeywordType() != null && StringUtils.hasText(cond.getKeyword())) {
            switch (cond.getKeywordType()) {
                case ORDER_NO -> {
                    where.append("  and o.orderNo like :kw\n");
                    params.put("kw", likeKeyword(cond.getKeyword()));
                }
                case EXTERNAL_REF -> {
                    where.append("  and o.externalRef like :kw\n");
                    params.put("kw", likeKeyword(cond.getKeyword()));
                }
                case CUSTOMER_PHONE -> {
                    where.append("  and o.customerPhone like :kw\n");
                    params.put("kw", likeKeyword(cond.getKeyword()));
                }
                case CUSTOMER_NAME -> {
                    where.append("  and o.customerName like :kw\n");
                    params.put("kw", likeKeyword(cond.getKeyword()));
                }
            }
        }

        return new QueryParts(where.toString(), params);
    }

    private static String likeKeyword(String keyword) {
        String k = keyword.trim();
        return "%" + k + "%";
    }

    private static void applyParams(TypedQuery<?> query, Map<String, Object> params) {
        for (Map.Entry<String, Object> e : params.entrySet()) {
            query.setParameter(e.getKey(), e.getValue());
        }
    }

    private record QueryParts(String whereClause, Map<String, Object> params) {}
}