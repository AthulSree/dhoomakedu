package com.bruhmosuki.dhoomaKedu.Controller;

import org.springframework.ui.Model;
import com.bruhmosuki.dhoomaKedu.service.sshService;
import com.bruhmosuki.dhoomaKedu.config.WebMvcConfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.bruhmosuki.dhoomaKedu.entity.groupHost;
import com.bruhmosuki.dhoomaKedu.entity.employee;
import com.bruhmosuki.dhoomaKedu.service.groupHostService;
import com.bruhmosuki.dhoomaKedu.service.commonServices;
import com.bruhmosuki.dhoomaKedu.service.employeeService;
import com.bruhmosuki.dhoomaKedu.dao.chatMessageRepository;
import com.bruhmosuki.dhoomaKedu.dao.chatMessageConfirmationRepository;
import com.bruhmosuki.dhoomaKedu.entity.chatMessage;
import com.bruhmosuki.dhoomaKedu.entity.chatMessageConfirmation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDateTime;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.util.Enumeration;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/message")
public class MessageController {

    private final sshService sshService;
    private final groupHostService groupHostService;
    private final employeeService employeeService;
    private final commonServices theCommonService;
    private final chatMessageRepository chatMessageRepository;
    private final chatMessageConfirmationRepository confirmationRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Value("${developer.mock.ip:}")
    private String developerMockIp;

    @Value("${server.callback.url:}")
    private String serverCallbackUrl;

    private String getLocalLanIp() {
        if (developerMockIp != null && !developerMockIp.trim().isEmpty()) {
            return developerMockIp.trim();
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress() && !addr.isLinkLocalAddress()) {
                        String ip = addr.getHostAddress();
                        if (ip != null && !ip.isEmpty() && !ip.startsWith("127.")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return "10.162.6.11";
    }

    private String resolveBaseUrl(HttpServletRequest request) {
        if (serverCallbackUrl != null && !serverCallbackUrl.trim().isEmpty()) {
            return serverCallbackUrl.trim().replaceAll("/+$", "");
        }

        String scheme = request.getScheme();
        if (request.getHeader("X-Forwarded-Proto") != null && !request.getHeader("X-Forwarded-Proto").isEmpty()) {
            scheme = request.getHeader("X-Forwarded-Proto").split(",")[0].trim();
        }

        String hostHeader = request.getHeader("X-Forwarded-Host");
        if (hostHeader == null || hostHeader.isEmpty()) {
            hostHeader = request.getHeader("Host");
        }

        String hostPart = null;
        if (hostHeader != null && !hostHeader.trim().isEmpty()) {
            hostPart = hostHeader.trim().split(",")[0].trim();
        } else {
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String portPart = (serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort;
            hostPart = serverName + portPart;
        }

        // When sending to remote machines (like Akhil's), "localhost" or "127.0.0.1" cannot be used
        // because curl on their PC would try to connect to itself! Substitute the real LAN IP.
        if (hostPart.startsWith("localhost") || hostPart.startsWith("127.0.0.1") || hostPart.startsWith("0:0:0:0")) {
            String port = hostPart.contains(":") ? hostPart.substring(hostPart.indexOf(":")) : ":7003";
            String fallbackIp = getLocalLanIp();
            hostPart = fallbackIp + port;
        }

        return scheme + "://" + hostPart;
    }

    private void saveScaledPreview(File sourceFile, File targetFile, int maxWidth, int maxHeight) {
        try {
            BufferedImage original = ImageIO.read(sourceFile);
            if (original == null) {
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return;
            }

            int origWidth = original.getWidth();
            int origHeight = original.getHeight();

            if (origWidth <= maxWidth && origHeight <= maxHeight) {
                ImageIO.write(original, "png", targetFile);
                return;
            }

            double scale = Math.min((double) maxWidth / origWidth, (double) maxHeight / origHeight);
            int newWidth = Math.max(1, (int) Math.round(origWidth * scale));
            int newHeight = Math.max(1, (int) Math.round(origHeight * scale));

            int imageType = (original.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
            BufferedImage resized = new BufferedImage(newWidth, newHeight, imageType);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(original, 0, 0, newWidth, newHeight, null);
            g.dispose();

            ImageIO.write(resized, "png", targetFile);
        } catch (Exception e) {
            System.err.println("Failed to create scaled preview: " + e.getMessage());
            try {
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ignored) {}
        }
    }

    private String resolveCallbackUrl(HttpServletRequest request) {
        return resolveBaseUrl(request) + "/message/api/confirm";
    }

    public MessageController(sshService sshService, groupHostService groupHostService,
            employeeService employeeService, commonServices theCommonService,
            chatMessageRepository chatMessageRepository,
            chatMessageConfirmationRepository confirmationRepository) {
        this.sshService = sshService;
        this.groupHostService = groupHostService;
        this.employeeService = employeeService;
        this.theCommonService = theCommonService;
        this.chatMessageRepository = chatMessageRepository;
        this.confirmationRepository = confirmationRepository;
    }

    @GetMapping("/msgLander")
    public String msgLander(Model model, HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");

        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        } else {
            // In case of multiple proxies, the first IP is the client
            clientIp = clientIp.split(",")[0].trim();
        }
        if ("0:0:0:0:0:0:0:1".equals(clientIp) || "127.0.0.1".equals(clientIp)) {
            clientIp = getLocalLanIp();
        }

        employee sender = employeeService.findBySysIp(clientIp);
        String senderName = (sender != null) ? sender.getFirst_name() + " " + sender.getLast_name() : "Unknown Sender";

        List<groupHost> groupHosts = groupHostService.findAll();
        model.addAttribute("groupHosts", groupHosts);
        model.addAttribute("senderName", senderName);
        model.addAttribute("clientIp", clientIp);
        return "msgLander";
    }

    @PostMapping("/sendMsg")
    public String sendMsg(@RequestParam("hostId") List<Integer> hostId,
            @RequestParam("msgContainer") String message,
            @RequestParam(value = "msgType", defaultValue = "normal") String msgType,
            HttpServletRequest request) {

        String clientIp = request.getHeader("X-Forwarded-For");

        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        } else {
            // In case of multiple proxies, the first IP is the client
            clientIp = clientIp.split(",")[0].trim();
        }
        if ("0:0:0:0:0:0:0:1".equals(clientIp) || "127.0.0.1".equals(clientIp)) {
            clientIp = getLocalLanIp();
        }

        employee sender = employeeService.findBySysIp(clientIp);
        String senderName = (sender != null) ? sender.getFirst_name() + " " + sender.getLast_name() : "Unknown Sender";

        if (clientIp.equals("10.162.6.11")) {
            senderName = "Brahmo-Z";
        }

        // Append sender name to message
        String fullMessage = message + "\n\n\nRegards,\n" + senderName + "\n[Nēnu nīke Dūtanu]";
        LocalDateTime now = LocalDateTime.now();

        for (Integer hostId1 : hostId) {
            groupHost groupHost = groupHostService.findById(hostId1);
            if (groupHost == null) continue;
            System.out.println("hostIds: " + groupHost.getUserName());
            String hostIp = groupHost.getHost();
            String hostUserName = groupHost.getUserName();
            String hostPassword = groupHost.getPassword();

            chatMessage chatMsg = new chatMessage(
                clientIp,
                senderName,
                hostIp,
                hostUserName,
                message,
                msgType,
                null,
                now
            );
            chatMessage savedMsg = chatMessageRepository.save(chatMsg);

            if ("confirm".equals(msgType)) {
                chatMessageConfirmation conf = new chatMessageConfirmation(
                    savedMsg.getId(),
                    hostIp,
                    hostUserName,
                    "PENDING",
                    null
                );
                confirmationRepository.save(conf);
            }

            // theCommonService.showError("Sending message to " + hostIp + " as " + hostUserName);

            String command = "";
            
            if ("docsquad".equals(msgType)) {
                //------ Docsquad message ------ 
                String url = "http://10.162.6.180:8015/docsquad/"; // from request/form
                String text = fullMessage + "\n\nLink: " + url;
                String dialogFlow = "if zenity --question " +
                        "--title='Doothan Incoming...' " +
                        "--ok-label='Open the byte-WhaSSH 2.0 / DocsQuad' --cancel-label='Close' " +
                        "--text=" + shellQuote(text) + "; then " +
                        "xdg-open " + shellQuote(url) + " >/dev/null 2>&1; " +
                        "fi";

                command = "export DISPLAY=:0; " +
                        "nohup bash -lc " + shellQuote(dialogFlow) + " >/dev/null 2>&1 &";
            } else if ("leave".equals(msgType)) {
                //------ Add Leave message ------ 
                String url = "http://10.162.6.188:7003/leaves/manage"; // from request/form
                String text = fullMessage + "\n\nLink: " + url;
                String dialogFlow = "if zenity --question " +
                        "--title='Doothan Incoming...' " +
                        "--ok-label='Open Leave Portal' --cancel-label='Close' " +
                        "--text=" + shellQuote(text) + "; then " +
                        "xdg-open " + shellQuote(url) + " >/dev/null 2>&1; " +
                        "fi";

                command = "export DISPLAY=:0; " +
                        "nohup bash -lc " + shellQuote(dialogFlow) + " >/dev/null 2>&1 &";
            } else if ("confirm".equals(msgType)) {
                //------ Confirmation message (OK / Cancel / Custom Message) ------ 
                String callbackUrl = resolveCallbackUrl(request);
                String dialogFlow = buildConfirmDialogFlow(fullMessage, callbackUrl, savedMsg.getId(), hostIp);
                command = "export DISPLAY=:0; " +
                        "nohup bash -lc " + shellQuote(dialogFlow) + " >/dev/null 2>&1 &";
            } else {
                //------ Normal message ------ 
                command = "export DISPLAY=:0; " +
                        "nohup zenity --info " +
                        "--title='\uD83D\uDD4A\uFE0F Doothan Incoming...' " +
                        "--text=\"" + fullMessage + "\" " + // wrap message in quotes
                        " > /dev/null 2>&1 &";
            }

            sshService.sendCommand(hostIp, hostUserName, hostPassword, command);
            // results.append("Host: ").append(host.getHost()).append(" ->
            // ").append(res).append("<br>");

        }

        return "redirect:/message/msgLander";

        // List<HostInfo> groupHosts = Arrays.asList(
        // new HostInfo("10.162.6.11", "athul", "nic*123"),
        // new HostInfo("10.162.6.102", "gokul", "password"),
        // new HostInfo("10.162.6.236", "akhil", "nic*123"),
        // new HostInfo("10.162.6.167", "simi", "nic*123"),
        // new HostInfo("10.162.6.190", "nisanth", "nic*123")
        //// new HostInfo("10.162.6.180", "sooraj", "sooraj@123")
        //
        // );

        //
        // String command = "export DISPLAY=:0; " +
        // "nohup zenity --list " +
        // "--title='Notification' " +
        // "--text='Chaya Kudikkaam ?' " +
        // "--column='Options' 'Pokaam' 'Enthayalum Pokaam' 'Pinnenthaa' > /dev/null
        // 2>&1 &";

        // StringBuilder results = new StringBuilder();
        //
        // for (HostInfo host : groupHosts) {
        // String cmd = String.format(command, host.getUsername());
        // String res = sshService.sendCommand(host.getHost(), host.getUsername(),
        // host.getPassword(), cmd);
        // results.append("Host: ").append(host.getHost()).append(" ->
        // ").append(res).append("<br>");
        // }
        //
        // return "dashboard";
    }

    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        } else {
            clientIp = clientIp.split(",")[0].trim();
        }
        if ("0:0:0:0:0:0:0:1".equals(clientIp) || "127.0.0.1".equals(clientIp)) {
            clientIp = getLocalLanIp();
        }
        return clientIp;
    }

    private String getSenderName(String clientIp) {
        if ("10.162.6.11".equals(clientIp)) {
            return "Brahmo-Z";
        }
        employee sender = employeeService.findBySysIp(clientIp);
        return (sender != null) ? sender.getFirst_name() + " " + sender.getLast_name() : "Unknown (" + clientIp + ")";
    }

    @GetMapping("/api/contacts")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getContacts(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        List<Map<String, Object>> contactList = new ArrayList<>();

        // Add virtual groups
        String[] groupNames = {"MPR", "Spill Tea", "Crex"};
        String[] groupIds = {"group_mpr", "group_tea", "group_crex"};
        String[][] groupMembers = {
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "12", "13"},
            {"1", "2", "3", "4", "9"},
            {"3"}
        };

        for (int i = 0; i < groupNames.length; i++) {
            Map<String, Object> gMap = new HashMap<>();
            gMap.put("id", groupIds[i]);
            gMap.put("name", groupNames[i]);
            gMap.put("isGroup", true);
            gMap.put("members", groupMembers[i]);
            
            chatMessage latest = chatMessageRepository.findLatestGroupMessage(groupNames[i]);
            if (latest != null) {
                gMap.put("lastMessage", latest.getMessageContent());
                gMap.put("lastMessageTime", latest.getTimestamp().toString());
            } else {
                gMap.put("lastMessage", "No messages yet");
                gMap.put("lastMessageTime", null);
            }
            contactList.add(gMap);
        }

        // Add individual hosts
        List<groupHost> hosts = groupHostService.findAll();
        for (groupHost host : hosts) {
            Map<String, Object> hMap = new HashMap<>();
            hMap.put("id", host.getId().toString());
            hMap.put("name", host.getUserName() + " (" + host.getHost() + ")");
            hMap.put("ip", host.getHost());
            hMap.put("isGroup", false);
            hMap.put("isActive", host.getActive());

            chatMessage latest = chatMessageRepository.findLatestMessage(clientIp, host.getHost());
            if (latest != null) {
                hMap.put("lastMessage", latest.getMessageContent());
                hMap.put("lastMessageTime", latest.getTimestamp().toString());
            } else {
                hMap.put("lastMessage", "No messages yet");
                hMap.put("lastMessageTime", null);
            }
            contactList.add(hMap);
        }

        return ResponseEntity.ok(contactList);
    }

    @GetMapping("/api/history")
    @ResponseBody
    public ResponseEntity<List<chatMessage>> getHistory(
            @RequestParam("target") String target,
            @RequestParam("isGroup") boolean isGroup,
            HttpServletRequest request) {
        
        String clientIp = getClientIp(request);
        List<chatMessage> history;

        if (isGroup) {
            history = chatMessageRepository.findGroupChatHistory(target);
        } else {
            history = chatMessageRepository.findChatHistory(clientIp, target);
        }

        if (history != null && !history.isEmpty()) {
            List<Long> msgIds = history.stream().map(chatMessage::getId).toList();
            List<chatMessageConfirmation> allConfs = confirmationRepository.findByMessageIdIn(msgIds);
            Map<Long, List<chatMessageConfirmation>> confMap = allConfs.stream()
                .collect(Collectors.groupingBy(chatMessageConfirmation::getMessageId));

            for (chatMessage msg : history) {
                msg.setConfirmations(confMap.getOrDefault(msg.getId(), Collections.emptyList()));
            }
        }

        return ResponseEntity.ok(history);
    }

    @PostMapping("/api/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMsgApi(
            @RequestParam(value = "hostId[]", required = false) List<Integer> hostIds,
            @RequestParam(value = "groupName", required = false) String groupName,
            @RequestParam(value = "message", required = false, defaultValue = "") String message,
            @RequestParam(value = "msgType", defaultValue = "normal") String msgType,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            HttpServletRequest request) {

        String clientIp = getClientIp(request);
        String senderName = getSenderName(clientIp);
        Map<String, Object> response = new HashMap<>();

        boolean hasText = message != null && !message.trim().isEmpty();
        boolean hasImage = imageFile != null && !imageFile.isEmpty();

        if (!hasText && !hasImage) {
            response.put("success", false);
            response.put("error", "Message content or image attachment is required");
            return ResponseEntity.badRequest().body(response);
        }

        List<groupHost> recipients = new ArrayList<>();
        if (groupName != null && !groupName.isEmpty()) {
            String[] memberIds = {};
            if ("MPR".equalsIgnoreCase(groupName)) {
                memberIds = new String[]{"1", "2", "3", "5", "6", "7", "8", "9", "10", "12"};
            } else if ("Spill Tea".equalsIgnoreCase(groupName) || "tea".equalsIgnoreCase(groupName)) {
                groupName = "Spill Tea";
                memberIds = new String[]{"1", "2", "3", "4", "9"};
            } else if ("Crex".equalsIgnoreCase(groupName)) {
                memberIds = new String[]{"3"};
            }
            
            for (String mId : memberIds) {
                try {
                    groupHost gh = groupHostService.findById(Integer.parseInt(mId));
                    if (gh != null) {
                        recipients.add(gh);
                    }
                } catch (Exception ignored) {}
            }
        } else if (hostIds != null && !hostIds.isEmpty()) {
            for (Integer hostId : hostIds) {
                groupHost gh = groupHostService.findById(hostId);
                if (gh != null) {
                    recipients.add(gh);
                }
            }
        } else {
            response.put("success", false);
            response.put("error", "No recipients specified");
            return ResponseEntity.badRequest().body(response);
        }

        if (recipients.isEmpty()) {
            response.put("success", false);
            response.put("error", "Recipients list is empty");
            return ResponseEntity.badRequest().body(response);
        }

        String attachmentUrl = null;
        String previewAttachmentUrl = null;
        if (hasImage) {
            try {
                String originalFilename = imageFile.getOriginalFilename();
                String ext = ".png";
                if (originalFilename != null && originalFilename.contains(".")) {
                    String extractedExt = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
                    if (extractedExt.matches("^\\.(png|jpg|jpeg|gif|webp|bmp|svg)$")) {
                        ext = extractedExt;
                    }
                }
                String subDir = "chat-images";
                File uploadFolder = new File(WebMvcConfig.getUploadBaseDirectory(), subDir);
                if (!uploadFolder.exists()) {
                    uploadFolder.mkdirs();
                }

                String baseUuid = UUID.randomUUID().toString();
                String filename = baseUuid + ext;
                File targetFile = new File(uploadFolder, filename);
                Files.copy(imageFile.getInputStream(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                attachmentUrl = "/message/uploads/" + subDir + "/" + filename;

                // Create a scaled preview for desktop notification popups (max 460x320 px)
                String previewFilename = "prev_" + baseUuid + ".png";
                File previewFile = new File(uploadFolder, previewFilename);
                saveScaledPreview(targetFile, previewFile, 460, 320);
                previewAttachmentUrl = "/message/uploads/" + subDir + "/" + previewFilename;
            } catch (Exception e) {
                System.err.println("Failed to save chat image: " + e.getMessage());
                response.put("success", false);
                response.put("error", "Failed to upload image: " + e.getMessage());
                return ResponseEntity.internalServerError().body(response);
            }
        }

        String contentText = hasText ? message.trim() : "";
        String fullMessage = (contentText.isEmpty() ? "" : contentText + "\n\n\n") + "Regards,\n" + senderName + "\n[Nēnu nīke Dūtanu]";

        Map<String, Long> recipientMsgIdMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        if (groupName != null && !groupName.isEmpty()) {
            chatMessage chatMsg = new chatMessage(
                clientIp,
                senderName,
                null,
                groupName + " Broadcast",
                contentText,
                msgType,
                groupName,
                attachmentUrl,
                now
            );
            chatMessage savedMsg = chatMessageRepository.save(chatMsg);

            for (groupHost gh : recipients) {
                recipientMsgIdMap.put(gh.getHost(), savedMsg.getId());
                if ("confirm".equals(msgType)) {
                    chatMessageConfirmation conf = new chatMessageConfirmation(
                        savedMsg.getId(),
                        gh.getHost(),
                        gh.getUserName(),
                        "PENDING",
                        null
                    );
                    confirmationRepository.save(conf);
                }
            }
        } else {
            for (groupHost gh : recipients) {
                chatMessage chatMsg = new chatMessage(
                    clientIp,
                    senderName,
                    gh.getHost(),
                    gh.getUserName(),
                    contentText,
                    msgType,
                    null,
                    attachmentUrl,
                    now
                );
                chatMessage savedMsg = chatMessageRepository.save(chatMsg);
                recipientMsgIdMap.put(gh.getHost(), savedMsg.getId());

                if ("confirm".equals(msgType)) {
                    chatMessageConfirmation conf = new chatMessageConfirmation(
                        savedMsg.getId(),
                        gh.getHost(),
                        gh.getUserName(),
                        "PENDING",
                        null
                    );
                    confirmationRepository.save(conf);
                }
            }
        }

        String baseUrl = resolveBaseUrl(request);
        String callbackUrl = baseUrl + "/message/api/confirm";
        final String finalAttachmentUrl = attachmentUrl;
        final String finalPreviewUrl = (previewAttachmentUrl != null) ? previewAttachmentUrl : attachmentUrl;

        for (groupHost gh : recipients) {
            String hostIp = gh.getHost();
            String hostUserName = gh.getUserName();
            String hostPassword = gh.getPassword();
            Long targetMsgId = recipientMsgIdMap.get(hostIp);

            executorService.submit(() -> {
                String command = "";
                if ("docsquad".equals(msgType)) {
                    String url = "http://10.162.6.180:8015/docsquad/";
                    String text = fullMessage + "\n\nLink: " + url;
                    if (finalAttachmentUrl != null && !finalAttachmentUrl.isEmpty()) {
                        String fullImageUrl = baseUrl + finalAttachmentUrl;
                        String previewImageUrl = baseUrl + finalPreviewUrl;
                        String dialogFlow = "IMG_FILE=\"/tmp/doothan_$(date +%s%N).png\"; " +
                                "curl -s -f --connect-timeout 5 --max-time 15 " + shellQuote(previewImageUrl) + " -o \"$IMG_FILE\"; " +
                                "if which yad >/dev/null 2>&1 && [ -s \"$IMG_FILE\" ]; then " +
                                "  yad --title='Doothan: DocsQuad' --image=\"$IMG_FILE\" --image-on-top --center " +
                                "      --text=" + shellQuote(text) + " " +
                                "      --button='Open DocsQuad:0' --button='🖼️ Full Size:2' --button='Close:1'; " +
                                "  RES=$?; " +
                                "  if [ $RES -eq 0 ]; then xdg-open " + shellQuote(url) + " >/dev/null 2>&1; fi; " +
                                "  if [ $RES -eq 2 ]; then xdg-open " + shellQuote(fullImageUrl) + " >/dev/null 2>&1; fi; " +
                                "else " +
                                "  if zenity --question --title='Doothan Incoming...' --ok-label='Open the byte-WhaSSH 2.0 / DocsQuad' --cancel-label='Close' --text=" + shellQuote(text) + "; then " +
                                "    xdg-open " + shellQuote(url) + " >/dev/null 2>&1; " +
                                "  fi; " +
                                "fi; " +
                                "(sleep 120 && rm -f \"$IMG_FILE\") >/dev/null 2>&1 &";
                        command = "export DISPLAY=:0; nohup bash -lc " + shellQuote(dialogFlow) + " >/dev/null 2>&1 &";
                    } else {
                        String dialogFlow = "if zenity --question --title='Doothan Incoming...' --ok-label='Open the byte-WhaSSH 2.0 / DocsQuad' --cancel-label='Close' --text=" + shellQuote(text) + "; then xdg-open " + shellQuote(url) + " >/dev/null 2>&1; fi";
                        command = "export DISPLAY=:0; nohup bash -lc " + shellQuote(dialogFlow) + " >/dev/null 2>&1 &";
                    }
                } else if ("leave".equals(msgType)) {
                    String url = "http://10.162.6.188:7003/leaves/manage";
                    String text = fullMessage + "\n\nLink: " + url;
                    if (finalAttachmentUrl != null && !finalAttachmentUrl.isEmpty()) {
                        String fullImageUrl = baseUrl + finalAttachmentUrl;
                        String previewImageUrl = baseUrl + finalPreviewUrl;
                        String dialogFlow = "IMG_FILE=\"/tmp/doothan_$(date +%s%N).png\"; " +
                                "curl -s -f --connect-timeout 5 --max-time 15 " + shellQuote(previewImageUrl) + " -o \"$IMG_FILE\"; " +
                                "if which yad >/dev/null 2>&1 && [ -s \"$IMG_FILE\" ]; then " +
                                "  yad --title='Doothan: Leave Portal' --image=\"$IMG_FILE\" --image-on-top --center " +
                                "      --text=" + shellQuote(text) + " " +
                                "      --button='Open Leave Portal:0' --button='🖼️ Full Size:2' --button='Close:1'; " +
                                "  RES=$?; " +
                                "  if [ $RES -eq 0 ]; then xdg-open " + shellQuote(url) + " >/dev/null 2>&1; fi; " +
                                "  if [ $RES -eq 2 ]; then xdg-open " + shellQuote(fullImageUrl) + " >/dev/null 2>&1; fi; " +
                                "else " +
                                "  if zenity --question --title='Doothan Incoming...' --ok-label='Open Leave Portal' --cancel-label='Close' --text=" + shellQuote(text) + "; then " +
                                "    xdg-open " + shellQuote(url) + " >/dev/null 2>&1; " +
                                "  fi; " +
                                "fi; " +
                                "(sleep 120 && rm -f \"$IMG_FILE\") >/dev/null 2>&1 &";
                        command = "export DISPLAY=:0; nohup bash -lc " + shellQuote(dialogFlow) + " >/dev/null 2>&1 &";
                    } else {
                        String dialogFlow = "if zenity --question --title='Doothan Incoming...' --ok-label='Open Leave Portal' --cancel-label='Close' --text=" + shellQuote(text) + "; then xdg-open " + shellQuote(url) + " >/dev/null 2>&1; fi";
                        command = "export DISPLAY=:0; nohup bash -lc " + shellQuote(dialogFlow) + " >/dev/null 2>&1 &";
                    }
                } else if ("confirm".equals(msgType)) {
                    String fullImageUrl = (finalAttachmentUrl != null && !finalAttachmentUrl.isEmpty()) ? (baseUrl + finalAttachmentUrl) : null;
                    String previewImageUrl = (finalPreviewUrl != null && !finalPreviewUrl.isEmpty()) ? (baseUrl + finalPreviewUrl) : null;
                    String dialogFlow = buildConfirmDialogFlow(fullMessage, callbackUrl, targetMsgId, hostIp, previewImageUrl, fullImageUrl);
                    command = "export DISPLAY=:0; nohup bash -lc " + shellQuote(dialogFlow) + " >/dev/null 2>&1 &";
                } else if (finalAttachmentUrl != null && !finalAttachmentUrl.isEmpty()) {
                    String fullImageUrl = baseUrl + finalAttachmentUrl;
                    String previewImageUrl = baseUrl + finalPreviewUrl;
                    String dialogFlow = "IMG_FILE=\"/tmp/doothan_$(date +%s%N).png\"; " +
                            "curl -s -f --connect-timeout 5 --max-time 15 " + shellQuote(previewImageUrl) + " -o \"$IMG_FILE\"; " +
                            "if which yad >/dev/null 2>&1 && [ -s \"$IMG_FILE\" ]; then " +
                            "  yad --title='🕊️ Doothan: Image Incoming...' " +
                            "      --image=\"$IMG_FILE\" --image-on-top --center " +
                            "      --text=" + shellQuote(fullMessage) + " " +
                            "      --button='Close:0' --button='🖼️ Full Size:2'; " +
                            "  if [ $? -eq 2 ]; then xdg-open " + shellQuote(fullImageUrl) + " >/dev/null 2>&1; fi; " +
                            "else " +
                            "  if zenity --question --title='🕊️ Doothan: Image Incoming...' " +
                            "            --ok-label='🖼️ View Image' --cancel-label='Close' " +
                            "            --text=" + shellQuote(fullMessage + "\n\n📷 [Image Attached - Click 'View Image' to open]") + "; then " +
                            "    xdg-open " + shellQuote(fullImageUrl) + " >/dev/null 2>&1; " +
                            "  fi; " +
                            "fi; " +
                            "(sleep 120 && rm -f \"$IMG_FILE\") >/dev/null 2>&1 &";
                    command = "export DISPLAY=:0; nohup bash -lc " + shellQuote(dialogFlow) + " >/dev/null 2>&1 &";
                } else {
                    command = "export DISPLAY=:0; nohup zenity --info --title='\uD83D\uDD4A\uFE0F Doothan Incoming...' --text=\"" + fullMessage + "\" > /dev/null 2>&1 &";
                }

                sshService.sendCommand(hostIp, hostUserName, hostPassword, command);
            });
        }

        response.put("success", true);
        response.put("attachmentUrl", attachmentUrl);
        response.put("timestamp", now.toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recordConfirmation(
            @RequestParam("messageId") Long messageId,
            @RequestParam(value = "recipientIp", required = false) String recipientIp,
            @RequestParam("status") String status,
            @RequestParam(value = "replyText", required = false) String replyText,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        if (recipientIp == null || recipientIp.isEmpty()) {
            recipientIp = getClientIp(request);
        }

        String normalizedStatus = "OK";
        if ("CANCEL".equalsIgnoreCase(status)) {
            normalizedStatus = "CANCEL";
        } else if ("CUSTOM".equalsIgnoreCase(status)) {
            normalizedStatus = "CUSTOM";
        }

        List<chatMessageConfirmation> confs = confirmationRepository.findByMessageId(messageId);
        boolean updated = false;

        for (chatMessageConfirmation conf : confs) {
            if (recipientIp.equals(conf.getRecipientIp()) || confs.size() == 1) {
                conf.setStatus(normalizedStatus);
                conf.setReplyText(replyText);
                conf.setRespondedAt(LocalDateTime.now());
                confirmationRepository.save(conf);
                updated = true;
                break;
            }
        }

        if (!updated && !confs.isEmpty()) {
            for (chatMessageConfirmation conf : confs) {
                if ("PENDING".equals(conf.getStatus())) {
                    conf.setStatus(normalizedStatus);
                    conf.setReplyText(replyText);
                    conf.setRespondedAt(LocalDateTime.now());
                    confirmationRepository.save(conf);
                    updated = true;
                    break;
                }
            }
        }

        response.put("success", updated);
        response.put("status", normalizedStatus);
        response.put("replyText", replyText);
        return ResponseEntity.ok(response);
    }

    private String buildConfirmDialogFlow(String fullMessage, String callbackUrl, Long messageId, String hostIp) {
        return buildConfirmDialogFlow(fullMessage, callbackUrl, messageId, hostIp, null, null);
    }

    private String buildConfirmDialogFlow(String fullMessage, String callbackUrl, Long messageId, String hostIp, String previewImageUrl, String fullImageUrl) {
        if (previewImageUrl != null && !previewImageUrl.isEmpty()) {
            return "IMG_FILE=\"/tmp/doothan_$(date +%s%N).png\"; " +
                    "curl -s -f --connect-timeout 5 --max-time 15 " + shellQuote(previewImageUrl) + " -o \"$IMG_FILE\"; " +
                    "if which yad >/dev/null 2>&1 && [ -s \"$IMG_FILE\" ]; then " +
                    "  while true; do " +
                    "    yad --title='🕊️ Doothan Confirmation' " +
                    "        --image=\"$IMG_FILE\" --image-on-top --center " +
                    "        --text=" + shellQuote(fullMessage) + " " +
                    "        --button='🖼️ Full Size:3' --button='OK:0' --button='Cancel:1' --button='Custom Message:2'; " +
                    "    CODE=$?; " +
                    "    if [ $CODE -eq 3 ]; then xdg-open " + shellQuote(fullImageUrl) + " >/dev/null 2>&1; continue; fi; " +
                    "    break; " +
                    "  done; " +
                    "  STATUS='OK'; REPLY=''; " +
                    "  if [ $CODE -eq 2 ]; then " +
                    "    STATUS='CUSTOM'; " +
                    "    REPLY=$(zenity --entry --title='🕊️ Doothan Custom Message' --text='Enter your custom message:' --width=350); " +
                    "  elif [ $CODE -ne 0 ]; then " +
                    "    STATUS='CANCEL'; " +
                    "    REPLY=$(zenity --entry --title='🕊️ Doothan Cancel Reason' --text='Enter reason / custom message:' --width=350); " +
                    "  else " +
                    "    STATUS='OK'; REPLY=''; " +
                    "  fi; " +
                    "else " +
                    "  while true; do " +
                    "    BTN=$(zenity --question --title='🕊️ Doothan Confirmation' " +
                    "          --ok-label='OK' --cancel-label='Cancel' --extra-button='Custom Message' --extra-button='🖼️ View Image' " +
                    "          --text=" + shellQuote(fullMessage + "\n\n📷 [Image Attached - Click 'View Image' to open]") + "); " +
                    "    CODE=$?; " +
                    "    if [ \"$BTN\" = \"🖼️ View Image\" ]; then xdg-open " + shellQuote(fullImageUrl) + " >/dev/null 2>&1; continue; fi; " +
                    "    break; " +
                    "  done; " +
                    "  STATUS='OK'; REPLY=''; " +
                    "  if [ \"$BTN\" = \"Custom Message\" ]; then " +
                    "    STATUS='CUSTOM'; " +
                    "    REPLY=$(zenity --entry --title='🕊️ Doothan Custom Message' --text='Enter your custom message:' --width=350); " +
                    "  elif [ $CODE -ne 0 ]; then " +
                    "    STATUS='CANCEL'; " +
                    "    REPLY=$(zenity --entry --title='🕊️ Doothan Cancel Reason' --text='Enter reason / custom message:' --width=350); " +
                    "  else " +
                    "    STATUS='OK'; REPLY=''; " +
                    "  fi; " +
                    "fi; " +
                    "(sleep 120 && rm -f \"$IMG_FILE\") >/dev/null 2>&1 & " +
                    "curl -s --connect-timeout 5 --max-time 10 -X POST " + shellQuote(callbackUrl) + " " +
                    "--data-urlencode \"messageId=" + messageId + "\" " +
                    "--data-urlencode \"recipientIp=" + hostIp + "\" " +
                    "--data-urlencode \"status=$STATUS\" " +
                    "--data-urlencode \"replyText=$REPLY\" >/dev/null 2>&1";
        }

        return "if which yad >/dev/null 2>&1; then " +
                "  yad --title='🕊️ Doothan Confirmation' --center " +
                "      --text=" + shellQuote(fullMessage) + " " +
                "      --button='OK:0' --button='Cancel:1' --button='Custom Message:2'; " +
                "  CODE=$?; " +
                "  STATUS='OK'; REPLY=''; " +
                "  if [ $CODE -eq 2 ]; then " +
                "    STATUS='CUSTOM'; " +
                "    REPLY=$(zenity --entry --title='🕊️ Doothan Custom Message' --text='Enter your custom message:' --width=350); " +
                "  elif [ $CODE -ne 0 ]; then " +
                "    STATUS='CANCEL'; " +
                "    REPLY=$(zenity --entry --title='🕊️ Doothan Cancel Reason' --text='Enter reason / custom message:' --width=350); " +
                "  else " +
                "    STATUS='OK'; REPLY=''; " +
                "  fi; " +
                "else " +
                "  BTN=$(zenity --question --title='🕊️ Doothan Confirmation' " +
                "        --ok-label='OK' --cancel-label='Cancel' --extra-button='Custom Message' " +
                "        --text=" + shellQuote(fullMessage) + "); " +
                "  CODE=$?; " +
                "  STATUS='OK'; REPLY=''; " +
                "  if [ \"$BTN\" = \"Custom Message\" ]; then " +
                "    STATUS='CUSTOM'; " +
                "    REPLY=$(zenity --entry --title='🕊️ Doothan Custom Message' --text='Enter your custom message:' --width=350); " +
                "  elif [ $CODE -ne 0 ]; then " +
                "    STATUS='CANCEL'; " +
                "    REPLY=$(zenity --entry --title='🕊️ Doothan Cancel Reason' --text='Enter reason / custom message:' --width=350); " +
                "  else " +
                "    STATUS='OK'; REPLY=''; " +
                "  fi; " +
                "fi; " +
                "curl -s --connect-timeout 5 --max-time 10 -X POST " + shellQuote(callbackUrl) + " " +
                "--data-urlencode \"messageId=" + messageId + "\" " +
                "--data-urlencode \"recipientIp=" + hostIp + "\" " +
                "--data-urlencode \"status=$STATUS\" " +
                "--data-urlencode \"replyText=$REPLY\" >/dev/null 2>&1";
    }

    private static String shellQuote(String s) {
        return "'" + s.replace("'", "'\"'\"'") + "'";
    }

  }
