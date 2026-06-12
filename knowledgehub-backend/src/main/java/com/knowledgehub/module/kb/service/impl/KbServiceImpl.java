package com.knowledgehub.module.kb.service.impl;
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
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库 Service 实现
 */
@Service
public class KbServiceImpl extends ServiceImpl<KbMapper, KnowledgeBase> implements KbService {

    @Override
    public KbVO create(KbCreateDTO dto, Long userId) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setUserId(userId);
        kb.setName(dto.getName());
        kb.setDescription(dto.getDescription());
        kb.setKbVersion(1);
        baseMapper.insert(kb);
        return toVO(kb);
    }

    @Override
    public PageResult<KbVO> listByPage(KbPageDTO dto, Long userId) {
        // 构建查询条件：只看自己的知识库
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getUserId, userId)
               .orderByDesc(KnowledgeBase::getCreateTime);

        // 分页查询
        Page<KnowledgeBase> page = new Page<>(dto.getPage(), dto.getSize());
        Page<KnowledgeBase> result = baseMapper.selectPage(page, wrapper);

        // Entity → VO 转换
        List<KbVO> voList = result.getRecords()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), dto.getPage(), dto.getSize(), voList);
    }

    @Override
    public KbVO getDetail(Long kbId, Long userId) {
        KnowledgeBase kb = getWithCheck(kbId, userId);
        return toVO(kb);
    }


    //更新逻辑
    @Override
    public KbVO update(Long kbId, KbUpdateDTO dto, Long userId) {
        KnowledgeBase kb = getWithCheck(kbId, userId);

        kb.setName(dto.getName());
        kb.setDescription(dto.getDescription());
        kb.setKbVersion(kb.getKbVersion() +1);
        baseMapper.updateById(kb);
        return toVO(kb);
    }

    /**
     *删除知识库逻辑。
     */
    @Override
    public void delete(Long kbId, Long userId) {
        getWithCheck(kbId, userId);
        baseMapper.deleteById(kbId);
    }

    // ==================== 内部方法 ====================

    /**
     * 校验知识库存在且属于当前用户
     *
     * 这是用户隔离的核心方法，所有操作前先调用它。
     * id不存在或者id不符
     */
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

    /**
     * Entity → VO 转换
     *
     * docCount 暂时写 0，Day 5 文档上传后再关联统计。
     */
    private KbVO toVO(KnowledgeBase kb) {
        return KbVO.builder()
                .id(kb.getId())
                .name(kb.getName())
                .description(kb.getDescription())
                .docCount(0)  // Day 5 统计文档数
                .createTime(kb.getCreateTime())
                .updateTime(kb.getUpdateTime())
                .build();
    }
}
