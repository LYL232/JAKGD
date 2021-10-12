package pers.lyl232.jakgd.service.relationship;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.repository.relationship.ReferRelationshipRepository;
import pers.lyl232.jakgd.service.BaseService;
import pers.lyl232.jakgd.util.BriefJSONResponse;

@Service
public class ReferRelationshipService extends BaseService {

    private final ReferRelationshipRepository referRelationshipRepository;

    public ReferRelationshipService(ReferRelationshipRepository referRelationshipRepository) {
        this.referRelationshipRepository = referRelationshipRepository;
    }

    /**
     * 添加两个节点之间的__refer关系
     *
     * @param id      startNode id
     * @param referId endNode id
     */
    @Transactional
    public void create(Long id, Long referId) throws ExceptionWithBriefJSONResponse {
        if (referRelationshipRepository.queryIfHasReferred(id, referId) > 0) {
            throw new ExceptionWithBriefJSONResponse(BriefJSONResponse.Code.DUPLICATE_REFER);
        }
        if (referRelationshipRepository.queryIfHasReferred(referId, id) > 0) {
            throw new ExceptionWithBriefJSONResponse(BriefJSONResponse.Code.SELF_REFER);
        }
        referRelationshipRepository.deleteDuplicateReferRelationships(id, referId);
        referRelationshipRepository.deleteRefererDuplicateReferRelationships(id, referId);
        referRelationshipRepository.create(id, referId, getFormatNowDateStringBySecond());
    }

}
