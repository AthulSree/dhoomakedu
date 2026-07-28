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

        // Skip redraw if message count hasn't changed and we aren't forcing a reload
        const currentBubbleCount = $chatMessages.find(".message-bubble").length;
        if (!forceScroll && messages.length === currentBubbleCount) {
            return;
        }

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
            
            const typeTag = msg.msgType !== 'normal' ? `<span class="msg-type-tag ${msg.msgType}">${msg.msgType}</span>` : '';
            const statusCheck = isOutgoing ? '<i class="fa-solid fa-check-double message-status-icon"></i>' : '';
            const formattedTime = formatMessageTime(msg.timestamp);

            const $bubble = $(`
                <div class="message-bubble ${bubbleClass}">
                    <div class="msg-sender">${displaySender}</div>
                    ${typeTag}
                    <div class="message-content">${escapeHtml(msg.messageContent)}</div>
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