package com.bruhmosuki.dhoomaKedu.dao;

import com.bruhmosuki.dhoomaKedu.entity.chatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface chatMessageRepository extends JpaRepository<chatMessage, Long> {

    @Query("SELECT m FROM chatMessage m WHERE " +
           "((m.senderIp = :ip1 AND m.recipientIp = :ip2) OR (m.senderIp = :ip2 AND m.recipientIp = :ip1)) " +
           "AND m.groupName IS NULL " +
           "ORDER BY m.timestamp ASC")
    List<chatMessage> findChatHistory(@Param("ip1") String ip1, @Param("ip2") String ip2);

    @Query("SELECT m FROM chatMessage m WHERE m.groupName = :groupName ORDER BY m.timestamp ASC")
    List<chatMessage> findGroupChatHistory(@Param("groupName") String groupName);
    
    @Query(value = "SELECT * FROM chat_message WHERE " +
           "((sender_ip = :ip1 AND recipient_ip = :ip2) OR (sender_ip = :ip2 AND recipient_ip = :ip1)) " +
           "AND group_name IS NULL " +
           "ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    chatMessage findLatestMessage(@Param("ip1") String ip1, @Param("ip2") String ip2);

    @Query(value = "SELECT * FROM chat_message WHERE group_name = :groupName ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    chatMessage findLatestGroupMessage(@Param("groupName") String groupName);
}
