package com.ogqcorp.metabrowser.content.specification;

import com.ogqcorp.metabrowser.common.SearchCriteria;
import com.ogqcorp.metabrowser.domain.Content;
import com.ogqcorp.metabrowser.domain.Tag;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

public class ContentSpecification implements Specification<Content>{
    private SearchCriteria criteria;

    public ContentSpecification(SearchCriteria searchCriteria){
        this.criteria = searchCriteria;
    }
    @Override
    public Predicate toPredicate(Root<Content> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        if (criteria.getOperation().equalsIgnoreCase(">")) {
            return criteriaBuilder.greaterThanOrEqualTo(
                    root.<String> get(criteria.getKey()), criteria.getValue().toString());
        }
        else if (criteria.getOperation().equalsIgnoreCase("<")) {
            return criteriaBuilder.lessThanOrEqualTo(
                    root.<String> get(criteria.getKey()), criteria.getValue().toString());
        }
        else if (criteria.getOperation().equalsIgnoreCase(":")) {
            if (root.get(criteria.getKey()).getJavaType() == String.class) {
                return criteriaBuilder.like(
                        root.<String>get(criteria.getKey()), "%" + criteria.getValue() + "%");
            } else {
                return criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue());
            }
        }


        return null;
    }
}
