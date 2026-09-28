package com.bruhmosuki.dhoomaKedu.dao;

import com.bruhmosuki.dhoomaKedu.entity.chatMessageConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface chatMessageConfirmationRepository extends JpaRepository<chatMessageConfirmation, Long> {
    List<chatMessageConfirmation> findByMessageId(Long messageId);
    List<chatMessageConfirmation> findByMessageIdIn(List<Long> messageIds);
    Optional<chatMessageConfirmation> findByMessageIdAndRecipientIp(Long messageId, String recipientIp);
}
