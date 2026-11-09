package com.vtc.crm.controller;

import com.vtc.crm.entity.Conversation;
import com.vtc.crm.entity.Message;
import com.vtc.crm.entity.User;
import com.vtc.crm.repository.ConversationRepository;
import com.vtc.crm.repository.MessageRepository;
import com.vtc.crm.repository.UserRepository;
import com.vtc.crm.websocket.ChatWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class CommsController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public CommsController(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            ChatWebSocketHandler chatWebSocketHandler) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    // 1. Inbox: returns announcements, groups, and user direct chats
    @GetMapping("/comms/inbox")
    public Map<String, Object> getInbox(@AuthenticationPrincipal User currentUser) {
        Long myId = currentUser != null ? currentUser.getId() : 1L;
        List<Conversation> all = conversationRepository.findAllByOrderByLastMessageAtDesc();
        List<Map<String, Object>> convs = new ArrayList<>();

        for (Conversation c : all) {
            // Visibility check:
            // - announcements: visible to all
            // - groups: visible to all or members
            // - direct: visible only if currentUser is directUser1 or directUser2
            // - record: visible to all
            if ("direct".equalsIgnoreCase(c.getKind())) {
                if (!Objects.equals(c.getDirectUser1Id(), myId) && !Objects.equals(c.getDirectUser2Id(), myId)) {
                    continue;
                }
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("kind", c.getKind());

            // For direct chat, show the other person's name as the title
            if ("direct".equalsIgnoreCase(c.getKind())) {
                Long otherId = Objects.equals(c.getDirectUser1Id(), myId) ? c.getDirectUser2Id() : c.getDirectUser1Id();
                String title = c.getTitle();
                if (otherId != null) {
                    Optional<User> otherUser = userRepository.findById(otherId);
                    if (otherUser.isPresent()) {
                        title = otherUser.get().getFullName() + " · " + otherUser.get().getRole();
                    }
                }
                map.put("title", title);
            } else {
                map.put("title", c.getTitle());
            }

            if (c.getLastMessageBody() != null) {
                Map<String, Object> last = new HashMap<>();
                last.put("author", c.getLastMessageAuthor() != null ? c.getLastMessageAuthor() : "Staff");
                last.put("body", c.getLastMessageBody());
                map.put("last", last);
            } else {
                map.put("last", null);
            }

            map.put("unread", 0);
            convs.add(map);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("conversations", convs);
        return res;
    }

    // 2. Conversation detail with full message thread
    @GetMapping("/comms/conversations/{id}")
    public ResponseEntity<?> getConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        Optional<Conversation> opt = conversationRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Conversation c = opt.get();
        Long myId = currentUser != null ? currentUser.getId() : 1L;

        Map<String, Object> cMap = new HashMap<>();
        cMap.put("id", c.getId());
        cMap.put("kind", c.getKind());
        cMap.put("record_type", c.getRecordType());

        String displayTitle = c.getTitle();
        List<String> memberNames = new ArrayList<>();

        if ("direct".equalsIgnoreCase(c.getKind())) {
            Long otherId = Objects.equals(c.getDirectUser1Id(), myId) ? c.getDirectUser2Id() : c.getDirectUser1Id();
            if (otherId != null) {
                userRepository.findById(otherId).ifPresent(u -> {
                    memberNames.add(u.getFullName());
                });
            }
            if (currentUser != null) {
                memberNames.add(currentUser.getFullName());
            }
            if (!memberNames.isEmpty()) {
                displayTitle = String.join(" & ", memberNames);
            }
        } else if ("announcement".equalsIgnoreCase(c.getKind())) {
            memberNames.add("All factory & office staff");
        } else {
            // Group or record
            List<User> users = userRepository.findAll();
            for (User u : users) {
                if ("ACTIVE".equalsIgnoreCase(u.getStatus())) {
                    memberNames.add(u.getFullName());
                }
            }
        }
        cMap.put("title", displayTitle);

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(id);
        List<Map<String, Object>> msgList = new ArrayList<>();
        for (Message m : messages) {
            Map<String, Object> mm = formatMessage(m);
            msgList.add(mm);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("conversation", cMap);
        res.put("members", memberNames);
        res.put("notice", "Work messages can be read by the admin for oversight. Please keep them about work.");
        res.put("may_post", true);
        res.put("oversight", false);
        res.put("roles", Collections.emptyList());
        res.put("teams", Collections.emptyList());
        res.put("messages", msgList);

        return ResponseEntity.ok(res);
    }

    // 3. Post a message to a conversation (saves to MySQL and broadcasts live via WebSocket)
    @PostMapping("/comms/conversations/{id}/messages")
    public ResponseEntity<?> postMessage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        Optional<Conversation> opt = conversationRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Conversation c = opt.get();

        String body = req.get("body") != null ? req.get("body").toString().trim() : "";
        String voiceId = req.get("voice_file_id") != null ? req.get("voice_file_id").toString() : null;
        String audience = req.get("audience") != null ? req.get("audience").toString() : "all";

        if (body.isEmpty() && voiceId == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "EMPTY_MESSAGE"));
        }

        Long authorId = currentUser != null ? currentUser.getId() : 1L;
        String authorName = currentUser != null ? currentUser.getFullName() : "Owner";
        String authorRole = currentUser != null ? currentUser.getRole() : "Admin";

        Message msg = new Message(id, authorId, authorName, authorRole, body, audience);
        msg.setVoiceFileId(voiceId);
        Message savedMsg = messageRepository.save(msg);

        // Update conversation summary
        c.setLastMessageBody(body.isEmpty() ? "🎤 Voice note" : body);
        c.setLastMessageAuthor(authorName);
        c.setLastMessageAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(c);

        Map<String, Object> formattedMsg = formatMessage(savedMsg);

        // Broadcast to all connected clients via WebSocket
        chatWebSocketHandler.broadcastNewMessage(id, formattedMsg);

        return ResponseEntity.ok(formattedMsg);
    }

    // 4. Contacts list for "+ New chat" (includes all active users: Admin, Owner, Manager, Staff)
    @GetMapping("/comms/contacts")
    public List<Map<String, Object>> getContacts(@AuthenticationPrincipal User currentUser) {
        Long myId = currentUser != null ? currentUser.getId() : null;
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> contacts = new ArrayList<>();

        for (User u : users) {
            if ("ACTIVE".equalsIgnoreCase(u.getStatus())) {
                // Don't direct-chat with yourself
                if (myId != null && Objects.equals(myId, u.getId())) {
                    continue;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("id", u.getId());
                m.put("username", u.getUsername());
                m.put("full_name", u.getFullName());
                m.put("role_name", u.getRole());
                contacts.add(m);
            }
        }
        return contacts;
    }

    // 5. Open or create direct 1-on-1 chat
    @PostMapping("/comms/direct")
    public ResponseEntity<?> openDirectChat(
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        if (!req.containsKey("user_id") || req.get("user_id") == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "USER_ID_REQUIRED"));
        }
        Long targetId = Long.valueOf(req.get("user_id").toString());
        Long myId = currentUser != null ? currentUser.getId() : 1L;

        Optional<User> targetUserOpt = userRepository.findById(targetId);
        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "USER_NOT_FOUND"));
        }
        User targetUser = targetUserOpt.get();

        Long u1 = Math.min(myId, targetId);
        Long u2 = Math.max(myId, targetId);

        Optional<Conversation> existing = conversationRepository.findDirectConversation(u1, u2);
        if (existing.isPresent()) {
            return ResponseEntity.ok(Collections.singletonMap("id", existing.get().getId()));
        }

        String title = targetUser.getFullName();
        Conversation conv = new Conversation(title, "direct", myId + "," + targetId);
        conv.setDirectUser1Id(u1);
        conv.setDirectUser2Id(u2);
        Conversation saved = conversationRepository.save(conv);

        return ResponseEntity.ok(Collections.singletonMap("id", saved.getId()));
    }

    // 6. Record discussion thread (e.g. task, roast, order)
    @GetMapping({"/comms/record/{type}/{id}", "/comms/record/{type}/{id}"})
    @PostMapping({"/comms/record/{type}/{id}", "/comms/record/{type}/{id}"})
    public ResponseEntity<?> getRecordThread(
            @PathVariable String type,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        Optional<Conversation> existing = conversationRepository.findByRecordTypeAndRecordId(type, id);
        if (existing.isPresent()) {
            return ResponseEntity.ok(Collections.singletonMap("id", existing.get().getId()));
        }

        String title = type.toUpperCase() + " #" + id;
        Conversation conv = new Conversation(title, "record", "*");
        conv.setRecordType(type);
        conv.setRecordId(id);
        Conversation saved = conversationRepository.save(conv);

        return ResponseEntity.ok(Collections.singletonMap("id", saved.getId()));
    }

    // 7. Oversight list for admin
    @GetMapping("/comms/oversight")
    public List<Map<String, Object>> getOversight() {
        List<Conversation> all = conversationRepository.findAllByOrderByLastMessageAtDesc();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Conversation c : all) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("title", c.getTitle());
            m.put("kind", c.getKind());
            m.put("messages", messageRepository.countByConversationId(c.getId()));
            m.put("last_at", c.getLastMessageAt() != null ? c.getLastMessageAt().toString() : c.getCreatedAt().toString());
            list.add(m);
        }
        return list;
    }

    // 8. Badge counts
    @GetMapping("/comms/badge")
    public Map<String, Object> getCommsBadge() {
        Map<String, Object> map = new HashMap<>();
        map.put("messages", 0);
        map.put("alerts", 0);
        return map;
    }

    // 9. Alerts
    @GetMapping("/alerts")
    public List<Map<String, Object>> getAlerts() {
        return Collections.emptyList();
    }

    @PostMapping("/alerts/read")
    public Map<String, Object> markAlertsRead() {
        return Collections.singletonMap("ok", true);
    }

    // 10. Voice upload mock
    @PostMapping("/comms/voice")
    public Map<String, Object> uploadVoice(@RequestBody Map<String, Object> req) {
        return Collections.singletonMap("id", UUID.randomUUID().toString());
    }

    private Map<String, Object> formatMessage(Message m) {
        Map<String, Object> mm = new HashMap<>();
        mm.put("id", m.getId());
        mm.put("conversation_id", m.getConversationId());
        mm.put("author_id", m.getAuthorId());
        mm.put("author", m.getAuthor());
        mm.put("author_role", m.getAuthorRole());
        mm.put("body", m.getBody());
        mm.put("audience", m.getAudience());
        mm.put("voice_file_id", m.getVoiceFileId());
        mm.put("created_at", m.getCreatedAt() != null ? m.getCreatedAt().toString() : LocalDateTime.now().toString());
        return mm;
    }
}
