package com.knowledgehub.module.kb.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledgehub.common.ErrorCode;
import com.knowledgehub.common.PageResult;
import com.knowledgehub.exception.BusinessException;
import com.knowledgehub.module.kb.dto.KbCreateDTO;
import com.knowledgehub.module.kb.dto.KbPageDTO;
import com.knowledgehub.module.kb.dto.KbUpdateDTO;
import com.knowledgehub.module.kb.entity.KnowledgeBase;
import com.knowledgehub.module.kb.mapper.KbMapper;
import com.knowledgehub.module.kb.service.KbService;
import com.knowledgehub.module.kb.vo.KbVO;
import com.knowledgehub.redis.CacheService;
import com.knowledgehub.redis.RedisKeyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 知识库 Service 实现 — Day 11 集成 Cache Aside 缓存
 *
 * 缓存逻辑：
 * - 读：查缓存 → 命中返回 → 未命中查 DB → 写缓存 → 返回
 * - 写：更新/删除 DB → 删缓存 → 下次读时重建
 */
@Service
@RequiredArgsConstructor
public class KbServiceImpl extends ServiceImpl<KbMapper, KnowledgeBase> implements KbService {

    private final CacheService cacheService;

    // 随机 TTL 10-30 分钟，防止批量过期同时回源（缓存雪崩）
    private static final int TTL_MIN = 10 * 60;
    private static final int TTL_MAX = 30 * 60;
    private static final Random RANDOM = new Random();

    private Duration randomTtl() {
        return Duration.ofSeconds(TTL_MIN + RANDOM.nextInt(TTL_MAX - TTL_MIN + 1));
    }

    /**
     * TODO: 创建完成后删除列表缓存。
     *
     * 学习目标：理解"写操作后删缓存"——让下次查询自己重建缓存
     *
     * 参考实现：
     * cacheService.delete(RedisKeyBuilder.kbList(userId));
     */
    @Override
    public KbVO create(KbCreateDTO dto, Long userId) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setUserId(userId);
        kb.setName(dto.getName());
        kb.setDescription(dto.getDescription());
        kb.setKbVersion(1);
        baseMapper.insert(kb);
        //删除列表缓存
        cacheService.delete(RedisKeyBuilder.kbList(userId));
        return toVO(kb);
    }

    /**
     * TODO: 请你手敲完成 Cache Aside 读取逻辑。
     *
     * 学习目标：
     * 1. 理解"先查缓存，没有再查 DB"的流程
     * 2. 理解缓存命中后不需要查 DB
     * 3. 理解查 DB 后要把结果写回缓存
     *
     * 参考实现（请不要直接复制，建议手敲）：
     *
     * // 1. 构建缓存 key
     * String cacheKey = RedisKeyBuilder.kbList(userId);
     *
     * // 2. 查缓存
     * String cached = cacheService.get(cacheKey);
     * if (cached != null) {
     *     // 缓存命中，直接返回
     *     return JSON.parseObject(cached, PageResult.class);
     * }
     *
     * // 3. 缓存未命中 → 查 DB（这段已有，保留）
     * [查 DB 的逻辑已经在下面了]
     *
     * // 4. 写缓存
     * cacheService.set(cacheKey, JSON.toJSONString(pageResult), randomTtl());
     *
     * return pageResult;
     */
    @Override
    public PageResult<KbVO> listByPage(KbPageDTO dto, Long userId) {
        //1. 构建缓存 key → 2. 查缓存 → 命中直接返回
        String cacheKey = RedisKeyBuilder.kbList(userId);
        String cached = cacheService.get(cacheKey);
        if (cached != null) {
            //缓存命中直接返回
            return JSON.parseObject(cached,PageResult.class);
        }
        // 缓存未命中 → 查 DB
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getUserId, userId)
               .orderByDesc(KnowledgeBase::getCreateTime);

        Page<KnowledgeBase> page = new Page<>(dto.getPage(), dto.getSize());
        Page<KnowledgeBase> result = baseMapper.selectPage(page, wrapper);

        List<KbVO> voList = result.getRecords()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        PageResult<KbVO> pageResult = PageResult.of(
                result.getTotal(), dto.getPage(), dto.getSize(), voList);

        //3. 写缓存（用 JSON.toJSONString 序列化，TTL 用 randomTtl()）
        cacheService.set(cacheKey, JSON.toJSONString(pageResult), randomTtl());
        return pageResult;
    }

    @Override
    public KbVO getDetail(Long kbId, Long userId) {
        KnowledgeBase kb = getWithCheck(kbId, userId);
        return toVO(kb);
    }

   
    @Override
    public KbVO update(Long kbId, KbUpdateDTO dto, Long userId) {
        KnowledgeBase kb = getWithCheck(kbId, userId);
        kb.setName(dto.getName());
        kb.setDescription(dto.getDescription());
        kb.setKbVersion(kb.getKbVersion() + 1);
        baseMapper.updateById(kb);
        //删除列表缓存
        cacheService.delete(RedisKeyBuilder.kbList(userId));
        return toVO(kb);
    }

 
    @Override
    public void delete(Long kbId, Long userId) {
        getWithCheck(kbId, userId);
        baseMapper.deleteById(kbId);
        //删除列表缓存
        cacheService.delete(RedisKeyBuilder.kbList(userId));
    }

    // ==================== 内部方法 ====================

    private KnowledgeBase getWithCheck(Long kbId, Long userId) {
        KnowledgeBase kb = baseMapper.selectById(kbId);
        if (kb == null) {
            throw new BusinessException(ErrorCode.KB_NOT_FOUND);
        }
        if (!kb.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.KB_NO_PERMISSION);
        }
        return kb;
    }

    private KbVO toVO(KnowledgeBase kb) {
        return KbVO.builder()
                .id(kb.getId())
                .name(kb.getName())
                .description(kb.getDescription())
                .docCount(0)
                .createTime(kb.getCreateTime())
                .updateTime(kb.getUpdateTime())
                .build();
    }
}
