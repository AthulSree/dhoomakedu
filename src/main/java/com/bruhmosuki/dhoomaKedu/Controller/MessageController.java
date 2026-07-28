package com.bruhmosuki.dhoomaKedu.Controller;

import org.springframework.ui.Model;
import com.bruhmosuki.dhoomaKedu.service.sshService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.bruhmosuki.dhoomaKedu.entity.groupHost;
import com.bruhmosuki.dhoomaKedu.entity.employee;
import com.bruhmosuki.dhoomaKedu.service.groupHostService;
import com.bruhmosuki.dhoomaKedu.service.commonServices;
import com.bruhmosuki.dhoomaKedu.service.employeeService;
import com.bruhmosuki.dhoomaKedu.dao.chatMessageRepository;
import com.bruhmosuki.dhoomaKedu.entity.chatMessage;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDateTime;

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
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Value("${developer.mock.ip:}")
    private String developerMockIp;

    public MessageController(sshService sshService, groupHostService groupHostService,
            employeeService employeeService, commonServices theCommonService,
            chatMessageRepository chatMessageRepository) {
        this.sshService = sshService;
        this.groupHostService = groupHostService;
        this.employeeService = employeeService;
        this.theCommonService = theCommonService;
        this.chatMessageRepository = chatMessageRepository;
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
        if ("0:0:0:0:0:0:0:1".equals(clientIp)) {
            clientIp = developerMockIp;
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
        if ("0:0:0:0:0:0:0:1".equals(clientIp)) {
            clientIp = developerMockIp;
        }

        employee sender = employeeService.findBySysIp(clientIp);
        String senderName = (sender != null) ? sender.getFirst_name() + " " + sender.getLast_name() : "Unknown Sender";

        if (clientIp.equals("10.162.6.11")) {
            senderName = "Brahmo-Z";
        }

        // Append sender name to message
        String fullMessage = message + "\n\n\nRegards,\n" + senderName + "\n[Nēnu nīke Dūtanu]";

        for (Integer hostId1 : hostId) {
            groupHost groupHost = groupHostService.findById(hostId1);
            System.out.println("hostIds: " + groupHost.getUserName());
            String hostIp = groupHost.getHost();
            String hostUserName = groupHost.getUserName();
            String hostPassword = groupHost.getPassword();

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
        if ("0:0:0:0:0:0:0:1".equals(clientIp)) {
            clientIp = developerMockIp;
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
            {"1", "2", "3", "5", "6", "7", "8", "9", "10", "12"},
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

        return ResponseEntity.ok(history);
    }

    @PostMapping("/api/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMsgApi(
            @RequestParam(value = "hostId[]", required = false) List<Integer> hostIds,
            @RequestParam(value = "groupName", required = false) String groupName,
            @RequestParam("message") String message,
            @RequestParam(value = "msgType", defaultValue = "normal") String msgType,
            HttpServletRequest request) {

        String clientIp = getClientIp(request);
        String senderName = getSenderName(clientIp);
        Map<String, Object> response = new HashMap<>();

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

          String fullMessage = message + "\n\n\nRegards,\n" + senderName + "\n[Nēnu nīke Dūtanu]";

          LocalDateTime now = LocalDateTime.now();
          if (groupName != null && !groupName.isEmpty()) {
              chatMessage chatMsg = new chatMessage(
                  clientIp,
                  senderName,
                  null,
                  groupName + " Broadcast",
                  message,
                  msgType,
                  groupName,
                  now
              );
              chatMessageRepository.save(chatMsg);
          } else {
              for (groupHost gh : recipients) {
                  chatMessage chatMsg = new chatMessage(
                      clientIp,
                      senderName,
                      gh.getHost(),
                      gh.getUserName(),
                      message,
                      msgType,
                      null,
                      now
                  );
                  chatMessageRepository.save(chatMsg);
              }
          }

          for (groupHost gh : recipients) {
              String hostIp = gh.getHost();
              String hostUserName = gh.getUserName();
              String hostPassword = gh.getPassword();

              executorService.submit(() -> {
                  String command = "";
                  if ("docsquad".equals(msgType)) {
                      String url = "http://10.162.6.180:8015/docsquad/";
                      String text = fullMessage + "\n\nLink: " + url;
                      String dialogFlow = "if zenity --question --title='Doothan Incoming...' --ok-label='Open the byte-WhaSSH 2.0 / DocsQuad' --cancel-label='Close' --text=" + shellQuote(text) + "; then xdg-open " + shellQuote(url) + " >/dev/null 2>&1; fi";
                      command = "export DISPLAY=:0; nohup bash -lc " + shellQuote(dialogFlow) + " >/dev/null 2>&1 &";
                  } else if ("leave".equals(msgType)) {
                      String url = "http://10.162.6.188:7003/leaves/manage";
                      String text = fullMessage + "\n\nLink: " + url;
                      String dialogFlow = "if zenity --question --title='Doothan Incoming...' --ok-label='Open Leave Portal' --cancel-label='Close' --text=" + shellQuote(text) + "; then xdg-open " + shellQuote(url) + " >/dev/null 2>&1; fi";
                      command = "export DISPLAY=:0; nohup bash -lc " + shellQuote(dialogFlow) + " >/dev/null 2>&1 &";
                  } else {
                      command = "export DISPLAY=:0; nohup zenity --info --title='\uD83D\uDD4A\uFE0F Doothan Incoming...' --text=\"" + fullMessage + "\" > /dev/null 2>&1 &";
                  }

                  sshService.sendCommand(hostIp, hostUserName, hostPassword, command);
              });
          }

          response.put("success", true);
          response.put("timestamp", now.toString());
          return ResponseEntity.ok(response);
      }

      private static String shellQuote(String s) {
          return "'" + s.replace("'", "'\"'\"'") + "'";
      }

  }
