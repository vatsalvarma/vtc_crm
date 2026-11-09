package com.vtc.crm.repository;

import com.vtc.crm.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findAllByOrderByLastMessageAtDesc();

    List<Conversation> findByKindOrderByLastMessageAtDesc(String kind);

    @Query("SELECT c FROM Conversation c WHERE (c.directUser1Id = :u1 AND c.directUser2Id = :u2) OR (c.directUser1Id = :u2 AND c.directUser2Id = :u1)")
    Optional<Conversation> findDirectConversation(@Param("u1") Long u1, @Param("u2") Long u2);

    Optional<Conversation> findByRecordTypeAndRecordId(String recordType, Long recordId);
}
