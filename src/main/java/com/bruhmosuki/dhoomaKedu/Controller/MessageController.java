package com.bruhmosuki.dhoomaKedu.Controller;

import org.springframework.ui.Model;
import com.bruhmosuki.dhoomaKedu.service.sshService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.bruhmosuki.dhoomaKedu.entity.groupHost;
import com.bruhmosuki.dhoomaKedu.entity.employee;
import com.bruhmosuki.dhoomaKedu.service.groupHostService;
import com.bruhmosuki.dhoomaKedu.service.employeeService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/message")
public class MessageController {

    private sshService sshService;
    private groupHostService groupHostService;
    private employeeService employeeService;

    public MessageController(sshService sshService, groupHostService groupHostService,
            employeeService employeeService) {
        this.sshService = sshService;
        this.groupHostService = groupHostService;
        this.employeeService = employeeService;
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
            clientIp = "10.162.6.11";
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
            HttpServletRequest request) {

        String clientIp = request.getHeader("X-Forwarded-For");

        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        } else {
            // In case of multiple proxies, the first IP is the client
            clientIp = clientIp.split(",")[0].trim();
        }
        if ("0:0:0:0:0:0:0:1".equals(clientIp)) {
            clientIp = "10.162.6.11";
        }

        employee sender = employeeService.findBySysIp(clientIp);
        String senderName = (sender != null) ? sender.getFirst_name() + " " + sender.getLast_name() : "Unknown Sender";

        if(clientIp.equals("10.162.6.11")){
            senderName = "Brahm-0-zuki";
        }

        // Append sender name to message
        String fullMessage = message + "\n\nRegards,\n" + senderName;

        for (Integer hostId1 : hostId) {
            groupHost groupHost = groupHostService.findById(hostId1);
            System.out.println("hostIds: " + groupHost.getUserName());
            String hostIp = groupHost.getHost();
            String hostUserName = groupHost.getUserName();
            String hostPassword = groupHost.getPassword();

            String command = "export DISPLAY=:0; " +
                    "nohup zenity --info " +
                    "--title='\uD83D\uDD4A\uFE0F Doothan Incoming...' " +
                    "--text=\"" + fullMessage + "\" " + // wrap message in quotes
                    " > /dev/null 2>&1 &";

            String cmd = String.format(command, hostUserName);
            sshService.sendCommand(hostIp, hostUserName, hostPassword, cmd);
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
}
