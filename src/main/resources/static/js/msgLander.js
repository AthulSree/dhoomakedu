$(function () {
    let selectedContact = null;
    let contactsList = [];
    let pollingInterval = null;
    let lastContactsStateString = "";
    let currentAttachedFile = null;

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
        clearAttachment();

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

        // Skip redraw if message content, attachments, and confirmation states haven't changed
        const messagesStateKey = JSON.stringify(messages.map(m => ({
            id: m.id,
            att: m.attachmentUrl,
            confs: (m.confirmations || []).map(c => c.recipientIp + ':' + c.status + ':' + (c.replyText || ''))
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

        function formatConfirmationPill(c) {
            if (!c) return '';
            if (c.status === 'OK') {
                return `<span class="confirmation-status-pill ok"><i class="fa fa-check"></i> OK</span>`;
            } else if (c.status === 'CANCEL') {
                const rText = c.replyText ? `Cancel: ${escapeHtml(c.replyText)}` : 'Cancel';
                return `<span class="confirmation-status-pill cancel" title="${escapeHtml(c.replyText || '')}"><i class="fa fa-times"></i> ${rText}</span>`;
            } else if (c.status === 'CUSTOM') {
                const rText = c.replyText ? escapeHtml(c.replyText) : 'Custom Reply';
                return `<span class="confirmation-status-pill custom" title="${escapeHtml(c.replyText || '')}"><i class="fa fa-comment"></i> ${rText}</span>`;
            } else {
                return `<span class="confirmation-status-pill pending"><i class="fa fa-clock"></i> Pending</span>`;
            }
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
                        const customCount = confs.filter(c => c.status === 'CUSTOM').length;
                        const pendingCount = confs.filter(c => c.status === 'PENDING').length;

                        let listItems = '';
                        confs.forEach(c => {
                            const pill = formatConfirmationPill(c);
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
                                    <span style="color: #cbd5e1;">${okCount} OK • ${cancelCount} Cancel • ${customCount} Custom • ${pendingCount} Pending</span>
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
                            const pill = formatConfirmationPill(conf);
                            const rTime = conf.respondedAt ? `<span class="conf-time">${formatMessageTime(conf.respondedAt)}</span>` : '';
                            confirmationHtml = `
                                <div class="confirmation-box">
                                    <div class="confirmation-title"><span>Recipient Response</span></div>
                                    <div>${pill} ${rTime}</div>
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
                                    <button class="btn-confirm-action btn-confirm-custom" data-msg-id="${msg.id}" data-ip="${clientIp}">
                                        <i class="fa fa-comment"></i> Custom Message
                                    </button>
                                </div>
                            </div>
                        `;
                    } else if (myConf) {
                        const pill = formatConfirmationPill(myConf);
                        const rTime = myConf.respondedAt ? `<span class="conf-time">${formatMessageTime(myConf.respondedAt)}</span>` : '';
                        confirmationHtml = `
                            <div class="confirmation-box">
                                <div class="confirmation-title"><span>Your Response</span></div>
                                <div>${pill} ${rTime}</div>
                            </div>
                        `;
                    }
                }
            }

    function normalizeAttachmentUrl(url) {
        if (!url) return '';
        if (url.startsWith('/uploads/')) {
            return '/message' + url;
        }
        return url;
    }

    function optimizeImageBeforeUpload(file, callback) {
        if (!file) {
            callback(null);
            return;
        }
        // If file is under 750 KB or is GIF/SVG, send as-is
        if (file.size <= 750 * 1024 || file.type === 'image/gif' || file.type === 'image/svg+xml') {
            callback(file);
            return;
        }

        const reader = new FileReader();
        reader.onload = function (e) {
            const img = new Image();
            img.onload = function () {
                let width = img.width;
                let height = img.height;
                const maxDim = 1600;
                if (width > maxDim || height > maxDim) {
                    if (width > height) {
                        height = Math.round((height * maxDim) / width);
                        width = maxDim;
                    } else {
                        width = Math.round((width * maxDim) / height);
                        height = maxDim;
                    }
                }

                const canvas = document.createElement("canvas");
                canvas.width = width;
                canvas.height = height;
                const ctx = canvas.getContext("2d");
                ctx.drawImage(img, 0, 0, width, height);

                canvas.toBlob(function (blob) {
                    if (blob && blob.size < file.size) {
                        const newName = file.name.replace(/\.[^.]+$/, "") + ".jpg";
                        const optimizedFile = new File([blob], newName, {
                            type: "image/jpeg",
                            lastModified: Date.now()
                        });
                        callback(optimizedFile);
                    } else {
                        callback(file);
                    }
                }, "image/jpeg", 0.82);
            };
            img.onerror = function () {
                callback(file);
            };
            img.src = e.target.result;
        };
        reader.onerror = function () {
            callback(file);
        };
        reader.readAsDataURL(file);
    }

            let imageHtml = '';
            if (msg.attachmentUrl) {
                const imgUrl = normalizeAttachmentUrl(msg.attachmentUrl);
                imageHtml = `
                    <div class="message-image-wrapper">
                        <img src="${imgUrl}" class="message-image" alt="Image" loading="lazy" />
                    </div>
                `;
            }

            const showText = msg.messageContent && msg.messageContent !== '[📷 Image]' && msg.messageContent.trim().length > 0;
            const contentHtml = showText ? `<div class="message-content">${escapeHtml(msg.messageContent)}</div>` : '';

            const $bubble = $(`
                <div class="message-bubble ${bubbleClass}">
                    <div class="msg-sender">${displaySender}</div>
                    ${typeTag}
                    ${imageHtml}
                    ${contentHtml}
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

        if (!message && !currentAttachedFile) return;

        // Optimistically disable input/send button to prevent double-clicks
        $("#messageInput").prop("disabled", true);
        $("#sendMessageBtn").prop("disabled", true);
        $("#attachBtn").prop("disabled", true);

        optimizeImageBeforeUpload(currentAttachedFile, function (fileToSend) {
            const formData = new FormData();
            formData.append("message", message);
            formData.append("msgType", msgType);
            if (fileToSend) {
                formData.append("image", fileToSend);
            }

            if (selectedContact.isGroup) {
                formData.append("groupName", selectedContact.name);
            } else {
                formData.append("hostId[]", parseInt(selectedContact.id));
            }

            $.ajax({
                url: "/message/api/send",
                method: "POST",
                data: formData,
                processData: false,
                contentType: false,
                success: function (res) {
                    // Clear input & attachment
                    $("#messageInput").val("").trigger("input");
                    clearAttachment();
                    
                    // Re-enable inputs
                    $("#messageInput").prop("disabled", false).focus();
                    $("#sendMessageBtn").prop("disabled", false);
                    $("#attachBtn").prop("disabled", false);

                    // Reload contacts list and chat history
                    loadContacts(selectedContact.id);
                    loadChatHistory(selectedContact, true);
                },
                error: function (xhr, status, error) {
                    let errorMsg = "Transmission failed. Please check the network or system status.";
                    if (xhr.status === 413) {
                        errorMsg = "Transmission failed (HTTP 413): The image is too large for the proxy server limit (client_max_body_size in Nginx).";
                    } else if (xhr.responseJSON && xhr.responseJSON.error) {
                        errorMsg = "Transmission failed: " + xhr.responseJSON.error;
                    } else if (xhr.status) {
                        errorMsg = "Transmission failed (HTTP " + xhr.status + ": " + (xhr.statusText || error) + ").";
                    }
                    alert(errorMsg);
                    $("#messageInput").prop("disabled", false);
                    $("#sendMessageBtn").prop("disabled", false);
                    $("#attachBtn").prop("disabled", false);
                }
            });
        });
    }

    // Confirmation action button handler (OK / Cancel / Custom Message)
    $(document).on("click", ".btn-confirm-action", function (e) {
        e.preventDefault();
        const $btn = $(this);
        const messageId = $btn.data("msg-id");
        const recipientIp = $btn.data("ip");
        let status = "OK";
        let replyText = "";

        if ($btn.hasClass("btn-confirm-ok")) {
            status = "OK";
            replyText = ""; // OK is clicked -> do nothing extra
        } else if ($btn.hasClass("btn-confirm-cancel")) {
            status = "CANCEL";
            const entered = prompt("Enter reason / message for cancellation (optional):");
            if (entered === null) {
                return; // User cancelled the prompt dialog
            }
            replyText = entered;
        } else if ($btn.hasClass("btn-confirm-custom")) {
            status = "CUSTOM";
            const entered = prompt("Enter your custom message:");
            if (entered === null || entered.trim() === "") {
                return; // User cancelled or entered empty string
            }
            replyText = entered;
        }

        $btn.closest(".confirmation-actions").find("button").prop("disabled", true);

        $.ajax({
            url: "/message/api/confirm",
            method: "POST",
            data: {
                messageId: messageId,
                recipientIp: recipientIp,
                status: status,
                replyText: replyText
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

    // Attachment event handlers
    $("#attachBtn").on("click", function () {
        $("#imageFileInput").val("").click();
    });

    $("#imageFileInput").on("change", function () {
        if (this.files && this.files[0]) {
            setAttachment(this.files[0]);
        }
    });

    $("#removeImageBtn").on("click", function () {
        clearAttachment();
    });

    // Paste from clipboard support (Ctrl + V)
    $("#messageInput").on("paste", function (e) {
        const clipboardData = e.originalEvent.clipboardData || window.clipboardData;
        if (!clipboardData || !clipboardData.items) return;

        for (let i = 0; i < clipboardData.items.length; i++) {
            const item = clipboardData.items[i];
            if (item.type && item.type.indexOf("image") !== -1) {
                const file = item.getAsFile();
                if (file) {
                    setAttachment(file);
                    break;
                }
            }
        }
    });

    function setAttachment(file) {
        if (!file.type || !file.type.startsWith("image/")) {
            alert("Only image files (PNG, JPG, GIF, WebP) are supported.");
            return;
        }
        if (file.size > 25 * 1024 * 1024) {
            alert("Image size exceeds the 25MB limit.");
            return;
        }
        currentAttachedFile = file;
        $("#attachBtn").addClass("has-file");

        const reader = new FileReader();
        reader.onload = function (e) {
            $("#imagePreviewImg").attr("src", e.target.result);
            $("#imagePreviewName").text(file.name || "pasted-image.png");
            $("#imagePreviewSize").text((file.size / 1024).toFixed(1) + " KB");
            $("#imagePreviewContainer").slideDown(150);
        };
        reader.readAsDataURL(file);
    }

    function clearAttachment() {
        currentAttachedFile = null;
        $("#attachBtn").removeClass("has-file");
        $("#imageFileInput").val("");
        $("#imagePreviewImg").attr("src", "");
        $("#imagePreviewContainer").slideUp(150);
    }

    // Lightbox modal event handlers
    $(document).on("click", ".message-image", function (e) {
        e.stopPropagation();
        const src = $(this).attr("src");
        $("#lightboxImg").attr("src", src);
        $("#chatLightbox").fadeIn(150);
    });

    $("#chatLightbox, #lightboxClose").on("click", function (e) {
        if (e.target.id === "lightboxImg") return;
        $("#chatLightbox").fadeOut(150);
    });

    $(document).on("keydown", function (e) {
        if (e.key === "Escape" && $("#chatLightbox").is(":visible")) {
            $("#chatLightbox").fadeOut(150);
        }
    });
});