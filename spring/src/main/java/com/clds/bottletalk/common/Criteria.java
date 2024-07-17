package com.clds.bottletalk.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@ToString
public class Criteria {
    private int pageNum;
    private int amount;
    private String searchValue;
    private Sort sort;

    public Criteria() {
        this(1, 5, Sort.unsorted());  // 1페이지, 5개 게시글, 기본 정렬 없음
    }

    public Criteria(int pageNum, int amount, Sort sort) {
        this.pageNum = pageNum;
        this.amount = amount;
        this.sort = sort;
    }
}
