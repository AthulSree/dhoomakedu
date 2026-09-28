$(function () {
    let selectedContact = null;
    let contactsList = [];
    let pollingInterval = null;
    let lastContactsStateString = "";

    // Load contacts initially
    loadContacts();

    // Set search handler
    $("#searchContacts").on("input", function () {
        filterContacts($(this).val());
    });

    // Handle character count in message input
    $("#messageInput").on("input", function () {
        const length = $(this).val().length;
        $("#charCounter").text(`${length} / 500`);
    });

    // Press Enter to send
    $("#messageInput").on("keydown", function (e) {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // Click send button
    $("#sendMessageBtn").on("click", function () {
        sendMessage();
    });

    function loadContacts(restoreId = null) {
        $.ajax({
            url: "/message/api/contacts",
            method: "GET",
            success: function (data) {
                contactsList = data;
                renderContacts(restoreId);
            },
            error: function (err) {
                console.error("Error loading contacts:", err);
            }
        });
    }

    function renderContacts(restoreId = null) {
        const searchQuery = $("#searchContacts").val().toLowerCase().trim();
        
        let filtered = contactsList.filter(c => c.name.toLowerCase().includes(searchQuery));
        
        // Generate a state signature of the filtered contacts to detect changes
        let currentStateString = JSON.stringify(filtered.map(c => ({
            id: c.id,
            isActive: c.isActive,
            lastMessage: c.lastMessage,
            lastMessageTime: c.lastMessageTime,
            selected: selectedContact && selectedContact.id === c.id
        })));

        if (currentStateString === lastContactsStateString) {
            if (restoreId) {
                $("#contactList .contact-item").removeClass("active");
                $(`#contactList .contact-item[data-id="${restoreId}"]`).addClass('active');
            }
            return;
        }
        lastContactsStateString = currentStateString;

        const $contactList = $("#contactList");
        $contactList.empty();

        if (filtered.length === 0) {
            $contactList.append(`
                <div style="text-align: center; color: #64748b; padding: 30px;">
                    <p>No explorers found</p>
                </div>
            `);
            return;
        }

        filtered.forEach(contact => {
            const isGroup = contact.isGroup === true;
            const initials = contact.name.substring(0, 2).toUpperCase();
            const activeClass = selectedContact && selectedContact.id === contact.id ? 'active' : '';
            const statusDot = isGroup ? '' : `<div class="status-dot ${contact.isActive ? 'online' : 'offline'}"></div>`;
            const avatarClass = isGroup ? 'avatar group-avatar' : 'avatar';
            
            const lastMsg = contact.lastMessage || "No messages yet";
            const formattedTime = formatMessageTime(contact.lastMessageTime);

            const $item = $(`
                <div class="contact-item ${activeClass}" data-id="${contact.id}">
                    <div class="${avatarClass}">
                        ${initials}
                        ${statusDot}
                    </div>
                    <div class="contact-details">
                        <div class="contact-name-row">
                            <span class="contact-name">${contact.name}</span>
                            <span class="contact-time">${formattedTime}</span>
                        </div>
                        <div class="contact-last-msg">${lastMsg}</div>
                    </div>
                </div>
            `);

            $item.on("click", function () {
                selectContact(contact);
            });

            $contactList.append($item);
        });

        // Restore selected contact styling
        if (restoreId) {
            $(`#contactList .contact-item[data-id="${restoreId}"]`).addClass('active');
        }
    }

    function filterContacts(query) {
        renderContacts(selectedContact ? selectedContact.id : null);
    }

    function selectContact(contact) {
        selectedContact = contact;
        
        // Update styling
        $("#contactList .contact-item").removeClass("active");
        $(`#contactList .contact-item[data-id="${contact.id}"]`).addClass("active");

        // Set up Chat UI Header
        $("#chatPlaceholder").hide();
        $("#chatHeader").show();
        $("#chatMessages").show();
        $("#chatFooter").show();

        // Update header details
        $("#chatHeaderName").text(contact.name);
        $("#chatHeaderAvatar").text(contact.name.substring(0, 2).toUpperCase());
        if (contact.isGroup) {
            $("#chatHeaderAvatar").addClass("group-avatar");
            $("#chatHeaderStatus").text("Group Broadcast");
            $("#chatHeaderIp").text("");
        } else {
            $("#chatHeaderAvatar").removeClass("group-avatar");
            $("#chatHeaderStatus").text(contact.isActive ? "online" : "offline");
            $("#chatHeaderIp").text(contact.ip || "");
        }

        // Reset message form inputs
        $("#messageInput").val("").trigger("input");
        $("#msgType").val("normal");

        // Load chat history
        window._lastRenderedState = null;
        loadChatHistory(contact, true);

        // Reset polling timer to avoid collisions
        if (pollingInterval) clearInterval(pollingInterval);
        pollingInterval = setInterval(pollUpdate, 3000);
    }

    function loadChatHistory(contact, forceScroll = false) {
        if (!contact) return;
        const target = contact.isGroup ? contact.name : contact.ip;
        
        $.ajax({
            url: "/message/api/history",
            method: "GET",
            data: {
                target: target,
                isGroup: contact.isGroup === true
            },
            success: function (data) {
                renderMessages(data, forceScroll);
            },
            error: function (err) {
                console.error("Error loading history:", err);
            }
        });
    }

    function renderMessages(messages, forceScroll = false) {
        const $chatMessages = $("#chatMessages");
        
        // Cache scroll position to see if user is currently scrolled up
        const scrollHeight = $chatMessages[0].scrollHeight;
        const scrollTop = $chatMessages[0].scrollTop;
        const clientHeight = $chatMessages[0].clientHeight;
        const isScrolledToBottom = scrollHeight - scrollTop - clientHeight < 100;

        // Skip redraw if message content and confirmation states haven't changed
        const messagesStateKey = JSON.stringify(messages.map(m => ({
            id: m.id,
            confs: (m.confirmations || []).map(c => c.recipientIp + ':' + c.status)
        })));
        if (!forceScroll && window._lastRenderedState === messagesStateKey) {
            return;
        }
        window._lastRenderedState = messagesStateKey;

        $chatMessages.empty();

        if (messages.length === 0) {
            $chatMessages.append(`
                <div style="text-align: center; color: #64748b; margin-top: 50px;">
                    <p>No messages in this transmission yet.</p>
                </div>
            `);
            return;
        }

        messages.forEach(msg => {
            const isOutgoing = msg.senderIp === clientIp;
            const bubbleClass = isOutgoing ? 'outgoing' : 'incoming';
            const displaySender = isOutgoing ? 'You' : msg.senderName;
            
            const typeTag = msg.msgType !== 'normal' ? `<span class="msg-type-tag ${msg.msgType}">${msg.msgType === 'confirm' ? 'Confirmation' : msg.msgType}</span>` : '';
            const statusCheck = isOutgoing ? '<i class="fa-solid fa-check-double message-status-icon"></i>' : '';
            const formattedTime = formatMessageTime(msg.timestamp);

            // Render confirmation details if type is confirm
            let confirmationHtml = '';
            if (msg.msgType === 'confirm') {
                const confs = msg.confirmations || [];
                if (isOutgoing) {
                    // Sent by current user -> View responses
                    if (selectedContact && selectedContact.isGroup && confs.length > 1) {
                        const okCount = confs.filter(c => c.status === 'OK').length;
                        const cancelCount = confs.filter(c => c.status === 'CANCEL').length;
                        const pendingCount = confs.filter(c => c.status === 'PENDING').length;

                        let listItems = '';
                        confs.forEach(c => {
                            let pill = '';
                            if (c.status === 'OK') {
                                pill = `<span class="confirmation-status-pill ok"><i class="fa fa-check"></i> OK</span>`;
                            } else if (c.status === 'CANCEL') {
                                pill = `<span class="confirmation-status-pill cancel"><i class="fa fa-times"></i> Cancel</span>`;
                            } else {
                                pill = `<span class="confirmation-status-pill pending"><i class="fa fa-clock"></i> Pending</span>`;
                            }
                            const rTime = c.respondedAt ? `<span class="conf-time">${formatMessageTime(c.respondedAt)}</span>` : '';
                            listItems += `
                                <div class="group-conf-item">
                                    <span>${escapeHtml(c.recipientName || c.recipientIp)}</span>
                                    <div>${pill} ${rTime}</div>
                                </div>
                            `;
                        });

                        confirmationHtml = `
                            <div class="confirmation-box">
                                <div class="confirmation-title">
                                    <span>Responses</span>
                                    <span style="color: #cbd5e1;">${okCount} OK • ${cancelCount} Cancel • ${pendingCount} Pending</span>
                                </div>
                                <div class="group-conf-list">
                                    ${listItems}
                                </div>
                            </div>
                        `;
                    } else {
                        // 1-on-1 outgoing
                        const conf = confs[0];
                        if (conf) {
                            let pill = '';
                            if (conf.status === 'OK') {
                                pill = `<span class="confirmation-status-pill ok"><i class="fa fa-check"></i> Confirmed (OK)</span> <span class="conf-time">${formatMessageTime(conf.respondedAt)}</span>`;
                            } else if (conf.status === 'CANCEL') {
                                pill = `<span class="confirmation-status-pill cancel"><i class="fa fa-times"></i> Cancelled</span> <span class="conf-time">${formatMessageTime(conf.respondedAt)}</span>`;
                            } else {
                                pill = `<span class="confirmation-status-pill pending"><i class="fa fa-clock"></i> Awaiting response...</span>`;
                            }
                            confirmationHtml = `
                                <div class="confirmation-box">
                                    <div class="confirmation-title"><span>Recipient Response</span></div>
                                    <div>${pill}</div>
                                </div>
                            `;
                        }
                    }
                } else {
                    // Incoming message to current user -> Provide action buttons or show your response
                    const myConf = confs.find(c => c.recipientIp === clientIp) || confs[0];
                    if (myConf && myConf.status === 'PENDING') {
                        confirmationHtml = `
                            <div class="confirmation-box">
                                <div class="confirmation-title"><span>Confirmation Requested</span></div>
                                <div class="confirmation-actions">
                                    <button class="btn-confirm-action btn-confirm-ok" data-msg-id="${msg.id}" data-ip="${clientIp}">
                                        <i class="fa fa-check"></i> OK
                                    </button>
                                    <button class="btn-confirm-action btn-confirm-cancel" data-msg-id="${msg.id}" data-ip="${clientIp}">
                                        <i class="fa fa-times"></i> Cancel
                                    </button>
                                </div>
                            </div>
                        `;
                    } else if (myConf) {
                        let pill = '';
                        if (myConf.status === 'OK') {
                            pill = `<span class="confirmation-status-pill ok"><i class="fa fa-check"></i> You selected OK</span> <span class="conf-time">${formatMessageTime(myConf.respondedAt)}</span>`;
                        } else if (myConf.status === 'CANCEL') {
                            pill = `<span class="confirmation-status-pill cancel"><i class="fa fa-times"></i> You selected Cancel</span> <span class="conf-time">${formatMessageTime(myConf.respondedAt)}</span>`;
                        }
                        confirmationHtml = `
                            <div class="confirmation-box">
                                <div class="confirmation-title"><span>Your Response</span></div>
                                <div>${pill}</div>
                            </div>
                        `;
                    }
                }
            }

            const $bubble = $(`
                <div class="message-bubble ${bubbleClass}">
                    <div class="msg-sender">${displaySender}</div>
                    ${typeTag}
                    <div class="message-content">${escapeHtml(msg.messageContent)}</div>
                    ${confirmationHtml}
                    <div class="message-time-row">
                        <span class="message-time">${formattedTime}</span>
                        ${statusCheck}
                    </div>
                </div>
            `);

            $chatMessages.append($bubble);
        });

        // Scroll to bottom if we forced it (on contact switch) or if the user was already at the bottom
        if (forceScroll || isScrolledToBottom) {
            $chatMessages.scrollTop($chatMessages[0].scrollHeight);
        }
    }

    function sendMessage() {
        if (!selectedContact) return;
        const message = $("#messageInput").val().trim();
        const msgType = $("#msgType").val();

        if (!message) return;

        // Optimistically disable input/send button to prevent double-clicks
        $("#messageInput").prop("disabled", true);
        $("#sendMessageBtn").prop("disabled", true);

        const postData = {
            message: message,
            msgType: msgType
        };

        if (selectedContact.isGroup) {
            postData.groupName = selectedContact.name;
        } else {
            postData["hostId[]"] = [parseInt(selectedContact.id)];
        }

        $.ajax({
            url: "/message/api/send",
            method: "POST",
            data: postData,
            success: function (res) {
                // Clear input
                $("#messageInput").val("").trigger("input");
                
                // Re-enable inputs
                $("#messageInput").prop("disabled", false).focus();
                $("#sendMessageBtn").prop("disabled", false);

                // Reload contacts list and chat history
                loadContacts(selectedContact.id);
                loadChatHistory(selectedContact, true);
            },
            error: function (err) {
                alert("Transmission failed. Please check the network or system status.");
                $("#messageInput").prop("disabled", false);
                $("#sendMessageBtn").prop("disabled", false);
            }
        });
    }

    // Confirmation action button handler (OK / Cancel)
    $(document).on("click", ".btn-confirm-action", function (e) {
        e.preventDefault();
        const $btn = $(this);
        const messageId = $btn.data("msg-id");
        const recipientIp = $btn.data("ip");
        const status = $btn.hasClass("btn-confirm-ok") ? "OK" : "CANCEL";

        $btn.closest(".confirmation-actions").find("button").prop("disabled", true);

        $.ajax({
            url: "/message/api/confirm",
            method: "POST",
            data: {
                messageId: messageId,
                recipientIp: recipientIp,
                status: status
            },
            success: function () {
                window._lastRenderedState = null;
                if (selectedContact) {
                    loadChatHistory(selectedContact, false);
                }
            },
            error: function (err) {
                console.error("Failed to submit confirmation:", err);
                $btn.closest(".confirmation-actions").find("button").prop("disabled", false);
            }
        });
    });

    function pollUpdate() {
        if (selectedContact) {
            // Load messages for selected contact
            loadChatHistory(selectedContact, false);
        }
        // Load contact previews without disturbing view
        loadContacts(selectedContact ? selectedContact.id : null);
    }

    // Helper functions
    function formatMessageTime(isoString) {
        if (!isoString) return "";
        try {
            const date = new Date(isoString);
            const now = new Date();
            const isToday = date.toDateString() === now.toDateString();
            
            const timeStr = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
            if (isToday) {
                return timeStr;
            } else {
                const dateStr = date.toLocaleDateString([], { month: 'short', day: 'numeric' });
                return `${dateStr} ${timeStr}`;
            }
        } catch (e) {
            return "";
        }
    }

    function escapeHtml(str) {
        return str
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
});