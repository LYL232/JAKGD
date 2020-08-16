package pers.lyl232.jakgd.service.node;

import org.springframework.stereotype.Service;
import pers.lyl232.jakgd.entity.node.Knowledge;
import pers.lyl232.jakgd.exception.ObjectNotFoundException;
import pers.lyl232.jakgd.repository.node.KnowledgeRepository;
import pers.lyl232.jakgd.service.BaseService;

@Service
public class KnowledgeService extends BaseService {

    private final KnowledgeRepository knowledgeRepository;

    public KnowledgeService(KnowledgeRepository knowledgeRepository) {
        this.knowledgeRepository = knowledgeRepository;
    }

    /**
     * 获取Knowledge对象, 找不到则抛出异常
     *
     * @param id Knowledge id
     * @return 非空对象
     * @throws ObjectNotFoundException 找不到异常
     */
    public Knowledge getNotNull(Long id) throws ObjectNotFoundException {
        Knowledge knowledge = knowledgeRepository.get(id);
        if (knowledge == null) {
            throw new ObjectNotFoundException("Knowledge id: " + id);
        }
        return knowledge;
    }
}
