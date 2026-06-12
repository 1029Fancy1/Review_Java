package com.knowledgehub.module.kb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledgehub.common.PageResult;
import com.knowledgehub.module.kb.dto.KbCreateDTO;
import com.knowledgehub.module.kb.dto.KbPageDTO;
import com.knowledgehub.module.kb.dto.KbUpdateDTO;
import com.knowledgehub.module.kb.entity.KnowledgeBase;
import com.knowledgehub.module.kb.vo.KbVO;

/**
 * 知识库 Service 接口
 */
public interface KbService extends IService<KnowledgeBase> {

    /**
     * 创建知识库
     *
     * @param dto    创建信息
     * @param userId 当前用户 ID
     * @return 创建成功的知识库
     */
    KbVO create(KbCreateDTO dto, Long userId);

    /**
     * 分页查询知识库列表（按用户隔离）
     *
     * @param dto    分页参数
     * @param userId 当前用户 ID
     * @return 分页结果
     */
    PageResult<KbVO> listByPage(KbPageDTO dto, Long userId);

    /**
     * 查询知识库详情
     *
     * @param kbId   知识库 ID
     * @param userId 当前用户 ID
     * @return 知识库详情
     */
    KbVO getDetail(Long kbId, Long userId);

    /**
     * 更新知识库
     *
     * @param kbId   知识库 ID
     * @param dto    更新信息
     * @param userId 当前用户 ID
     * @return 更新后的知识库
     */
    KbVO update(Long kbId, KbUpdateDTO dto, Long userId);

    /**
     * 删除知识库
     *
     * @param kbId   知识库 ID
     * @param userId 当前用户 ID
     */
    void delete(Long kbId, Long userId);
}
