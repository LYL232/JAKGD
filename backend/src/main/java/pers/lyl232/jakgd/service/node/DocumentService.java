package pers.lyl232.jakgd.service.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.lyl232.jakgd.entity.node.Document;
import pers.lyl232.jakgd.exception.ObjectNotFoundException;
import pers.lyl232.jakgd.repository.node.DocumentRepository;
import pers.lyl232.jakgd.service.BaseService;

import java.util.List;

@Service
public class DocumentService extends BaseService {

    final static Logger logger = LoggerFactory.getLogger(Document.class);

    final private DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * 获取指定节点的按索引顺序排列的文档
     *
     * @param id 节点id
     * @return Document对象列表
     */
    @Transactional(readOnly = true)
    public List<Document> getNodeDocuments(Long id) throws ObjectNotFoundException {
        List<Document> res = documentRepository.getNodeDocuments(id);
        if (res == null) {
            throw new ObjectNotFoundException("Node id: " + id);
        }
        return res;
    }

    /**
     * 根据id获取指定的Document对象
     *
     * @param id id
     * @return Document 对象
     * @throws ObjectNotFoundException 找不到异常
     */
    public Document getNotNull(Long id) throws ObjectNotFoundException {
        Document document = documentRepository.get(id);
        if (document == null) {
            throw new ObjectNotFoundException("Document id: " + id);
        }
        return document;
    }

    /**
     * 给节点添加一个part markdown Document
     *
     * @param id      node id
     * @param author  作者用户名
     * @param docName 文档名字
     * @param content markdown 内容
     * @return Document id
     * @throws ObjectNotFoundException 创建失败
     */
    @Transactional
    public Long create(
            Long id, String author, String docName, String content)
            throws ObjectNotFoundException {
        Integer dCount = documentRepository.getPartDocumentsCount(id);
        if (dCount == null) {
            throw new ObjectNotFoundException("Node id: " + id);
        }
        String now = getFormatNowDateStringBySecond();
        return documentRepository.createPartDocument(
                id, "markdown", docName, content, now, now, author, dCount
        );
    }

    /**
     * 覆盖修改文档
     *
     * @param document Document object
     */
    public void update(Document document) {
        document.setUpdated(getFormatNowDateStringBySecond());
        documentRepository.save(document);
    }


    /**
     * 向下交换Document => index++
     *
     * @param kId Node id
     * @param dId Document id
     */
    public void switchDocumentUpward(Long kId, Long dId) {
        documentRepository.switchDocumentUpward(kId, dId);
    }

    /**
     * 向上交换Document => index--
     *
     * @param kId Node id
     * @param dId Document id
     */
    public void switchDocumentDownward(Long kId, Long dId) {
        documentRepository.switchDocumentDownward(kId, dId);
    }

    /**
     * 删除markdown Document的图数据
     *
     * @param document document
     */
    @Transactional
    public void deleteMarkdownDoc(Document document) {
        Long nodeId = documentRepository.getPartOfNodeId(document.getId());
        documentRepository.correctPartOfIndex(nodeId,
                documentRepository.getPartOfIndex(nodeId, document.getId()));
        documentRepository.delete(document);
    }

    /**
     * 删除指定id的Node的指定id的Document
     *
     * @param document Document object
     */
    public void delete(Document document) {
        if (document.getType().equals("markdown")) {
            deleteMarkdownDoc(document);
        } else {
            logger.error("unknown Document type internal: " + document.getType());
        }
    }
}
