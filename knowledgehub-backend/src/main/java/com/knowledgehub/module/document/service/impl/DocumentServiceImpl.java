package com.knowledgehub.module.document.service.impl;

import com.alibaba.fastjson.JSON;
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
import com.knowledgehub.redis.CacheService;
import com.knowledgehub.redis.RedisKeyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.print.Doc;
/**
 * 缓存逻辑：
 * - 读：查缓存 → 命中 null 标记 → 返回"不存在"
 *                 → 命中真实数据 → 直接返回
 *                 → 未命中 → 查 DB → 存在则写缓存，不存在则写空值缓存
 * - 删：删除 DB → 删缓存
 */
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    @Value("${knowledgehub.file.upload-path:./uploads}")
    private String uploadPath;

    private final CacheService cacheService;

    // 文档详情缓存 TTL：30-60 分钟随机
    private static final int DOC_TTL_MIN = 30 * 60;
    private static final int DOC_TTL_MAX = 60 * 60;
    // 空值缓存 TTL：5 分钟（不存在的数据不会突然存在，短 TTL 足够防攻击）
    private static final int NULL_TTL = 5 * 60;
    private static final Random RANDOM = new Random();

    private Duration randomDocTtl() {
        return Duration.ofSeconds(DOC_TTL_MIN + RANDOM.nextInt(DOC_TTL_MAX - DOC_TTL_MIN + 1));
    }

    private static final String NULL_MARKER = "__NULL__";

    @Override
    public DocumentVO upload(Long kbId, MultipartFile file, Long userId) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || (!originalFilename.endsWith(".pdf")
                    && !originalFilename.endsWith(".md"))) {
                throw new BusinessException(ErrorCode.DOC_TYPE_NOT_SUPPORTED);
            }

            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + ext;

            Path dir = Paths.get(uploadPath).toAbsolutePath().normalize();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            Path filePath = dir.resolve(newFilename);
            file.transferTo(filePath.toFile());
 
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
        // 文档详情缓存（Cache Aside + 空值缓存）
        // 1. 查缓存
        // 2. NULL_MARKER 命中 → 抛 DOC_NOT_FOUND
        // 3. 正常命中 → JSON.parseObject 返回
        // 4. 未命中 → 查 DB
        // 5. 不存在 → 写空值缓存（NULL_TTL）→ 抛 DOC_NOT_FOUND
        // 6. 存在 → 验权限 → 写正常缓存（randomDocTtl）→ 返回
        String cacheKey = RedisKeyBuilder.docDetail(docId);

        //查缓存
        String cached = cacheService.get(cacheKey);
        if (cached != null) {
            if (NULL_MARKER.equals(cached)) {
                //空值命中
                throw new BusinessException(ErrorCode.DOC_NOT_FOUND);
            }
        return JSON.parseObject(cached,DocumentVO.class);
        }

        //未命中 查DB
        Document doc = baseMapper.selectById(docId);
        
        //文档不存在 写空值缓存 短TTL 抛异常
        if (doc == null) {
            cacheService.set(cacheKey,NULL_MARKER,Duration.ofSeconds(NULL_TTL));
            throw new BusinessException(ErrorCode.DOC_NOT_FOUND);
        }

        //校验权限 userid
        if (!doc.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.KB_NO_PERMISSION);   
        }

        //文档存在 正常写缓存
        DocumentVO vo = toVO(doc);
        cacheService.set(cacheKey, JSON.toJSONString(vo), randomDocTtl());

        return vo;
    }

    @Override
    public void delete(Long docId, Long userId) {
        Document doc = getWithCheck(docId, userId);
        try {
            Path filePath = Paths.get(doc.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
        }
        baseMapper.deleteById(docId);
        //删除文档详情缓存
        cacheService.delete(RedisKeyBuilder.docDetail(docId));
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
