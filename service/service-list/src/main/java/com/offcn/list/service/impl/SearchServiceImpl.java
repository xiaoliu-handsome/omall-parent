package com.offcn.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.offcn.list.repository.GoodsRepository;
import com.offcn.list.service.SearchService;
import com.offcn.model.list.*;
import com.offcn.model.product.*;
import com.offcn.product.client.ProductFeignClient;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    /**
     * 上架商品列表
     * @param skuId
     */
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        //查询sku对应的平台属性
        List<BaseAttrInfo> baseAttrInfoList =  productFeignClient.getAttrList(skuId);
        if(null != baseAttrInfoList) {
            List<SearchAttr> searchAttrList =  baseAttrInfoList.stream().map(baseAttrInfo -> {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                //一个sku只对应一个属性值
                List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getAttrValueList();
                searchAttr.setAttrValue(baseAttrValueList.get(0).getValueName());
                return searchAttr;
            }).collect(Collectors.toList());

            goods.setAttrs(searchAttrList);
        }

        //查询sku信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        // 查询品牌
        BaseTrademark baseTrademark = productFeignClient.getTrademark(skuInfo.getTmId());
        if (baseTrademark != null){
            goods.setTmId(skuInfo.getTmId());
            goods.setTmName(baseTrademark.getTmName());
            goods.setTmLogoUrl(baseTrademark.getLogoUrl());

        }

        // 查询分类
        BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        if (baseCategoryView != null) {
            goods.setCategory1Id(baseCategoryView.getCategory1Id());
            goods.setCategory1Name(baseCategoryView.getCategory1Name());
            goods.setCategory2Id(baseCategoryView.getCategory2Id());
            goods.setCategory2Name(baseCategoryView.getCategory2Name());
            goods.setCategory3Id(baseCategoryView.getCategory3Id());
            goods.setCategory3Name(baseCategoryView.getCategory3Name());
        }

        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setId(skuInfo.getId());
        goods.setTitle(skuInfo.getSkuName());
        goods.setCreateTime(new Date());

        this.goodsRepository.save(goods);
    }

    /**
     * 下架商品列表
     * @param skuId
     */
    @Override
    public void lowerGoods(Long skuId) {
        this.goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        // 定义key
        String hotKey = "hotScore";
        // 保存数据
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);
        if (hotScore%10==0){
            // 更新es
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(Math.round(hotScore));
            goodsRepository.save(goods);
        }
    }

    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {
        // 构建dsl语句
        SearchRequest searchRequest = this.buildQueryDsl(searchParam);
        SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(response);

        SearchResponseVo responseVO = this.parseSearchResult(response);
        responseVO.setPageSize(searchParam.getPageSize());
        responseVO.setPageNo(searchParam.getPageNo());
        long totalPages = (responseVO.getTotal()+searchParam.getPageSize()-1)/searchParam.getPageSize();
        responseVO.setTotalPages(totalPages);
        return responseVO;

    }
    //制作DSL语句
    public SearchRequest buildQueryDsl(SearchParam searchParam){
        //构建查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建多条件查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 判断查询条件是否为空 关键字
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            MatchQueryBuilder title = QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND);
            //关联查询条件到多条件查询对象
            boolQueryBuilder.must(title);
        }
        //判断品牌查询条件是否为空
        String trademark = searchParam.getTrademark();
        if(!StringUtils.isEmpty(trademark)){
            //切开品牌查询条件 trademark=2:华为
            String[] split = trademark.split(":");
            if(split!=null&&split.length==2){
                //创建按照品牌编号进行筛选
                TermQueryBuilder tmId = QueryBuilders.termQuery("tmId", split[0]);
                //关联查询条件到多条件查询对象
                boolQueryBuilder.filter(tmId);
            }

        }
        //构建分类查询条件
        if(null!=searchParam.getCategory1Id()){
            TermQueryBuilder category1Id = QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id());
            boolQueryBuilder.filter(category1Id);
        }
        if(null!=searchParam.getCategory2Id()){
            TermQueryBuilder category2Id = QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id());
            boolQueryBuilder.filter(category2Id);
        }
        if(null!=searchParam.getCategory3Id()){
            TermQueryBuilder category3Id = QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id());
            boolQueryBuilder.filter(category3Id);
        }
        // 构建平台属性查询
        // 23:4G:运行内存
        String[] props = searchParam.getProps();
        if(props!=null&&props.length>0){
            for (String prop : props) {
                //切开
                String[] split = prop.split(":");
                if(split!=null&&split.length==3){
                    //构建嵌套查询对象
                    BoolQueryBuilder boolQuery =  QueryBuilders.boolQuery();
                    //构建嵌套查询子查询对象
                    BoolQueryBuilder subboolQuery=   QueryBuilders.boolQuery();
                    //构建子查询中的过滤条件
                    subboolQuery.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                    subboolQuery.must(QueryBuilders.termQuery("attrs.attrValue",split[1]));
                    //把子查询合并到嵌套查询对象,设置查询模式nested
                    boolQuery.must(QueryBuilders.nestedQuery("attrs",subboolQuery, ScoreMode.None));
                    //管理嵌套查询到多条件查询
                    boolQueryBuilder.filter(boolQuery);
                }
            }
        }

        // 关联多条件查询器对象，到主查询器对象
        searchSourceBuilder.query(boolQueryBuilder);
        //计算分页起始页
        int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        //设置分页参数
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());

        //排序
        String order = searchParam.getOrder();
        if(!StringUtils.isEmpty(order)){
            //切开排序条件
            String[] split = order.split(":");
            if(split!=null&&split.length==2){
                //排序字段
                String field=null;
                switch (split[0]){
                    case "1":
                        field="hotScore";
                        break;
                    case "2":
                        field="price";
                        break;
                }
                //设置排序条件
                searchSourceBuilder.sort(field,"asc".equals(split[1])? SortOrder.ASC:SortOrder.DESC);
            }else {
                //前端没有传递排序条件设置默认排序
                searchSourceBuilder.sort("hotScore",SortOrder.DESC);
            }
        }

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style=color:red>");
        highlightBuilder.postTags("</span>");
        //关联高亮对象到主查询器对象
        searchSourceBuilder.highlighter(highlightBuilder);

        //设置品牌聚合
        TermsAggregationBuilder brandTermsAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        //关联品牌聚合到主查询器对象
        searchSourceBuilder.aggregation(brandTermsAggregationBuilder);

        //设置平台属性聚合
        NestedAggregationBuilder attrTermsAggregationBuilder = AggregationBuilders.nested("attrAgg", "attrs");
        AggregationBuilder attrIdAgg=  AggregationBuilders.terms("attrIdAgg").field("attrs.attrId");
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"));
        attrTermsAggregationBuilder.subAggregation(attrIdAgg);
        searchSourceBuilder.aggregation(attrTermsAggregationBuilder);

        //定制要显示的字段
        searchSourceBuilder.fetchSource(new String[]{"id","defaultImg","title","price"},null);
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("goods");
        //关联主查询对象到搜索请求对象
        searchRequest.source(searchSourceBuilder);
        System.out.println("dsl:"+searchSourceBuilder.toString());
        return searchRequest;
    }
    //解析响应结果
    private SearchResponseVo parseSearchResult(SearchResponse response){
        SearchHits hits = response.getHits();
        //声明响应对象
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        //获取全部的聚合结果
        Map<String, Aggregation> aggregationMap = response.getAggregations().getAsMap();
        //获取品牌的聚合结果
        ParsedLongTerms tmIdAgg= (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        List<SearchResponseTmVo> searchResponseTmVoList = tmIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //设置品牌编号
            searchResponseTmVo.setTmId(Long.parseLong(((Terms.Bucket) bucket).getKeyAsString()));
            //获取子节点的分组结果
            Map<String, Aggregation> tmIdSubMap = ((Terms.Bucket) bucket).getAggregations().asMap();
            //获取子分组的品牌名称
            ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmIdSubMap.get("tmNameAgg");
            //获取品牌名称
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            //设置品牌名称到响应对象
            searchResponseTmVo.setTmName(tmName);
            //获取子分组的 品牌logo配图
            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) tmIdSubMap.get("tmLogoUrlAgg");
            //获取配图
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            return searchResponseTmVo;
        }).collect(Collectors.toList());

        //关联设置品牌集合到主响应对象
        searchResponseVo.setTrademarkList(searchResponseTmVoList);

        //获取搜索结果
        SearchHit[] subHits = hits.getHits();
        List<Goods> goodsList=new ArrayList<>();
        if(subHits!=null&&subHits.length>0){
            for (SearchHit subHit : subHits) {
                //获取查询对象，从json转换为对象
                Goods goods = JSON.parseObject(subHit.getSourceAsString(), Goods.class);
                //获取高亮
                if(subHit.getHighlightFields().get("title")!=null){
                    //读取高亮标题
                    Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                    //设置标题到商品对象
                    goods.setTitle(title.toString());
                }
                //添加商品对象到集合
                goodsList.add(goods);
            }
        }

        //设置搜索结果到响应对象
        searchResponseVo.setGoodsList(goodsList);

//获取平台属性的聚合结果
        ParsedNested attrAgg= (ParsedNested) aggregationMap.get("attrAgg");
        //获取平台属性子节点attrIdAgg
        ParsedLongTerms attrIdAgg=   attrAgg.getAggregations().get("attrIdAgg");
        //获取平台属性子节点attrIdAgg的存储桶
        List<? extends Terms.Bucket> idAggBuckets = attrIdAgg.getBuckets();
        //判断存储桶是否为空
        if(!CollectionUtils.isEmpty(idAggBuckets)){
            List<SearchResponseAttrVo> searchResponseAttrVos = idAggBuckets.stream().map(bucket -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                searchResponseAttrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                //获取子节点属性名的聚合结果
                ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
                if(attrNameAgg!=null) {
                    List<? extends Terms.Bucket> nameAggBuckets = attrNameAgg.getBuckets();
                    searchResponseAttrVo.setAttrName(nameAggBuckets.get(0).getKeyAsString());
                }
                //获取子节点属性值的聚合结果
                ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
                if(attrValueAgg!=null) {
                    List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                    List<String> list = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    searchResponseAttrVo.setAttrValueList(list);
                }
                return searchResponseAttrVo;
            }).collect(Collectors.toList());

            //设置
            searchResponseVo.setAttrsList(searchResponseAttrVos);

        }

        //设置总记录数
        searchResponseVo.setTotal(hits.getTotalHits());

        return searchResponseVo;
    }

}

