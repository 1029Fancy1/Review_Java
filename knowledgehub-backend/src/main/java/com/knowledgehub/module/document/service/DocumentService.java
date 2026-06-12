package com.knowledgehub.module.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledgehub.common.PageResult;
import com.knowledgehub.module.document.dto.DocumentPageDTO;
import com.knowledgehub.module.document.entity.Document;
import com.knowledgehub.module.document.vo.DocumentVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档 Service 接口
 */
public interface DocumentService extends IService<Document> {

    /**
     * 上传文档
     *
     * @param kbId   知识库 ID
     * @param file   上传的文件
     * @param userId 当前用户 ID
     * @return 文档信息
     */
    DocumentVO upload(Long kbId, MultipartFile file, Long userId);

    /**
     * 分页查询文档列表（按知识库过滤）
     */
    PageResult<DocumentVO> listByPage(DocumentPageDTO dto, Long userId);

    /**
     * 文档详情
     */
    DocumentVO getDetail(Long docId, Long userId);

    /**
     * 删除文档
     */
    void delete(Long docId, Long userId);
}
