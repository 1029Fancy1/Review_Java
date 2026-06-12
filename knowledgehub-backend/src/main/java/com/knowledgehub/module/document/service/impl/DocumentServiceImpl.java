package com.knowledgehub.module.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledgehub.common.ErrorCode;
import com.knowledgehub.common.PageResult;
import com.knowledgehub.exception.BusinessException;
import com.knowledgehub.module.document.dto.DocumentPageDTO;
import com.knowledgehub.module.document.entity.Document;
import com.knowledgehub.module.document.enums.ParseStatusEnum;
import com.knowledgehub.module.document.mapper.DocumentMapper;
import com.knowledgehub.module.document.service.DocumentService;
import com.knowledgehub.module.document.vo.DocumentVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文档 Service 实现
 */
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    @Value("${knowledgehub.file.upload-path:./uploads}")
    private String uploadPath;

    @Override
    public DocumentVO upload(Long kbId, MultipartFile file, Long userId) {
        try {
            // 1. 校验文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || (!originalFilename.endsWith(".pdf")
                    && !originalFilename.endsWith(".md"))) {
                throw new BusinessException(ErrorCode.DOC_TYPE_NOT_SUPPORTED);
            }

            // 2. UUID 重命名
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + ext;

            // 3. 创建目录
            Path dir = Paths.get(uploadPath).toAbsolutePath().normalize();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            // 4. 写入磁盘
            Path filePath = dir.resolve(newFilename);
            file.transferTo(filePath.toFile());

            // 5. 入库
            Document doc = new Document();
            doc.setKbId(kbId);
            doc.setUserId(userId);
            doc.setTitle(originalFilename);
            doc.setFileType(ext.endsWith(".pdf") ? "PDF" : "MARKDOWN");
            doc.setFilePath(filePath.toString());
            doc.setParseStatus(ParseStatusEnum.PENDING.getCode());
            doc.setChunkCount(0);
            baseMapper.insert(doc);

            return toVO(doc);
        } catch (IOException e) {
            
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public PageResult<DocumentVO> listByPage(DocumentPageDTO dto, Long userId) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getKbId, dto.getKbId())
               .eq(Document::getUserId, userId)
               .orderByDesc(Document::getCreateTime);

        Page<Document> page = new Page<>(dto.getPage(), dto.getSize());
        Page<Document> result = baseMapper.selectPage(page, wrapper);

        List<DocumentVO> voList = result.getRecords()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), dto.getPage(), dto.getSize(), voList);
    }

    @Override
    public DocumentVO getDetail(Long docId, Long userId) {
        Document doc = getWithCheck(docId, userId);
        return toVO(doc);
    }

    
    @Override
    public void delete(Long docId, Long userId) {
        //删除逻辑（含本地文件清理）
        Document doc = getWithCheck(docId, userId);
        try {
            Path filePath = Paths.get(doc.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
        }
        baseMapper.deleteById(docId);
    }
    // ==================== 内部方法 ====================

    private Document getWithCheck(Long docId, Long userId) {
        Document doc = baseMapper.selectById(docId);
        if (doc == null) {
            throw new BusinessException(ErrorCode.DOC_NOT_FOUND);
        }
        if (!doc.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.KB_NO_PERMISSION);
        }
        return doc;
    }

    private DocumentVO toVO(Document doc) {
        ParseStatusEnum statusEnum = ParseStatusEnum.of(doc.getParseStatus());
        return DocumentVO.builder()
                .id(doc.getId())
                .kbId(doc.getKbId())
                .title(doc.getTitle())
                .fileType(doc.getFileType())
                .parseStatus(doc.getParseStatus())
                .parseStatusDesc(statusEnum != null ? statusEnum.getDesc() : "未知")
                .chunkCount(doc.getChunkCount())
                .createTime(doc.getCreateTime())
                .build();
    }
}
