package com.offcn.list.repository;

import com.offcn.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
